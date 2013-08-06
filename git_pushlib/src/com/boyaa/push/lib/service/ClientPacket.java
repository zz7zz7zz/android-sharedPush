package com.boyaa.push.lib.service;

import com.boyaa.push.lib.util.AtomicIntegerUtil;

public class ClientPacket {
	
	private int packId=AtomicIntegerUtil.getIncrementID();//包唯一ID
	public byte[] data;
	
	public ClientPacket(byte[] data)
	{
		this.data=data;
	}
	
	public byte[] getData() {
		return data;
	}

	public int getPackId() {
		return packId;
	}
}
