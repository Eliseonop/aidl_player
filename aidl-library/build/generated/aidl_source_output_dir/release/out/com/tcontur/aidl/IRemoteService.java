/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\edu\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\edu\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\build\generated\aidl_source_output_dir\release\out -IC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\main\aidl -IC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\release\aidl -IC:\Users\edu\.gradle\caches\8.13\transforms\10f5a5e68e8f8abfc6a4d040f0821b47\transformed\core-1.15.0\aidl -IC:\Users\edu\.gradle\caches\8.13\transforms\6339c731b1b5a4a5476393e79a127f65\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\edu\AppData\Local\Temp\aidl9375332977135901183.d C:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\main\aidl\com\tcontur\aidl\IRemoteService.aidl
 */
package com.tcontur.aidl;
public interface IRemoteService extends android.os.IInterface
{
  /** Default implementation for IRemoteService. */
  public static class Default implements com.tcontur.aidl.IRemoteService
  {
    @Override public void sendCommand(java.lang.String command) throws android.os.RemoteException
    {
    }
    @Override public void registerCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException
    {
    }
    @Override public void unregisterCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.tcontur.aidl.IRemoteService
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.tcontur.aidl.IRemoteService interface,
     * generating a proxy if needed.
     */
    public static com.tcontur.aidl.IRemoteService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.tcontur.aidl.IRemoteService))) {
        return ((com.tcontur.aidl.IRemoteService)iin);
      }
      return new com.tcontur.aidl.IRemoteService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_sendCommand:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.sendCommand(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerCallback:
        {
          com.tcontur.aidl.IRemoteCallback _arg0;
          _arg0 = com.tcontur.aidl.IRemoteCallback.Stub.asInterface(data.readStrongBinder());
          this.registerCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterCallback:
        {
          com.tcontur.aidl.IRemoteCallback _arg0;
          _arg0 = com.tcontur.aidl.IRemoteCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterCallback(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.tcontur.aidl.IRemoteService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void sendCommand(java.lang.String command) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(command);
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendCommand, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void registerCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_sendCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.tcontur.aidl.IRemoteService";
  public void sendCommand(java.lang.String command) throws android.os.RemoteException;
  public void registerCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException;
  public void unregisterCallback(com.tcontur.aidl.IRemoteCallback callback) throws android.os.RemoteException;
}
