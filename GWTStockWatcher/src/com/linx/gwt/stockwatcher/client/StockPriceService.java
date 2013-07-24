package com.linx.gwt.stockwatcher.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.linx.gwt.stockwatcher.client.exception.DelistedException;
import com.linx.gwt.stockwatcher.client.model.StockPrice;

@RemoteServiceRelativePath("stockPrices")
public interface StockPriceService extends RemoteService {
	
	StockPrice [] getPrices(String [] symbols) throws DelistedException;

}
