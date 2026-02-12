package com.tcontur.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Clase base genérica para clientes AIDL
 *
 * Uso CON package conocido:
 * ```
 * class MiCliente(context: Context) : AidlClientBase(
 *     context = context,
 *     serviceAction = "com.tcontur.aidl.REMOTE_SERVICE",
 *     servicePackage = "com.example.demo_aidl"
 * )
 * ```
 *
 * Uso SIN package (auto-discovery):
 * ```
 * class MiCliente(context: Context) : AidlClientBase(
 *     context = context,
 *     serviceAction = "com.tcontur.aidl.REMOTE_SERVICE"
 * )
 * ```
 */
abstract class AidlClientBase(
    private val context: Context,
    private val serviceAction: String,
    private val servicePackage: String? = null
) {

    private var service: IRemoteService? = null
    private var isBound = false

    private val _messages = MutableStateFlow("Desconectado")
    val messages: StateFlow<String> = _messages

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    companion object {
        const val TAG = "AidlClientBase"
    }

    /**
     * Override este método para manejar mensajes recibidos del servicio
     */
    protected abstract fun onMessageReceived(message: String)

    /**
     * Override para manejar cambios de conexión (opcional)
     */
    protected open fun onConnectionChanged(state: ConnectionState) {
        Log.d(TAG, "🔄 Estado: $state")
    }

    private val callback = object : IRemoteCallback.Stub() {
        override fun onMessage(message: String) {
            Log.d(TAG, "📩 Mensaje recibido: $message")

            when {
                message.startsWith("CONNECTION:") -> {
                    val status = message.substringAfter(":")
                    val newState = if (status == "conectado")
                        ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
                    updateConnectionState(newState)
                }
                message == "PONG" -> updateConnectionState(ConnectionState.CONNECTED)
                else -> {
                    _messages.value = message
                    onMessageReceived(message)
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            try {
                service = IRemoteService.Stub.asInterface(binder)
                service?.registerCallback(callback)
                updateConnectionState(ConnectionState.CONNECTED)
                _messages.value = "Conectado"
                Log.d(TAG, "✅ Servicio conectado")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error conectando: ${e.message}", e)
                _messages.value = "Error: ${e.message}"
                updateConnectionState(ConnectionState.ERROR)
                service = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            isBound = false
            updateConnectionState(ConnectionState.DISCONNECTED)
            _messages.value = "Desconectado"
            Log.d(TAG, "🔌 Servicio desconectado")
        }
    }

    /**
     * Descubre automáticamente el package del servicio
     */
    private fun discoverServicePackage(): String? {
        return try {
            val intent = Intent(serviceAction)
            val services = context.packageManager.queryIntentServices(intent, 0)

            val pkg = services.firstOrNull()?.serviceInfo?.packageName
            if (pkg != null) {
                Log.d(TAG, "🔍 Servicio descubierto: $pkg")
            } else {
                Log.w(TAG, "⚠️ No se encontró servicio para: $serviceAction")
            }
            pkg
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error descubriendo servicio: ${e.message}", e)
            null
        }
    }

    /**
     * Conecta al servicio AIDL remoto
     */
    fun connect() {
        if (isBound) {
            Log.d(TAG, "⚠️ Ya está conectado")
            return
        }

        try {
            updateConnectionState(ConnectionState.CONNECTING)

            // Usar package proporcionado o descubrirlo automáticamente
            val targetPackage = servicePackage ?: discoverServicePackage()

            if (targetPackage == null) {
                _messages.value = "Servicio no encontrado. ¿Está instalada la app?"
                updateConnectionState(ConnectionState.ERROR)
                Log.e(TAG, "❌ No se pudo determinar el package del servicio")
                return
            }

            Log.d(TAG, "🔗 Conectando a $targetPackage...")

            val intent = Intent(serviceAction).apply {
                setPackage(targetPackage)
            }

            val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                _messages.value = "No se pudo conectar a $targetPackage"
                updateConnectionState(ConnectionState.ERROR)
                Log.e(TAG, "❌ bindService retornó false")
            } else {
                isBound = true
                Log.d(TAG, "⏳ Esperando conexión...")
            }
        } catch (e: Exception) {
            _messages.value = "Error al conectar: ${e.message}"
            updateConnectionState(ConnectionState.ERROR)
            Log.e(TAG, "❌ Error: ${e.message}", e)
        }
    }

    /**
     * Envía un comando al servicio
     */
    fun sendCommand(command: String) {
        try {
            Log.d(TAG, "📤 Enviando comando: $command")
            service?.sendCommand(command)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando comando: ${e.message}", e)
            _messages.value = "Error enviando comando"
            updateConnectionState(ConnectionState.ERROR)
        }
    }

    /**
     * Desconecta del servicio
     */
    fun disconnect() {
        if (!isBound) {
            Log.d(TAG, "⚠️ Ya está desconectado")
            return
        }

        try {
            Log.d(TAG, "🔌 Desconectando...")
            service?.unregisterCallback(callback)
            context.unbindService(connection)
            service = null
            isBound = false
            updateConnectionState(ConnectionState.DISCONNECTED)
            _messages.value = "Desconectado"
            Log.d(TAG, "✅ Desconectado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desconectando: ${e.message}", e)
            isBound = false
            service = null
        }
    }

    /**
     * Verifica si está conectado
     */
    fun isConnected(): Boolean = isBound && service != null

    private fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        onConnectionChanged(state)
    }
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
