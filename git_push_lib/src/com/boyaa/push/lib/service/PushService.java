
package com.boyaa.push.lib.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.boyaa.push.lib.util.LogUtil;
import com.boyaa.push.lib.util.NetworkUtil;

/**
 * 后台服务类
 * @author DexYang
 *
 */
public class PushService extends Service {

	private Client mClient=null;

	@Override
	public void onCreate() {
		super.onCreate();
LogUtil.v("PushService", "onCreate()");
		android.os.Debug.waitForDebugger();
		
		mClient=new Client(getApplicationContext());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	private void handleCommand(Intent intent)
	{
		if(null==intent)
		{
			LogUtil.v("PushService","handleCommand() pid:"+(android.os.Process.myPid())+" tid:"+(android.os.Process.myTid()));
			return;
		}
		
		if(NetworkUtil.isNetworkAvailable(getApplicationContext()))
		{
			mClient.open(mSocketResponseListener);
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
LogUtil.v("PushService", "onBind()");
		if(PushService.class.getName().equals(arg0.getAction()))
		{
			handleCommand(arg0);
			return mBinder;
		}
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
LogUtil.v("PushService", "onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
LogUtil.v("PushService", "onDestroy()");
		mClient.close();
		mCallbacks.kill();
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	/**
	 * socket数据回调
	 */
	private ISocketResponse mSocketResponseListener=new ISocketResponse() {
		

		@Override
		public void onSocketResponse(int code ,Object retObject) {
			
    		Bundle mBundle=new Bundle();
    		mBundle.putByteArray("data", (byte[])retObject);
			final int callbacksNum = mCallbacks.beginBroadcast();
	        for (int i=callbacksNum-1; i>=0; i--) 
	        {
	            try {
   					mCallbacks.getBroadcastItem(i).response(mBundle);;
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
	        mCallbacks.finishBroadcast();
		}
	};
	
	private final CusRemoteCallbackList<ISocketServiceCallback> mCallbacks= new CusRemoteCallbackList<ISocketServiceCallback>();
	private ISocketService.Stub mBinder=new ISocketService.Stub() {

		@Override
		public int request(Bundle mBundle) throws RemoteException {
			
			byte[] data=mBundle.getByteArray("data");
			if(null!=data)
			{
				return mClient.send(new ClientPacket(data));
			}
			return 0;
		}

		@Override
		public void registerCallback(Bundle mBundle, ISocketServiceCallback cb)
				throws RemoteException {
			 if (cb != null)
			 {
				 boolean isRegistered = mCallbacks.register(cb);
LogUtil.v("PushService","registerCallback isRegistered"+isRegistered);
			 } 
		}

		@Override
		public void unregisterCallback(Bundle mBundle, ISocketServiceCallback cb)
				throws RemoteException {
			if (cb != null) 
			{
				boolean isUnregistered=mCallbacks.unregister(cb);
LogUtil.v("PushService","registerCallback isUnregistered"+isUnregistered);
			}
		}

	};
	
	/**
	 * 经过测试onCallbackDied()方法，只有在bindService()，没有调用unbind()方法process就挂了的情况下才会执行
	 * @author Administrator
	 *
	 * @param <E>
	 */
	private class CusRemoteCallbackList<E extends IInterface> extends RemoteCallbackList<E>
	{
		@Override
		public void onCallbackDied(E callback) {
LogUtil.v("PushService", "CusRemoteCallbackList onCallbackDied 1");
			super.onCallbackDied(callback);

		}

		@Override
		public void onCallbackDied(E callback, Object cookie) {
LogUtil.v("PushService", "CusRemoteCallbackList onCallbackDied 2");
			super.onCallbackDied(callback, cookie);
		}
	}
}
