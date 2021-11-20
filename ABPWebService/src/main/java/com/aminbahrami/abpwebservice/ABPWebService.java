package com.aminbahrami.abpwebservice;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okio.BufferedSink;

/**
 * Created by ABP on 8/28/2016.
 */
public class ABPWebService
{
	private int connectTimeout=3000;
	private int readTimeout=10000;
	
	private static final Handler HANDLER=new Handler();
	private IOnNetwork iOnNetwork=null;
	private String url="";
	
	private boolean isSsl=false;
	
	private ArrayList<HttpHeader> headers=new ArrayList<>();
	
	public void setConnectTimeout(int timeout)
	{
		this.connectTimeout=timeout;
	}
	
	public void setReadTimeout(int readTimeout)
	{
		this.readTimeout=readTimeout;
	}
	
	public ABPWebService setUrl(String url)
	{
		this.url=url;
		
		isSsl=url.toLowerCase().startsWith("https");
		
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
					Log.i("Webservice","inputName: "+inputName);
					
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
				
				
				
				try
				{
					
					OkHttpClient client=getNewClient();
					
					
					Request.Builder builder=new Request.Builder()
							.url(url)
							.post(requestBody);
					
					
					for(HttpHeader header : getHeaders())
					{
						builder.addHeader(header.getName(),header.getValue());
					}
					
					Request request=builder.build();
					
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
							String body=response.body().string();
							
							response.close();
							
							Log.i("WebService","Response: "+body);
							
							if(iOnNetwork!=null)
							{
								iOnNetwork.onResponse(body,response.code());
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
	
	private OkHttpClient getNewClient()
	{
		OkHttpClient.Builder builder=new OkHttpClient.Builder()
				.connectTimeout(connectTimeout,TimeUnit.MILLISECONDS)
				.readTimeout(readTimeout,TimeUnit.MILLISECONDS)
				.writeTimeout(readTimeout,TimeUnit.MILLISECONDS)
				.followRedirects(true)
				.followSslRedirects(true)
				.retryOnConnectionFailure(true)
				.cache(null);
		
		
		if(isSsl && (Build.VERSION.SDK_INT>=17 && Build.VERSION.SDK_INT<22))
		{
			try
			{
				ConnectionSpec spec=new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
						.supportsTlsExtensions(true)
						.tlsVersions(TlsVersion.TLS_1_2,TlsVersion.TLS_1_1,TlsVersion.TLS_1_0)
						.cipherSuites(
								CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
								CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
								CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
								CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
								CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
								CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
								CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
								CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
								CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
								CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
								CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
								CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
						.build();
				
				builder.connectionSpecs(Collections.singletonList(spec));
			}
			catch(Exception exc)
			{
				Log.e("OkHttpTLSCompat","Error while setting TLS 1.2",exc);
			}
		}
		
		
		return builder.build();
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
	
	public ArrayList<HttpHeader> getHeaders()
	{
		return headers;
	}
	
	public ABPWebService setHeaders(ArrayList<HttpHeader> headers)
	{
		this.headers=headers;
		
		return this;
	}
}
