// IRemoteService.aidl
package com.tcontur.aidl;

// Declare any non-default types here with import statements
import com.tcontur.aidl.IRemoteCallback;

interface IRemoteService {
    void sendCommand(String command);
    void registerCallback(IRemoteCallback callback);
    void unregisterCallback(IRemoteCallback callback);
}
