package com.aminbahrami.abpwebservice;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ABP on 8/28/2016.
 */
public class ABPWebService
{
	private int connectTimeout=3000;
	private int readTimeout=10000;
	
	public void setConnectTimeout(int timeout)
	{
		this.connectTimeout=timeout;
	}
	
	public void setReadTimeout(int readTimeout)
	{
		this.readTimeout=readTimeout;
	}
	
	private static final Handler HANDLER=new Handler();
	private IOnNetwork iOnNetwork=null;
	private String url="";
	
	public ABPWebService setUrl(String url)
	{
		this.url=url;
		
		return this;
	}
	
	public void setOnNetwork(IOnNetwork iOnNetwork)
	{
		this.iOnNetwork=iOnNetwork;
	}
	
	public void sendRequest(final String inputName,final String message)
	{
		Log.i("ABPWebService","Request: "+message);
		
		Thread thread=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//String requestData=inputName+"="+message;
				String requestData=message;
				
				URL apiUrl=null;
				
				try
				{
					apiUrl=new URL(url);
					
					HttpURLConnection httpURLConnection=(HttpURLConnection) apiUrl.openConnection();
					
					httpURLConnection.setConnectTimeout(connectTimeout);
					httpURLConnection.setReadTimeout(readTimeout);
					
					httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
					
					httpURLConnection.setRequestMethod("POST");
					httpURLConnection.setDoInput(true);
					httpURLConnection.setDoOutput(true);
					
					OutputStreamWriter osw=new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8");
					osw.write(requestData);
					osw.flush();
					
					BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
					
					StringBuilder strAll=new StringBuilder();
					String strLine;
					while((strLine=br.readLine())!=null)
					{
						strAll.append(strLine).append("\n");
					}
					
					strAll=new StringBuilder(strAll.toString().trim());
					
					Log.i("ABPWebService","Response: "+strAll);
					
					iOnNetwork.onResponse(strAll.toString());
				}
				catch(MalformedURLException e)
				{
					e.printStackTrace();
					
					if(iOnNetwork!=null)
					{
						HANDLER.post(new Runnable()
						{
							@Override
							public void run()
							{
								iOnNetwork.onError(-5,"Error in Assign URL");
							}
						});
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
					
					if(iOnNetwork!=null)
					{
						HANDLER.post(new Runnable()
						{
							@Override
							public void run()
							{
								iOnNetwork.onError(-6,"عدم توانایی در اتصال به سرور!");
							}
						});
					}
				}
			}
		});
		
		thread.start();
	}
}
