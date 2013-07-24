package com.linx.gwt.stockwatcher.client.exception;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DelistedException extends Exception implements Serializable {
	
	private String symbol;
	
	public DelistedException(){
		
	}
	
	public DelistedException(String symbol){
		this.symbol = symbol;
	}
		
	public String getSymbol() {
		return symbol;
	}
}
