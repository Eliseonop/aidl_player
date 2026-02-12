package com.tcontur.aidl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.tcontur.aidl.IRemoteCallback
import com.tcontur.aidl.IRemoteService
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Clase base genérica para crear servicios AIDL
 *
 * Uso:
 * ```
 * class MiServicio : AidlServiceBase() {
 *     override fun onCommandReceived(command: String) {
 *         when (command) {
 *             "PLAY" -> // tu lógica
 *             "STOP" -> // tu lógica
 *         }
 *     }
 * }
 * ```
 */
abstract class AidlServiceBase : Service() {

    private val callbacks = CopyOnWriteArrayList<CallbackWrapper>()
    private var clientsConnected = 0

    companion object {
        const val TAG = "AidlServiceBase"
    }

    private data class CallbackWrapper(
        val callback: IRemoteCallback,
        val deathRecipient: IBinder.DeathRecipient
    )

    /**
     * Override este método para manejar comandos recibidos
     */
    protected abstract fun onCommandReceived(command: String)

    /**
     * Override este método para ejecutar lógica cuando un cliente se conecta
     */
    protected open fun onClientConnected(clientCount: Int) {
        Log.d(TAG, "✅ Cliente conectado. Total: $clientCount")
    }

    /**
     * Override este método para ejecutar lógica cuando un cliente se desconecta
     */
    protected open fun onClientDisconnected(clientCount: Int) {
        Log.d(TAG, "❌ Cliente desconectado. Total: $clientCount")
    }

    private val binder = object : IRemoteService.Stub() {
        override fun sendCommand(command: String?) {
            command ?: return
            Log.d(TAG, "📥 Comando recibido: $command")

            // PING especial para health check
            if (command == "PING") {
                notifyAll("PONG")
                return
            }

            onCommandReceived(command)
        }

        override fun registerCallback(callback: IRemoteCallback?) {
            callback ?: return

            val existingWrapper = callbacks.find { it.callback.asBinder() == callback.asBinder() }
            if (existingWrapper != null) {
                Log.w(TAG, "⚠️ Callback ya registrado")
                return
            }

            val deathRecipient = IBinder.DeathRecipient {
                Log.d(TAG, "💀 Cliente murió")
                removeCallback(callback)
            }

            try {
                callback.asBinder().linkToDeath(deathRecipient, 0)
            } catch (e: RemoteException) {
                Log.e(TAG, "❌ Error linkToDeath", e)
                return
            }

            callbacks.add(CallbackWrapper(callback, deathRecipient))
            clientsConnected++

            onClientConnected(clientsConnected)
            notifyConnectionStatus("conectado")
        }

        override fun unregisterCallback(callback: IRemoteCallback?) {
            callback ?: return
            removeCallback(callback)
        }
    }

    private fun removeCallback(callback: IRemoteCallback) {
        val wrapper = callbacks.find { it.callback.asBinder() == callback.asBinder() }

        if (wrapper != null) {
            callbacks.remove(wrapper)
            clientsConnected--

            onClientDisconnected(clientsConnected)

            try {
                callback.asBinder().unlinkToDeath(wrapper.deathRecipient, 0)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error unlinkToDeath", e)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "🔗 onBind llamado")
        return binder
    }

    /**
     * Envía un mensaje a TODOS los clientes conectados
     */
    protected fun notifyAll(message: String) {
        Log.d(TAG, "📤 Notificando a ${callbacks.size} clientes: $message")

        val deadCallbacks = mutableListOf<IRemoteCallback>()
        callbacks.forEach { wrapper ->
            try {
                wrapper.callback.onMessage(message)
            } catch (e: RemoteException) {
                Log.e(TAG, "❌ Error notificando", e)
                deadCallbacks.add(wrapper.callback)
            }
        }
        deadCallbacks.forEach { removeCallback(it) }
    }

    private fun notifyConnectionStatus(status: String) {
        val deadCallbacks = mutableListOf<IRemoteCallback>()
        callbacks.forEach { wrapper ->
            try {
                wrapper.callback.onMessage("CONNECTION:$status")
            } catch (e: RemoteException) {
                Log.e(TAG, "❌ Error notificando conexión", e)
                deadCallbacks.add(wrapper.callback)
            }
        }
        deadCallbacks.forEach { removeCallback(it) }
    }

    /**
     * Retorna el número de clientes conectados actualmente
     */
    protected fun getConnectedClientsCount(): Int = clientsConnected

    override fun onDestroy() {
        super.onDestroy()

        callbacks.forEach { wrapper ->
            try {
                wrapper.callback.asBinder().unlinkToDeath(wrapper.deathRecipient, 0)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error limpiando", e)
            }
        }
        callbacks.clear()
        clientsConnected = 0

        Log.d(TAG, "🔴 Servicio destruido")
    }
}
