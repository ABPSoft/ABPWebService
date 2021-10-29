package com.aminbahrami.abpwebservice;

/**
 * Created by ABP on 10/29/2021 - 11:06 PM
 */

class HttpHeader
{
	private String name="";
	
	private String value="";
	
	public String getName()
	{
		return name;
	}
	
	public HttpHeader setName(String name)
	{
		this.name=name;
		
		return this;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public HttpHeader setValue(String value)
	{
		this.value=value;
		
		return this;
	}
}
