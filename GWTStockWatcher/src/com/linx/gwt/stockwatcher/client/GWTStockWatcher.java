package com.linx.gwt.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.linx.gwt.stockwatcher.client.constants.GWTStockWatcherConstants;
import com.linx.gwt.stockwatcher.client.constants.GWTStockWatcherMessages;
import com.linx.gwt.stockwatcher.client.exception.DelistedException;
import com.linx.gwt.stockwatcher.client.model.StockPrice;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GWTStockWatcher implements EntryPoint {

	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private TextBox symbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private Label appTitleLabel = new Label();

	private ArrayList<String> stocks = new ArrayList<String>();
	private StockPriceServiceAsync stockPriceSvc = GWT.create(StockPriceService.class);
	private Label errorMsgLabel = new Label();
	
	private GWTStockWatcherConstants constants = GWT.create(GWTStockWatcherConstants.class);
	private GWTStockWatcherMessages messages = GWT.create(GWTStockWatcherMessages.class);
	

	private static final int REFRESH_INTERVAL = 5000; // ms
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	@SuppressWarnings("unused")
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	@Override
	public void onModuleLoad() {
		/*
		 * Create table for stock data.
		 */
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");

		/*
		 * Assemble Add Stock panel.
		 */
		addPanel.add(symbolTextBox);
		addPanel.add(addStockButton);
		addPanel.addStyleName("addPanel");

		/*
		 * Assemble Main panel.
		 */
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);
		
		mainPanel.add(errorMsgLabel);
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		/*
		 * Associate the Main panel with the HTML host page using the RootPanel.
		 */
		RootPanel.get("stockList").add(mainPanel);
		
		/*
		 * Associate the Title of the Application with the HTML host page using the RootPanel
		 */
		//RootPanel.get("appTitle").add(appTitleLabel.setText(constants.stockWatcher()));

		/*
		 * Move cursor focus to the input box.
		 */
		symbolTextBox.setFocus(true);

		/*
		 * Add styles to elements in the stock list table
		 */
		stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
		stocksFlexTable.addStyleName("watchList");
		stocksFlexTable.getCellFormatter().addStyleName(0, 1,
				"watchListNumericColumn");
		stocksFlexTable.getCellFormatter().addStyleName(0, 2,
				"watchListNumericColumn");
		stocksFlexTable.getCellFormatter().addStyleName(0, 3,
				"watchListRemoveColumn");

		/*
		 * Listen for mouse events on the Add button
		 */
		addStockButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addStock();
			}

		});

		/*
		 * Listen for keyboard events on the input box
		 */
		symbolTextBox.addKeyDownHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}

			}
		});

		Timer refreshTimer = new Timer() {

			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

	}

	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton
	 * or presses enter in the newSymbolTextBox.
	 */
	private void addStock() {
		final String symbol = symbolTextBox.getText().toUpperCase().trim();
		symbolTextBox.setFocus(true);

		/*
		 * Stock code must be between 1 and 10 chars that are numbers, letters,
		 * or dots.
		 */
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' not a valid symbol");
			symbolTextBox.selectAll();
			return;
		}

		symbolTextBox.setText("");

		/*
		 * Don't add the stock if it's already in the table.
		 */
		if (stocks.contains(symbol))
			return;

		/*
		 * Add the stock to the table.
		 */
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		stocksFlexTable.setWidget(row, 2, new Label());
		stocksFlexTable.getCellFormatter().addStyleName(row, 1,
				"watchListNumericColumn");
		stocksFlexTable.getCellFormatter().addStyleName(row, 2,
				"watchListNumericColumn");
		stocksFlexTable.getCellFormatter().addStyleName(row, 3,
				"watchListRemoveColumn");

		/*
		 * Add a button to remove this stock from the table.
		 */
		Button removeStockButton = new Button("x");
		removeStockButton.addStyleDependentName("remove");
		removeStockButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				int removedIndex = stocks.indexOf(symbol);
				stocks.remove(removedIndex);
				stocksFlexTable.removeRow(removedIndex + 1);

			}
		});
		stocksFlexTable.setWidget(row, 3, removeStockButton);

		/*
		 * Get the stock price.
		 */
		refreshWatchList();

	}

	private void refreshWatchList() {

		/*
		 * Initialize the service proxy
		 */
		if(stockPriceSvc == null){
			stockPriceSvc = GWT.create(StockPriceService.class);
		}
		
		/*
		 * Set up the callback object
		 */
		AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {

			@Override
			public void onFailure(Throwable caught) {
				/*
				 * If the stock code is in the list of delisted codes, display an error message.
				 */
				String details = caught.getMessage();
				if(caught instanceof DelistedException){
					details = "Company '" + ((DelistedException)caught).getSymbol() + "' was delisted";
				}
				
				errorMsgLabel.setText("Error: " + details);
				errorMsgLabel.setVisible(true);
				
			}

			@Override
			public void onSuccess(StockPrice[] result) {
				updateTable(result);
				
			}
		};
		
		/*
		 * Make the call to the stock price service
		 */
		stockPriceSvc.getPrices(stocks.toArray(new String [0]), callback);

	}

	/**
	 * Update the Price and Change fields for all the rows in the stock table.
	 * 
	 * @param prices
	 *            Stock data for all rows.
	 */
	private void updateTable(StockPrice[] prices) {
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}

		/*
		 * Display timestamp showing last refresh
		 */
		lastUpdatedLabel.setText("Last Update : "
				+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM)
						.format(new Date()));
		
		/*
		 * Clear any errors
		 */
		errorMsgLabel.setVisible(false);
	}

	/**
	 * Update a single row in the stock table.
	 * 
	 * @param price
	 *            Stock data for a single row.
	 */
	private void updateTable(StockPrice price) {
		/*
		 * Make sure the stock is still in the stock table.
		 */
		if (!stocks.contains(price.getSymbol())) {
			return;
		}

		int row = stocks.indexOf(price.getSymbol()) + 1;

		/*
		 * Format the data in the Price and Change fields.
		 */
		NumberFormat priceFormat = NumberFormat.getFormat("#,##0.00");
		String priceText = priceFormat.format(price.getPrice());

		NumberFormat changeFormat = NumberFormat
				.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(price.getChange());
		String changePercentText = changeFormat
				.format(price.getChangePercent());

		/*
		 * Populate the Price and Change fields with new data.
		 */
		stocksFlexTable.setText(row, 1, priceText);
		Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
		changeWidget.setText(changeText + "(" + changePercentText + "%" + " )");
		
		/*
		 * Change the color of text in the Change field based on its value.
		 */
		String changeStyleName = "noChange";
		if(price.getChangePercent() < -0.1f){
			changeStyleName = "negativeChange";
		}
		else if(price.getChangePercent() > 0.1f){
			changeStyleName = "positiveChange";
		}
		changeWidget.setStyleName(changeStyleName);
	}
}
