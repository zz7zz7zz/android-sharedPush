package com.boyaa.push.lib.service;

import com.boyaa.push.lib.service.ISocketServiceCallback;

interface ISocketService {

    void registerCallback(inout Bundle mBundle,ISocketServiceCallback cb);
    
    void unregisterCallback(inout Bundle mBundle, ISocketServiceCallback cb);
    
    int request(inout Bundle mBundle);
}
