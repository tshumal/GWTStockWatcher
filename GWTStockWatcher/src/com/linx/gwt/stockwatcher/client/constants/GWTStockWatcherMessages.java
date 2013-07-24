package com.linx.gwt.stockwatcher.client.constants;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

public interface GWTStockWatcherMessages extends Messages{
	
	@DefaultMessage("''{0}'' is not a valid symbol")
	String invalidSymbol(String symbol);
	
	@DefaultMessage("Last update: {0, date, medium} {0, time, medium}")
	String lastUpdate(Date timestamp);

}
