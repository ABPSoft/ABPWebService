package com.aminbahrami.abpwebservice;

/**
 * Created by ABP on 10/28/2018 - 11:22 PM
 */
public interface IOnNetwork
{
	public void onResponse(String response,int httpCode);
	
	public void onError(int errorCode,String errorText,Exception exception);
}
