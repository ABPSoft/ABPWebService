package com.aminbahrami.abpwebservicelib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aminbahrami.abpwebservice.ABPWebService;
import com.aminbahrami.abpwebservice.IOnNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ActivityMain extends Activity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.sendRequest).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendRequest();
			}
		});
	}
	
	private void sendRequest()
	{
		ABPWebService abpWebService=new ABPWebService();
		//abpWebService.setUrl("https://14bazikon.com/api/users/login");
		//abpWebService.setUrl("http://api.apiservice.info/");
		abpWebService.setUrl("https://api.apiservice.info/");
		//abpWebService.setUrl("https://api.github.com/repos/square/okhttp/issues");
		//abpWebService.setUrl("https://reqres.in/api/users");
		
		//Check Permission in the feature
		File file=new File(Environment.getExternalStorageDirectory()+"/test.jpg");
		
		Log.i("LOG","FilePath: "+file);
		
		JSONObject object=new JSONObject();
		try
		{
			object.put("test","Hello");
			object.put("age",28);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		abpWebService.setConnectTimeout(5000);
		abpWebService.setReadTimeout(5000);
		
		abpWebService.setOnNetwork(new IOnNetwork()
		{
			@Override
			public void onResponse(String response)
			{
				Log.i("LOG","Response: "+response);
			}
			
			@Override
			public void onError(int errorCode,String errorText,Exception e)
			{
				Log.i("LOG",errorText);
				
				e.printStackTrace();
			}
		});
		
		abpWebService.sendRequest("test",object.toString());
	}
}
