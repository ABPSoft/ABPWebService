package com.aminbahrami.abpwebservicelib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.aminbahrami.abpwebservice.ABPWebService;

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
		abpWebService.setUrl("http://192.168.1.2/test/androidUploadFile/upload.php");
		
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
		
		abpWebService.sendRequest("data",object.toString(),"file",file);
	}
}
