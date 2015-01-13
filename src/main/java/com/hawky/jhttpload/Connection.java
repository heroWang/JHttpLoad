package com.hawky.jhttpload;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
	public int connState;
	public boolean didConnected;
	public boolean didResponse;
	public URLInfo urlInfo;
	public SocketChannel channel;
	public ByteBuffer buffer=ByteBuffer.allocate(3000);
	public long startAt;
	public long connectAt;
	public long requestAt;
	public long responseAt;
	public long endAt;
	public Task timeoutTask;
	public int totalBytes=0;
}
