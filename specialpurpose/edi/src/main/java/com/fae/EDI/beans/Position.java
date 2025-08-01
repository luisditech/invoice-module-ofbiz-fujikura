package com.fae.EDI.beans;

public class Position {
	private String type = null;
    private String name = null;
    private int initialPos = 0;
    private int finalPos = 0;
    private String required = null;
    private String dataType = null;
    
	public Position() {
		
	}    
    
    public Position(String type,String name,int initialPos,int finalPos, String required, String dataType){
    	this.type = type;
        this.name = name;
        this.initialPos = initialPos;
        this.finalPos = finalPos;
        this.required = required;
        this.dataType = dataType;
    }
                
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInitialPos() {
		return initialPos;
	}

	public void setInitialPos(int initialPos) {
		this.initialPos = initialPos;
	}

	public int getFinalPos() {
		return finalPos;
	}

	public void setFinalPos(int finalPos) {
		this.finalPos = finalPos;
	}

	@Override
	public String toString() {
		return "Positions [name=" + name + " \ninitialPos=" + initialPos + " \nfinalPos=" + finalPos + "]";
	}
    
    
    
}
