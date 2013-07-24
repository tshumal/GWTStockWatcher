package com.linx.gwt.stockwatcher.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.linx.gwt.stockwatcher.client.model.StockPrice;

public interface StockPriceServiceAsync {
	
	void getPrices(String [] symbols, AsyncCallback<StockPrice[]> callback);

}
