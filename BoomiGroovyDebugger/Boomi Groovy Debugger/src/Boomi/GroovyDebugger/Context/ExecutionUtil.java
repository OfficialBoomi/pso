package Boomi.GroovyDebugger.Context;

import java.util.Date;
import java.util.Properties;



public class ExecutionUtil  {
	
	private Properties _processProperties;
	private Properties _dynamicProcessProperties;
	
	public ExecutionUtil()
	{
		_processProperties = new Properties();
		_dynamicProcessProperties = new Properties();
		
		 _processProperties = new Properties();
		 _processProperties.put("lastrun", new Date().toString());
		 _processProperties.put("lastsuccessfulrun", new Date().toString());
			
	}
	
	public void setProcessProperty(String ComponentID, String PropertyKey, String PropertyValue)
	{
		_processProperties.put(PropertyKey, PropertyValue);
	}
	
	public  String getProcessProperty(String ComponentID, String PropertyKey)
	{
		String result = _processProperties.get(PropertyKey).toString();
		
		if(result == null)
		{
			return "";
		}
		else
		{
			return result;		
		}		
	}
	
	public String getDynamicProcessProperty(String PropertyName)
	{
		String result = _dynamicProcessProperties.get(PropertyName).toString();
		
		if(result == null)
		{
			return "";
		}
		else
		{
			return result;		
		}	
	}
	
	public  void setDynamicProcessProperty(String PropertyName, String PropertyValue, Boolean persist)
	{
		_dynamicProcessProperties.put(PropertyName, PropertyValue);
	}
	

	public  Logger getBaseLogger()
	{
		return new Logger();
	}
}

