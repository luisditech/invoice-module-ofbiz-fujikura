package com.fae.EDI.beans;

import java.sql.Timestamp;

public class Fichero {
	private String directory = "";
    private String name = null;
    private long size = 0;
    private String type = "";
    //private String timestamp = null;
    private Timestamp timestamp = null;
    private long   jdeTimestamp = 0;
    private String status = "";
    
	public Fichero() {

	}     
	
	public Fichero(String directory,String name,long size,String type,Timestamp timestamp) {
		this.directory = directory;
		this.name = name;
		this.size = size;
		this.type = type;
		this.timestamp = timestamp;
	}  	
	
	public Fichero(String directory,String name,long size,String type,Timestamp timestamp,String status) {
		this.directory = directory;
		this.name = name;
		this.size = size;
		this.type = type;
		this.timestamp = timestamp;
		this.status=status;
	} 	
	
	public Fichero(String directory,String name,long size) {
		this.directory = directory;
		this.name = name;
		this.size = size;
	}  		
		    
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getJdeTimestamp() {
		return jdeTimestamp;
	}

	public void setJdeTimestamp(long jdeTimestamp) {
		this.jdeTimestamp = jdeTimestamp;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
	@Override
	public String toString() { 
	    return "[ \""+ directory + "\"," +
	    	   " \""+ name + "\"," +
	    	   " \""+ size + "\"," +
	    	   " \""+ type + "\"," +
	    	   " \""+ timestamp + "\"," +	 
	    	   " \""+ status + "\"" +
	    		" ]";
	}
    
}
