package com.ds.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class VanRequest {
	
	private String baseUrl;
	
	public VanRequest(String ip, int port) {
		baseUrl = "http://" + ip + ":" + port;
	}
	
	public String execute(String method, String cmd) throws Exception {
		
		String url = baseUrl + method + "?cmd=" + cmd;

		RequestThread thread = new RequestThread(url);
		
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		String result = thread.getResponse();
		
		return result;
	}
	
	public class RequestThread extends Thread {
		
		private String url;
		private String ret;
		
		public RequestThread(String url) {
			this.url  = url;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
			try {
				ret = doRequest();
			} catch (Exception e) {
			}
		}
		
		private String doRequest() throws Exception {
			URL url = new URL(this.url);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setConnectTimeout(1000);
			conn.setReadTimeout(3000);
			conn.setRequestMethod("GET");

			int statusCode = conn.getResponseCode();

			if (200 != statusCode) {
				System.err.println("ErrorCode: " + statusCode);
				return null;
			}

			InputStream is = conn.getInputStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			int len = 0;
			byte buffer[] = new byte[1024];

			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}

			is.close();
			os.close();

			String result = new String(os.toByteArray());
			return result;
		}
		
		public String getResponse() {
			return ret;
		}
	}
}
