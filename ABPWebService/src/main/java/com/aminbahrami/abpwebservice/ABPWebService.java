package com.aminbahrami.abpwebservice;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
	
	public void sendRequest(String data)
	{
		sendRequest(null,data);
	}
	
	public void sendRequest(String inputName,String data)
	{
		sendRequest(inputName,data,null,null);
	}
	
	public void sendRequest(final String inputName,final String data,final String fileInputName,final File file)
	{
		Log.i("WebService","Request: "+data);
		
		Thread thread=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String requestData;
				
				if(inputName==null)
				{
					requestData=data;
				}
				else
				{
					requestData=inputName+"="+data;
				}
				
				
				URL apiUrl=null;
				
				try
				{
					apiUrl=new URL(url);
					
					HttpURLConnection httpURLConnection=(HttpURLConnection) apiUrl.openConnection();
					
					httpURLConnection.setConnectTimeout(connectTimeout);
					httpURLConnection.setReadTimeout(readTimeout);
					
					if(inputName==null)
					{
						httpURLConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
					}
					
					httpURLConnection.setRequestMethod("POST");
					httpURLConnection.setDoInput(true);
					httpURLConnection.setDoOutput(true);
					
					DataOutputStream dataOutputStream=null;
					
					if(file!=null)
					{
						String boundary="***************";
						
						String lineEnd="\r\n";
						String twoHyphens="--";
						
						FileInputStream fileInputStream=new FileInputStream(file);
						httpURLConnection.setUseCaches(false);
						httpURLConnection.setRequestProperty("ENCTYPE","multipart/form-data");
						httpURLConnection.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
						
						
						String content="Content-Disposition: form-data; name=\""+inputName+"\""+lineEnd+lineEnd+data+lineEnd+lineEnd;
						content+=twoHyphens+boundary+lineEnd+"Content-Disposition: form-data; name=\""+fileInputName+"\";filename=\""+file.getName()+"\""+lineEnd+"Content-Type:"+ getMimeType(file.getAbsolutePath())+lineEnd;
						
						dataOutputStream=new DataOutputStream(httpURLConnection.getOutputStream());
						dataOutputStream.writeBytes(twoHyphens+boundary+lineEnd);
						dataOutputStream.writeBytes(content);
						dataOutputStream.writeBytes(lineEnd);
						
						int byteAvailable, bufferSize, bytesRead;
						byte[] buffer;
						int maxBufferSize=1024*1024;
						
						byteAvailable=fileInputStream.available();
						bufferSize=Math.min(byteAvailable,maxBufferSize);
						
						buffer=new byte[bufferSize];
						bytesRead=fileInputStream.read(buffer,0,bufferSize);
						
						while(bytesRead>0)
						{
							dataOutputStream.write(buffer,0,bufferSize);
							byteAvailable=fileInputStream.available();
							bufferSize=Math.min(byteAvailable,maxBufferSize);
							
							bytesRead=fileInputStream.read(buffer,0,bufferSize);
						}
						
						dataOutputStream.writeBytes(lineEnd);
						dataOutputStream.writeBytes(twoHyphens+boundary+twoHyphens+lineEnd);
					}
					else
					{
						OutputStreamWriter osw=new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8");
						osw.write(requestData);
						osw.flush();
					}
					
					if(dataOutputStream!=null)
					{
						dataOutputStream.close();
						dataOutputStream.flush();
					}
					
					BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
					
					StringBuilder strAll=new StringBuilder();
					String strLine;
					while((strLine=br.readLine())!=null)
					{
						strAll.append(strLine).append("\n");
					}
					
					strAll=new StringBuilder(strAll.toString().trim());
					
					Log.i("WebService","Response: "+strAll);
					
					if(iOnNetwork!=null)
					{
						iOnNetwork.onResponse(strAll.toString());
					}
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
	
	private static String getMimeType(String url)
	{
		String type="";
		
		String extension=MimeTypeMap.getFileExtensionFromUrl(url);
		if(extension!=null)
		{
			type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
}
