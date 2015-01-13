package com.hawky.jhttpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.joda.time.DateTimeConstants;

import com.hawky.jhttpload.utils.Constants;
import com.hawky.jhttpload.utils.Utils;

public class BenchMark {
	private List<Connection> connections;
	private Statistic stsc;
	private int timeoutSecs = Constants.DEFAULT_TIMEOUT_SECONDS;// connection
																// timeout
																// seconds.
	private int start_mode = Constants.START_MODE_RATE;
	private int end_mode = Constants.END_MODE_SECONDS;

	private int rate = Constants.DEFAULT_FETCH_RATE;
	private int parallelNum = Constants.DEFAULT_PARALLEL_NUMBER;

	private int seconds = Constants.DEFAULT_SECONDS;
	private int fetchNum = Constants.DEFAULT_FETCH_NUMBER;

	private TimerTaskMgr timerTaskMgr = new TimerTaskMgr();
	private List<URLInfo> urlInfos;
	private Selector selector = null;

	//private SimpleTimer timer=new SimpleTimer();//for test
	
	public BenchMark() {
		this.stsc = new Statistic();
		this.connections = new ArrayList<Connection>(Constants.MAX_CONNECTIONS);
	}

	private void init() {
		for (int i = 0; i < Constants.MAX_CONNECTIONS; i++) {
			Connection connection = new Connection();
			connection.connState = Constants.CNST_FREE;
			connections.add(connection);
		}

	}

	public void run() {
		init();

		// timer.begin();
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// timer.end();
		// System.out.println("selector open elapsed(millis): " +
		// timer.elapsed());

		// parallel mode,prepare enough urlInfo.
		if (this.start_mode == Constants.START_MODE_PARALLEL) {
			List<URLInfo> tempURLInfos = urlInfos;
			urlInfos = new ArrayList<URLInfo>(tempURLInfos.size() * this.parallelNum);
			for (URLInfo urlInfo : tempURLInfos) {
				int pnum = 0;
				while (pnum < this.parallelNum) {
					urlInfos.add(urlInfo.clone());
					pnum++;
				}
			}
		}

		// rate mode,start timer task for every url.
		long now = getMillisOfDay();
		stsc.setStartAt(now);
		if (this.start_mode == Constants.START_MODE_RATE) {
			for (URLInfo urlInfo : urlInfos) {
				long interval = 1000L / rate;
				timerTaskMgr.submitTask(now, new TimerTaskCallback() {
					public void run(Object associatedData, long now) {
						callbackStartConnect(associatedData, now);
					}
				}, urlInfo, interval, true);
			}
		}

		if (this.end_mode == Constants.END_MODE_SECONDS) {
			timerTaskMgr.submitTask(now, new TimerTaskCallback() {
				public void run(Object associatedData, long now) {
					finish(now);
				}

			}, null, 1000L * this.seconds, false);
		}
		// Main loop
		while (true) {
			if (this.end_mode == Constants.END_MODE_FETCHES && this.fetchNum <= stsc.getFetchCompleted()) {
				finish(now);
			}
			
			if(this.start_mode == Constants.START_MODE_PARALLEL){
				//if any URLInfo is not loaded,load it.
				for(URLInfo urlInfo : urlInfos){
					if(!urlInfo.isLoaded()){
						startConnect(urlInfo, now);
						
						now = getMillisOfDay();
						timerTaskMgr.runTimer(now);
					}
				}
			}

			try {
				
			//	timer.begin();
				for (int i = 0; i < Constants.MAX_CONNECTIONS; i++) {
					Connection conn = connections.get(i);

					switch (conn.connState) {
					case Constants.CNST_CONNECTING:
						if (conn.channel.isRegistered()) {
							conn.channel.keyFor(selector).interestOps(SelectionKey.OP_CONNECT);
						} else {
							conn.channel.register(selector, SelectionKey.OP_CONNECT, conn);
						}

						break;
					case Constants.CNST_READING:
						conn.channel.keyFor(selector).interestOps(SelectionKey.OP_READ);
						break;
					default:
						break;
					}
				}
			} catch (ClosedChannelException e) {
				e.printStackTrace();
			}
//			timer.end();
//			System.out.println("connection event regist elapsed millis:"+timer.elapsed());

			try {
				// Return instantly.
				int readyCount = selector.selectNow();
				// System.out.println(String.format("key size:%d,selected keySize:%d ",selector.keys().size(),selector.selectedKeys().size()));

				now = getMillisOfDay();
				/* Service the connections whose I/O is ready. */
				if (readyCount > 0) {
					Iterator<SelectionKey> keyItr = selector.selectedKeys().iterator();
					while (keyItr.hasNext()) {
						SelectionKey key = keyItr.next();
						Connection conn = (Connection) key.attachment();
						if (conn.connState == Constants.CNST_CONNECTING && key.isConnectable()) {
							handleConnect(conn, now);
						} else if (conn.connState == Constants.CNST_READING && key.isReadable()) {
							handleRead(conn, now);
						}

						keyItr.remove();
					}
				}

				timerTaskMgr.runTimer(now);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void startConnect(URLInfo urlInfo, long now) {
		int cnum;
		for (cnum = 0; cnum < Constants.MAX_CONNECTIONS; cnum++) {
			Connection conn = connections.get(cnum);
			if (conn.connState == Constants.CNST_FREE) {
				startConnect(urlInfo, conn, now);
				return;
			}
		}
		System.err.println("ran out of connection slots.");
		finish(now);
	}

	public void startConnect(URLInfo urlInfo, Connection conn, long now) {
		if (conn.connState != Constants.CNST_FREE) {
			return;
		}
		stsc.incrFetchStarted();
		urlInfo.setLoaded(true);
		startSocket(conn, urlInfo, now);
	}

	public void startSocket(final Connection conn, URLInfo urlInfo, long now) {
		conn.urlInfo = urlInfo;
		conn.didConnected = false;
		conn.didResponse = false;
		conn.startAt = now;
		conn.channel = null;
		conn.totalBytes = 0;
		conn.timeoutTask = timerTaskMgr.submitTask(now, new TimerTaskCallback() {
			public void run(Object associatedData, long now) {
				callbackTimeoutConnection(associatedData, now);
			}
		}, conn, timeoutSecs * 1000L, false);

		SocketChannel channel = null;
		try {
			// create socket.and set non-blocking mode
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().setTcpNoDelay(true);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Connect.
		conn.channel = channel;
		conn.connectAt = now;
		
		stsc.incrCurrConnectionNum();
		stsc.refreshMaxParallelNum();
		
		String host = conn.urlInfo.getHost();
		int port = conn.urlInfo.getPort();
		try {
			if (!channel.connect(new InetSocketAddress(host, port))) {
				conn.connState = Constants.CNST_CONNECTING;
				return;
			}
		} catch (Exception e) {
			closeConnection(conn);
			e.printStackTrace();
			return;
		}

		/* Connect succeeded instantly, so handle it now. */
		now = getMillisOfDay();
		handleConnect(conn, now);
	}

	public void handleConnect(final Connection conn, long now) {
		SocketChannel channel = conn.channel;
		try {
			/*
			 * Finishes the process of connecting a socket channel in
			 * non-blocking mode.
			 */
			if (!channel.finishConnect()) {
				System.err.println("channel connect failed[host:" + conn.urlInfo.getHost() + ",port:" + conn.urlInfo.getPort() + "]");
				closeConnection(conn);
				return;
			}
		} catch (Exception e) {
			closeConnection(conn);
			e.printStackTrace();
			return;
		}

		conn.requestAt = now;
		conn.didConnected = true;
		// Format request. 
		StringBuilder reqBuilder = new StringBuilder();
		reqBuilder.append(String.format("GET %s HTTP/1.0\r\n",conn.urlInfo.getFile()));
		reqBuilder.append(String.format("Host: %s \r\n",conn.urlInfo.getHost()));
		reqBuilder.append("User-Agent: bechmark 1.0\r\n");
		reqBuilder.append("\r\n");

		//System.out.println(reqBuilder.toString());
		ByteBuffer byteBuffer = conn.buffer.put(reqBuilder.toString().getBytes());
		byteBuffer.flip();
		try {

			// timer.begin();
			//int bytecount = 0;
			while (byteBuffer.hasRemaining()) {
			  channel.write(byteBuffer);
			}
			// timer.end();

			// System.out.println("channel write "+bytecount+" bytes, elapsed(millis): "
			// + timer.elapsed());

			byteBuffer.clear();
			conn.connState = Constants.CNST_READING;
		} catch (Exception e) {
			closeConnection(conn);
			e.printStackTrace();
			return;
		}
	}

	private void handleRead(Connection conn, long now) {
		SocketChannel channel = conn.channel;
		timerTaskMgr.resetTask(now, conn.timeoutTask);
		try {
			int read = channel.read(conn.buffer);
			// System.out.println("read byte length:" + read);
			if (read < 0) {
				conn.endAt = now;
				closeConnection(conn);
				return;
			}
			conn.totalBytes += read;
			
//			 conn.buffer.flip();
//			 byte[] bytes = new byte[read];
//			 conn.buffer.get(bytes);
//			 System.out.println(new String( bytes));
			conn.buffer.clear();

			if (!conn.didResponse) {// First response.
				conn.didResponse = true;
				conn.responseAt = now;
			}
		} catch (IOException e) {
			// System.err.println("remote connection closed,read total bytes:" +
			// conn.totalBytes);
			closeConnection(conn);
			e.printStackTrace();
			return;
		}

	}

	private void callbackStartConnect(Object associatedData, long now) {
		URLInfo urlInfo = (URLInfo) associatedData;
		startConnect(urlInfo, now);
	}

	public void callbackTimeoutConnection(Object associatedData, long now) {
		Connection conn = (Connection) associatedData;
		conn.timeoutTask = null;
		System.err.println(String.format("[%s:%d] : timed out", conn.urlInfo.getHost(), conn.urlInfo.getPort()));
		closeConnection(conn);

		stsc.incrTotalTimouts();
	}

	private void closeConnection(final Connection conn) {
		SocketChannel channel = conn.channel;
		if (channel.isRegistered()) {
			channel.keyFor(selector).cancel();
		}
		if (conn.didConnected) {
			// Double check.
			if (channel.isConnected()) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		conn.connState = Constants.CNST_FREE;
		conn.buffer.clear();
		conn.urlInfo.setLoaded(false);
		if (conn.timeoutTask != null) {
			timerTaskMgr.cancelTask(conn.timeoutTask);
		}
		
		stsc.incrTotalBytes(conn.totalBytes);
		stsc.incrFetchCompleted();
		stsc.decrCurrConnectionNum();
		
		if (conn.didConnected) {
			long connectUseMillis = conn.requestAt - conn.connectAt;
			stsc.incrTotalConnectMillis(connectUseMillis);
			stsc.refreshMaxConnectMillis(connectUseMillis);
			stsc.refreshMinConnectMillis(connectUseMillis);
			stsc.incrConnectCompleted();
		}
		if(conn.didResponse){
			long responseUseMillis = conn.responseAt - conn.requestAt;
			stsc.incrTotalResponseMillis(responseUseMillis);
			stsc.refreshMaxResponseMillis(responseUseMillis);
			stsc.refreshMinResponseMillis(responseUseMillis);
			stsc.incrResponseCompleted();
		}
	}

	public void parseOptions(String[] args) {
		Options ops =this.createOptions();
		try {
			CommandLine cmd = new BasicParser().parse(ops, args);
			if (cmd.hasOption("h")) {
				usage(ops);
				System.exit(0);
			}

			if (cmd.hasOption("r")) {
				this.start_mode = Constants.START_MODE_RATE;
				this.rate = Integer.parseInt(cmd.getOptionValue("r"));
			} else if (cmd.hasOption("p")) {
				this.start_mode = Constants.START_MODE_PARALLEL;
				this.parallelNum = Integer.parseInt(cmd.getOptionValue("p"));
			} else {
				throw new RuntimeException("either -rate or -parallel is required.");
			}

			if (cmd.hasOption("s")) {
				this.end_mode = Constants.END_MODE_SECONDS;
				this.seconds = Integer.parseInt(cmd.getOptionValue("s"));
			} else if (cmd.hasOption("f")) {
				this.end_mode = Constants.END_MODE_FETCHES;
				this.fetchNum = Integer.parseInt(cmd.getOptionValue("f"));
			} else {
				throw new RuntimeException("either -seconds or -fetches is required.");
			}

			if (cmd.hasOption("timeout")) {
				this.timeoutSecs = Integer.parseInt(cmd.getOptionValue("timeout"));
			}

			String urlFilePath = Utils.getAbsolutePath(args[args.length - 1]);
			File urlFile = new File(urlFilePath);
			readUrl(urlFile);

		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			usage(ops);
			System.exit(1);
		}
	}

	public Options createOptions() {
		Options ops = new Options();

		ops.addOption("r", "rate", true, "rate mode,the program will test every url at specific rate,like 2 fetches per second.\r\n either -rate or -parallel is required.");
		ops.addOption("p", "parallel", true, "parallel mode,the program will test every url with specific number of connections.\r\n either -rate or -parallel is required.");
		ops.addOption("s", "seconds", true, "test last time(seconds).either -seconds or -fetches is required.");
		ops.addOption("f", "fetches", true, "fetch times for every url.either -seconds or -fetches is required.");
		ops.addOption("timeout", true, "request timout(seconds).not required,default 10 seconds.");
		ops.addOption("h", "help", false, "show usage.");

		return ops;
	}

	private void usage(Options ops) {
		System.out.println("usage: jhttpload -parallel N | -rate N \r\n -fetches N | -seconds N \r\n [-timeout seconds] url_file");
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", ops);
	}

	private void readUrl(File urlFile) {
		if (!urlFile.isFile()) {
			throw new RuntimeException("url file not exits");
		}
		urlInfos = new ArrayList<URLInfo>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(urlFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				URLInfo urlInfo = new URLInfo(line);
				urlInfos.add(urlInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void finish(long now) {
		System.out.print(stsc.report(now));
		System.exit(0);
	}

	private long getMillisOfDay() {
		return System.currentTimeMillis() % DateTimeConstants.MILLIS_PER_DAY;

	}

}
