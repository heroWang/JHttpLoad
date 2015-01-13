package com.hawky.jhttpload;

public class Statistic {
	private int currConnectionNum;
	private int maxParallelNum;

	private long startAt;

	private int fetchStarted;
	private int connectCompleted;
	private int responseCompleted;
	private int fetchCompleted;

	private long totalBytes;

	private long totalConnectMillis;
	private long maxConnectMillis;
	private long minConnectMillis = Long.MAX_VALUE;

	private long totalResponseMillis;
	private long maxResponseMillis;
	private long minResponseMillis = Long.MAX_VALUE;

	private int totalTimouts;

	public String report(long now) {
		float elapsed = (now - startAt) / 1000f;
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d fetches, %d max parallel, %d bytes, in %g seconds%n", this.fetchCompleted, this.maxParallelNum, this.totalBytes,elapsed ));
		builder.append(String.format("%g  mean bytes/connection%n",(float)this.totalBytes / (float)this.fetchCompleted));
		builder.append(String.format("%g fetches/sec, %g bytes/sec%n", (float)this.fetchCompleted / elapsed,(float)this.totalBytes / elapsed));
		builder.append(String.format("msecs/connect: %g mean, %d max, %d min%n",(float)this.totalConnectMillis /this.connectCompleted,this.maxConnectMillis,this.minConnectMillis));
		builder.append(String.format("msecs/first-response: %g mean, %d max, %d min%n",(float)this.totalResponseMillis/this.responseCompleted,this.maxResponseMillis,this.minResponseMillis));
		builder.append(String.format("%d timeouts\n", this.totalTimouts));
		
		return builder.toString();
	}

	public void setStartAt(long startAt) {
		this.startAt = startAt;
	}

	public int incrCurrConnectionNum() {
		return this.currConnectionNum++;
	}

	public int decrCurrConnectionNum() {
		return this.currConnectionNum--;
	}

	public void refreshMaxParallelNum() {
		if (this.currConnectionNum > this.maxParallelNum) {
			this.maxParallelNum = this.currConnectionNum;
		}
	}

	public int incrFetchStarted() {
		return this.fetchStarted++;
	}

	public int incrConnectCompleted() {
		return this.connectCompleted++;
	}

	public int incrResponseCompleted() {
		return this.responseCompleted++;
	}

	public int incrFetchCompleted() {
		return this.fetchCompleted++;
	}

	public long incrTotalBytes(long incr) {
		this.totalBytes += incr;
		return this.totalBytes;
	}

	public long incrTotalConnectMillis(long incr) {
		this.totalConnectMillis += incr;
		return this.totalConnectMillis;
	}

	public long incrTotalResponseMillis(long incr) {
		this.totalResponseMillis += incr;
		return this.totalResponseMillis;
	}

	public long incrTotalTimouts() {
		return this.totalTimouts++;
	}

	public int getFetchStarted() {
		return fetchStarted;
	}

	public int getConnectCompleted() {
		return connectCompleted;
	}

	public int getResponseCompleted() {
		return responseCompleted;
	}

	public int getFetchCompleted() {
		return fetchCompleted;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getTotalConnectMillis() {
		return totalConnectMillis;
	}

	public long getMaxConnectMillis() {
		return maxConnectMillis;
	}

	public void refreshMaxConnectMillis(long maxConnectMillis) {
		if (this.maxConnectMillis < maxConnectMillis) {
			this.maxConnectMillis = maxConnectMillis;
		}
	}

	public long getMinConnectMillis() {
		return minConnectMillis;
	}

	public void refreshMinConnectMillis(long minConnectMillis) {
		if (this.minConnectMillis > minConnectMillis) {
			this.minConnectMillis = minConnectMillis;
		}
	}

	public long getTotalResponseMillis() {
		return totalResponseMillis;
	}

	public long getMaxResponseMillis() {
		return maxResponseMillis;
	}

	public void refreshMaxResponseMillis(long maxResponseMillis) {
		if (this.maxResponseMillis < maxResponseMillis) {
			this.maxResponseMillis = maxResponseMillis;
		}
	}

	public long getMinResponseMillis() {
		return minResponseMillis;
	}

	public void refreshMinResponseMillis(long minResponseMillis) {
		if (this.minResponseMillis > minResponseMillis) {
			this.minResponseMillis = minResponseMillis;
		}
	}

	public int getTotalTimouts() {
		return totalTimouts;
	}

}
