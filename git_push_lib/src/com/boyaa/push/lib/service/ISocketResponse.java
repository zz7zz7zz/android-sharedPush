package com.boyaa.push.lib.service;


/**
 * Socket数据回调接口
 * @author DexYang
 *
 */
public interface ISocketResponse 
{
	public static final int STATUS_SOCKET_CLOSE=-1;

	public abstract void onSocketResponse(int code,Object retObject);
}
