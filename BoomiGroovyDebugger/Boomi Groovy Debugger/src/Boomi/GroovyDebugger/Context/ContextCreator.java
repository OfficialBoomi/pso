package Boomi.GroovyDebugger.Context;

import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ContextCreator {
	
private ArrayList<Properties> _incomingDocumentProperties;
private ArrayList<InputStream> _incomingStreams;
private ArrayList<InputStream> _resultStreams;
private ArrayList<Properties> _resultDocumentProperties;

//Default Constructor
	public ContextCreator(){
		
		_incomingDocumentProperties = new ArrayList<Properties>();
		_resultDocumentProperties = new ArrayList<Properties>();

		_incomingStreams = new ArrayList<InputStream>();
		_resultStreams = new ArrayList<InputStream>();
	}
	
	//Adds files to stream objects
	public void AddFiles(String path){
		try (Stream<Path> paths = Files.walk(Paths.get(path))) {
		    try {
				paths
				    .filter(Files::isRegularFile)
				    .forEach(file -> createStream(file.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		catch(Exception ex)
		{
			
		}	
	}
	
	//creates stream of files
	private void createStream(String path){
		
		try{
			InputStream is =  new FileInputStream(path);
			_incomingStreams.add(is);
		}
		catch(Exception ex)
		{
			
		}	
	}
	
	//returns the amount of streamobjects.
	public int getDataCount(){
		return _incomingStreams.size();
	}
	
	//Store stream and properties
	public void storeStream(InputStream stream, Properties props){
		_resultDocumentProperties.add(props);
		_resultStreams.add(stream);
	}
	
	//Get stream by it's index
	public InputStream getStream(int index){
		InputStream is = null;
		is =_incomingStreams.get(index);
		
		return is;
	}
	
	//Creates empty property objects
	public void createEmptyProperties(int numberOfProperties){
		for( int i = 0; i < numberOfProperties; i++ ) {
			Properties prop = new Properties();
			_incomingDocumentProperties.add(prop);
		}	
	}
	
	//Add property Key/Value pairs to the corresponding property
	public void addPropertyValues(int index, String key, String value){
		Properties prop = null;
		prop =  _incomingDocumentProperties.get(index);
		prop.put(key, value);
		
	}
	
	//Get properties by its index
	public Properties getProperties(int index){
		Properties prop = null;
		prop =  _incomingDocumentProperties.get(index);
		
		return prop;
	}	
}
