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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

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
				RequestBody requestBody;
				
				if(fileInputName!=null)
				{
					String mimeType=getMimeType(file.getAbsolutePath());
					
					requestBody=new MultipartBody.Builder()
							.setType(MultipartBody.FORM)
							.addFormDataPart(inputName,data)
							.addFormDataPart(fileInputName,file.getName(),RequestBody.create(MediaType.parse(mimeType),file))
							.build();
				}
				else if(inputName!=null)
				{
					requestBody=new FormBody.Builder()
							.add(inputName,data)
							.build();
				}
				else
				{
					requestBody=new RequestBody()
					{
						@Override
						public MediaType contentType()
						{
							return MediaType.parse("application/json; charset=utf-8");
						}
						
						@Override
						public void writeTo(BufferedSink sink) throws IOException
						{
							sink.writeUtf8(data);
						}
					};
				}
				
				
				OkHttpClient client=new OkHttpClient.Builder()
						.connectTimeout(connectTimeout,TimeUnit.MILLISECONDS)
						.readTimeout(readTimeout,TimeUnit.MILLISECONDS)
						.writeTimeout(readTimeout,TimeUnit.MILLISECONDS)
						.build();
				
				Request request=new Request.Builder()
						.url(url)
						.post(requestBody)
						.build();
				
				try
				{
					client.newCall(request).enqueue(new Callback()
					{
						@Override
						public void onFailure(Call call,final IOException e)
						{
							if(iOnNetwork!=null)
							{
								HANDLER.post(new Runnable()
								{
									@Override
									public void run()
									{
										iOnNetwork.onError(-6,"عدم توانایی در اتصال به سرور!",e);
									}
								});
							}
						}
						
						@Override
						public void onResponse(Call call,final Response response) throws IOException
						{
							if(response.isSuccessful())
							{
								String body=response.body().string();
								
								Log.i("WebService","Response: "+body);
								
								if(iOnNetwork!=null)
								{
									iOnNetwork.onResponse(body);
								}
							}
							else
							{
								if(iOnNetwork!=null)
								{
									HANDLER.post(new Runnable()
									{
										@Override
										public void run()
										{
											iOnNetwork.onError(-6,"عدم توانایی در اتصال به سرور!",new Exception(response+""));
										}
									});
								}
							}
						}
					});
					
					
				}
				catch(final Exception e)
				{
					e.printStackTrace();
					
					if(iOnNetwork!=null)
					{
						HANDLER.post(new Runnable()
						{
							@Override
							public void run()
							{
								iOnNetwork.onError(-6,"عدم توانایی در اتصال به سرور!",e);
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
