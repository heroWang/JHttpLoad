package com.hawky.jhttpload;

import java.net.MalformedURLException;
import java.net.URL;

public class URLInfo implements Cloneable{
	private URL url ;
	private boolean loaded;

	public URLInfo() {

	}
	
	@Override
	public URLInfo clone(){
		URLInfo urlInfo = new URLInfo();
		urlInfo.setURL(url);
		urlInfo.setLoaded(loaded);
		return urlInfo;
	}

	public URLInfo( String url) throws MalformedURLException {
		super();
		this.url=new URL(url);
	}

	public void setURL(URL url){
		this.url = url;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public String getHost() {
		return url.getHost();
	}
	public int getPort(){
		return url.getPort()<0?80:url.getPort();
	}
	public String getFile(){
		return url.getFile().isEmpty()?"/":url.getFile();
	}
	

}
