package com.boyaa.push;
 

import java.util.Observable;
import java.util.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.boyaa.push.lib.service.ISocketService;
import com.boyaa.push.lib.service.ISocketServiceCallback;
import com.boyaa.push.lib.service.PushService;
import com.boyaa.push.lib.util.LogUtil;

/**
 * PushSDK 包装类，各个应用只要调用该类的相关方法，不需要调用其它类的方法
 * @author DexYang
 *
 */
public class PushSDK {
	
	private static PushSDK ins=null;
	private ISocketService mService=null;
	private PushObservable mPushObservable=new PushObservable();
	private Context mContext=null;
	
	private PushSDK()
	{
	}
	
	public static PushSDK getInstance()
	{
		if(null==ins)
		{
			ins=new PushSDK();
		}
		return ins;
	}
	
	/**
	 * 注册应用，注册后可以收到推送消息
	 * @param context 上下文
	 */
	public boolean registerApp(Context context)
	{
		Intent mIntent=new Intent(PushService.class.getName());
		context.startService(mIntent);
		return context.bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * 解注册应用，解注册后无法收到推送消息
	 * @param context 上下文
	 */
	public void unRegisterApp(Context context)
	{
		try {
			if(null!=mService)
			{
				mService.unregisterCallback(null,mCallBack);
				context.unbindService(conn);
				mService=null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mContext=null;
	}
	
	private ServiceConnection conn=new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
LogUtil.v("PushSDK", "onServiceDisconnected");
			try {
				mService.unregisterCallback(null,mCallBack);
			} catch (RemoteException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			mService=null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
LogUtil.v("PushSDK", "onServiceConnected");
			mService=ISocketService.Stub.asInterface(service);
			try {
				mService.registerCallback(null,mCallBack);
			} catch (RemoteException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private ISocketServiceCallback mCallBack=new ISocketServiceCallback.Stub() {
		
		@Override
		public void response(Bundle mBundle) throws RemoteException {
			
			try {
LogUtil.v("PushSDK","cmd:");
					byte []data=mBundle.getByteArray("data");
					if(null!=mPushObservable)
					{
						mPushObservable.notifyPush(new String(data));
					}
			} catch (Exception e) {
				e.printStackTrace();
				registerApp(mContext);
			}
		}
	};
	
	public  int chat(Context context, String msg)
	{
		try {
			if(null==mService)
			{
				registerApp(context);
			}

			if(null!=mService)
			{
				Bundle mBundle=new Bundle();
				mBundle.putByteArray("data",msg.getBytes());
				return mService.request(mBundle);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			registerApp(context);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * 注册网络回调
	 * @param observer 观察者对象
	 */
	public void registerPushListener(Observer observer)
	{
		if (observer == null) 
		{
			return;
		}
		mPushObservable.addObserver(observer);
	}
	
	/**
	 * 解注册网络回调
	 * @param observer 先前注册过的观察者对象
	 */
	public void unRegisterPushListener(Observer observer)
	{
		if (observer == null) 
		{
			return;
		}
		mPushObservable.deleteObserver(observer);
	}
	
	private class PushObservable extends Observable {
		
		public void notifyPush(Object message)
		{
			 setChanged();
			 notifyObservers(message);
		}
	}
}
