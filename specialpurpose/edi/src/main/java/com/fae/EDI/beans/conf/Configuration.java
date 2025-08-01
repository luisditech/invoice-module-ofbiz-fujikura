package com.fae.EDI.beans.conf;

public class Configuration {
	private String sCfgName;
	private String sCfgValue; 
	private String sCfgObs;
	
	public Configuration(String sCfgName,String sCfgValue,String sCfgObs) {
		this.sCfgName  = sCfgName;
		this.sCfgValue = sCfgValue;
		this.sCfgObs   = sCfgObs;
	}
	
	public Configuration() {
		
	}

	public String getsCfgName() {
		return sCfgName;
	}

	public void setsCfgName(String sCfgName) {
		this.sCfgName = sCfgName;
	}

	public String getsCfgValue() {
		return sCfgValue;
	}

	public void setsCfgValue(String sCfgValue) {
		this.sCfgValue = sCfgValue;
	}

	public String getsCfgObs() {
		return sCfgObs;
	}

	public void setsCfgObs(String sCfgObs) {
		this.sCfgObs = sCfgObs;
	}
	
	
}
