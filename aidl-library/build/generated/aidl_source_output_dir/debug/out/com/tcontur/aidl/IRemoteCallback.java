/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\edu\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\edu\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\build\generated\aidl_source_output_dir\debug\out -IC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\main\aidl -IC:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\debug\aidl -IC:\Users\edu\.gradle\caches\8.13\transforms\10f5a5e68e8f8abfc6a4d040f0821b47\transformed\core-1.15.0\aidl -IC:\Users\edu\.gradle\caches\8.13\transforms\6339c731b1b5a4a5476393e79a127f65\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\edu\AppData\Local\Temp\aidl9108166210638434396.d C:\Users\edu\AndroidStudioProjects\demo_aidl\aidl-library\src\main\aidl\com\tcontur\aidl\IRemoteCallback.aidl
 */
package com.tcontur.aidl;
// Declare any non-default types here with import statements
public interface IRemoteCallback extends android.os.IInterface
{
  /** Default implementation for IRemoteCallback. */
  public static class Default implements com.tcontur.aidl.IRemoteCallback
  {
    @Override public void onMessage(java.lang.String message) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.tcontur.aidl.IRemoteCallback
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.tcontur.aidl.IRemoteCallback interface,
     * generating a proxy if needed.
     */
    public static com.tcontur.aidl.IRemoteCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.tcontur.aidl.IRemoteCallback))) {
        return ((com.tcontur.aidl.IRemoteCallback)iin);
      }
      return new com.tcontur.aidl.IRemoteCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onMessage:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onMessage(_arg0);
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
    private static class Proxy implements com.tcontur.aidl.IRemoteCallback
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
      @Override public void onMessage(java.lang.String message) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(message);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onMessage, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.tcontur.aidl.IRemoteCallback";
  public void onMessage(java.lang.String message) throws android.os.RemoteException;
}
