package samples.testbed;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import samples.testbed.advisor.FAMethodSamples;
import samples.testbed.contracts.ContractSamples;
import samples.testbed.orders.AvailableAlgoParams;
import samples.testbed.orders.OrderSamples;
import samples.testbed.scanner.ScannerSubscriptionSamples;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.Types.FADataType;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Testbed {
	
	private static int startId=4001;
	private static String myemail = "astartradingltd@gmail.com";
	private static String mypass = "a*strading";
	
	private static String targets = "shlomidolev@gmail.com,binunalex@gmail.com";
	private static String header = "PREDICTION";
	
	
    private static EWrapperImpl wrapper = new EWrapperImpl();
	
	public static void sendemail(String filename) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		
		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(myemail,mypass);
					}
				});
		
		try {
			
			File file = new File(filename);
		    FileReader fileReader = new FileReader(file);
		    BufferedReader bufferedReader = new BufferedReader(fileReader);
		    String linelongs = bufferedReader.readLine();
		    String lineshorts = bufferedReader.readLine();
		    fileReader.close();

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myemail));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(targets));
			
			message.setSubject(header);
			message.setText("LONGS: " + linelongs + "; SHORTS: " + lineshorts);

			Transport.send(message);

			System.out.println("Done");

		} catch (Exception e) {
			System.out.println("Cannot send email " + e.getMessage());
		}
	}
    
	public static void main(String[] args) throws InterruptedException {
		
		/*if (args.length<1)
			return;
		
		File f = new File(args[0]);
		
		if (f.isFile()) {
			sendemail(args[0]);
			return;
		}
		
		if (f.isDirectory()==false)
			return;*/
			
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
	    String dirResults = "results";
		if (args.length>0)
			dirResults = args[1];
		wrapper.setResDir(dirResults);
		
		
		//! [connect]
		m_client.eConnect("127.0.0.1", 7496, 1);
		//! [connect]
		//! [ereader]
		final EReader reader = new EReader(m_client, m_signal);   
		
		reader.start();
		//An additional thread is created in this program design to empty the messaging queue
		new Thread(() -> {
		    while (m_client.isConnected()) {
		        m_signal.waitForSignal();
		        try {
		            reader.processMsgs();
		        } catch (Exception e) {
		            System.out.println("Exception: "+e.getMessage());
		        }
		    }
		}).start();
		//! [ereader]
		// A pause to give the application time to establish the connection
		// In a production application, it would be best to wait for callbacks to confirm the connection is complete
		Thread.sleep(1000);

		//tickByTickOperations(wrapper.getClient());
		//tickDataOperations(wrapper.getClient());
		//orderOperations(wrapper.getClient(), wrapper.getCurrentOrderId());
		//contractOperations(wrapper.getClient());
		//hedgeSample(wrapper.getClient(), wrapper.getCurrentOrderId());
		//testAlgoSamples(wrapper.getClient(), wrapper.getCurrentOrderId());
		//bracketSample(wrapper.getClient(), wrapper.getCurrentOrderId());
		//bulletins(wrapper.getClient());
		//reutersFundamentals(wrapper.getClient());
		//marketDataType(wrapper.getClient());
		historicalDataRequests(wrapper.getClient());
		//accountOperations(wrapper.getClient());
		//newsOperations(wrapper.getClient());
		//marketDepthOperations(wrapper.getClient());
		//rerouteCFDOperations(wrapper.getClient());
		//marketRuleOperations(wrapper.getClient());
		//tickDataOperations(wrapper.getClient());
		//pnlSingle(wrapper.getClient());
		//continuousFuturesOperations(wrapper.getClient());
		//pnlSingle(wrapper.getClient());
		//histogram(wrapper.getClient());

		Thread.sleep(100000);
		m_client.eDisconnect();
		
	}
	
	private static void histogram(EClientSocket client) {
	    client.reqHistogramData(4002, ContractSamples.USStock(), false, "3 days");
        
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        client.cancelHistogramData(4002);
	}
	
	private static void historicalTicks(EClientSocket client) {
		//! [reqhistoricalticks]
        //client.reqHistoricalTicks(18001, ContractSamples.USStockAtSmart(), "20170712 21:39:33", null, 10, "TRADES", 1, true, null);
        //client.reqHistoricalTicks(18002, ContractSamples.USStockAtSmart(), "20170712 21:39:33", null, 10, "BID_ASK", 1, true, null);
        //client.reqHistoricalTicks(18003, ContractSamples.USStockAtSmart(), "20170712 21:39:33", null, 10, "MIDPOINT", 1, true, null);
		//! [reqhistoricalticks]
	}

	private static void pnl(EClientSocket client) throws InterruptedException {
		//! [reqpnl]
        client.reqPnL(17001, "DUD00029", "");
		//! [reqpnl]
        Thread.sleep(1000);
		//! [cancelpnl]
        client.cancelPnL(17001);        
		//! [cancelpnl]
    }
	
    private static void pnlSingle(EClientSocket client) throws InterruptedException
    {
		//! [reqpnlsingle]
        client.reqPnLSingle(17001, "DUD00029", "", 268084);
		//! [reqpnlsingle]
        Thread.sleep(1000);
		//! [cancelpnlsingle]
        client.cancelPnLSingle(17001);
		//! [cancelpnlsingle]
    }

    private static void orderOperations(EClientSocket client, int nextOrderId) throws InterruptedException {
		
		/*** Requesting the next valid id ***/
		//! [reqids]
        //The parameter is always ignored.
        client.reqIds(-1);
        //! [reqids]
        //Thread.sleep(1000);
        /*** Requesting all open orders ***/
        //! [reqallopenorders]
        client.reqAllOpenOrders();
        //! [reqallopenorders]
        //Thread.sleep(1000);
        /*** Taking over orders to be submitted via TWS ***/
        //! [reqautoopenorders]
        client.reqAutoOpenOrders(true);
        //! [reqautoopenorders]
        //Thread.sleep(1000);
        /*** Requesting this API client's orders ***/
        //! [reqopenorders]
        client.reqOpenOrders();
        //! [reqopenorders]
        //Thread.sleep(1000);
		
        /*** Placing/modifying an order - remember to ALWAYS increment the nextValidId after placing an order so it can be used for the next one! ***/
        //! [order_submission]
        client.placeOrder(nextOrderId++, ContractSamples.USStock(), OrderSamples.LimitOrder("SELL", 1, 50));
        //! [order_submission]
        
        //! [faorderoneaccount]
        Order faOrderOneAccount = OrderSamples.MarketOrder("BUY", 100);
        // Specify the Account Number directly
        faOrderOneAccount.account("DU119915");
        client.placeOrder(nextOrderId++, ContractSamples.USStock(), faOrderOneAccount);
        //! [faorderoneaccount]
        
        //! [faordergroupequalquantity]
        Order faOrderGroupEQ = OrderSamples.LimitOrder("SELL", 200, 2000);
        faOrderGroupEQ.faGroup("Group_Equal_Quantity");
        faOrderGroupEQ.faMethod("EqualQuantity");
        client.placeOrder(nextOrderId++, ContractSamples.USStock(), faOrderGroupEQ);
        //! [faordergroupequalquantity]
        
        //! [faordergrouppctchange]
        Order faOrderGroupPC = OrderSamples.MarketOrder("BUY", 0);
        // You should not specify any order quantity for PctChange allocation method
        faOrderGroupPC.faGroup("Pct_Change");
        faOrderGroupPC.faMethod("PctChange");
        faOrderGroupPC.faPercentage("100");
        client.placeOrder(nextOrderId++, ContractSamples.EurGbpFx(), faOrderGroupPC);
        //! [faordergrouppctchange]
        
        //! [faorderprofile]
        Order faOrderProfile = OrderSamples.LimitOrder("BUY", 200, 100);
        faOrderProfile.faProfile("Percent_60_40");
		client.placeOrder(nextOrderId++, ContractSamples.EuropeanStock(), faOrderProfile);
        //! [faorderprofile]
        
		//! [modelorder]
        Order modelOrder = OrderSamples.LimitOrder("BUY", 200, 100);
		modelOrder.account("DF12345");  // master FA account number
		modelOrder.modelCode("Technology"); // model for tech stocks first created in TWS
		client.placeOrder(nextOrderId++, ContractSamples.USStock(), modelOrder);
        //! [modelorder]
		
		//client.placeOrder(nextOrderId++, ContractSamples.USStock(), OrderSamples.PeggedToMarket("BUY", 10, 0.01));
		//client.placeOrder(nextOrderId++, ContractSamples.EurGbpFx(), OrderSamples.MarketOrder("BUY", 10));
        //client.placeOrder(nextOrderId++, ContractSamples.USStock(), OrderSamples.Discretionary("SELL", 1, 45, 0.5));
		
        //! [reqexecutions]
        client.reqExecutions(10001, new ExecutionFilter());
        //! [reqexecutions]

		int cancelID = nextOrderId -1;
		//! [cancelorder]
		client.cancelOrder(cancelID);
		//! [cancelorder]

		//! [reqglobalcancel]
		client.reqGlobalCancel();
		//! [reqglobalcancel]

        Thread.sleep(10000);
        
    }
	
	private static void OcaSample(EClientSocket client, int nextOrderId) {
		
		//OCA order
		//! [ocasubmit]
		List<Order> OcaOrders = new ArrayList<>();
		OcaOrders.add(OrderSamples.LimitOrder("BUY", 1, 10));
		OcaOrders.add(OrderSamples.LimitOrder("BUY", 1, 11));
		OcaOrders.add(OrderSamples.LimitOrder("BUY", 1, 12));
		OcaOrders = OrderSamples.OneCancelsAll("TestOCA_" + nextOrderId, OcaOrders, 2);
		for (Order o : OcaOrders) {
			
			client.placeOrder(nextOrderId++, ContractSamples.USStock(), o);
		}
		//! [ocasubmit]
		
	}
	
	private static void tickDataOperations(EClientSocket client) throws InterruptedException {
		
		/*** Requesting real time market data ***/
		//Thread.sleep(1000);
		//! [reqmktdata]
		client.reqMktData(1001, ContractSamples.StockComboContract(), "", false, false, null);
		//! [reqmktdata]

		//! [reqsmartcomponents]
		client.reqSmartComponents(1013, "a6");
		//! [reqsmartcomponents]

		//! [reqmktdata_snapshot]
		client.reqMktData(1003, ContractSamples.FutureComboContract(), "", true, false, null);
		//! [reqmktdata_snapshot]

		/* 
		//! [regulatorysnapshot] 
		// Each regulatory snapshot request incurs a 0.01 USD fee
		client.reqMktData(1014, ContractSamples.USStock(), "", false, true, null);
		//! [regulatorysnapshot]
		*/
		
		//! [reqmktdata_genticks]
		//Requesting RTVolume (Time & Sales), shortable and Fundamental Ratios generic ticks
		client.reqMktData(1004, ContractSamples.USStock(), "233,236,258", false, false, null);
		//! [reqmktdata_genticks]
		//! [reqmktdata_contractnews]
		// Without the API news subscription this will generate an "invalid tick type" error
		client.reqMktData(1005, ContractSamples.USStock(), "mdoff,292:BZ", false, false, null);
		client.reqMktData(1006, ContractSamples.USStock(), "mdoff,292:BT", false, false, null);
		client.reqMktData(1007, ContractSamples.USStock(), "mdoff,292:FLY", false, false, null);
		client.reqMktData(1008, ContractSamples.USStock(), "mdoff,292:MT", false, false, null);
		//! [reqmktdata_contractnews]
		//! [reqmktdata_broadtapenews]
		client.reqMktData(1009, ContractSamples.BTbroadtapeNewsFeed(), "mdoff,292", false, false, null);
		client.reqMktData(1010, ContractSamples.BZbroadtapeNewsFeed(), "mdoff,292", false, false, null);
		client.reqMktData(1011, ContractSamples.FLYbroadtapeNewsFeed(), "mdoff,292", false, false, null);
		client.reqMktData(1012, ContractSamples.MTbroadtapeNewsFeed(), "mdoff,292", false, false, null);
		//! [reqmktdata_broadtapenews]
		//! [reqoptiondatagenticks]
        //Requesting data for an option contract will return the greek values
        client.reqMktData(1002, ContractSamples.OptionWithLocalSymbol(), "", false, false, null);
        //! [reqoptiondatagenticks]
		//! [reqfuturesopeninterest]
        //Requesting data for a futures contract will return the futures open interest
        client.reqMktData(1014, ContractSamples.SimpleFuture(), "mdoff,588", false, false, null);
		//! [reqfuturesopeninterest]

		//! [reqmktdata_preopenbidask]
        //Requesting data for a futures contract will return the pre-open bid/ask flag
        client.reqMktData(1015, ContractSamples.SimpleFuture(), "", false, false, null);
		//! [reqmktData_preopenbidask]
        
		Thread.sleep(10000);
		//! [cancelmktdata]
		client.cancelMktData(1001);
		client.cancelMktData(1002);
		client.cancelMktData(1003);
		client.cancelMktData(1014);
		client.cancelMktData(1015);
		//! [cancelmktdata]
		
	}
	
	private static void historicalDataRequests(EClientSocket client) throws InterruptedException {
		
		/*ArrayList<String> stickers = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("dowstickers.csv")));
			String line=null;
		    while((line = br.readLine()) != null)
		      stickers.add(line);  
		    
		    br.close();
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			stickers.add("AXP");
			stickers.add("BA");
			stickers.add("CAT");
			stickers.add("DD");
			stickers.add("DIS");
		}*/
		
		
		
		Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.MONTH, -6);
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String formatted = form.format(cal.getTime());
		
		//client.reqHistoricalData(startId, ContractSamples.USStockAtSmart("AXP"), formatted, "7 M", "1 day", "MIDPOINT", 1, 1, false, null);
		
		client.reqMktData(startId, ContractSamples.USStockAtSmart("AXP"), "100,221,233", false, false,null);

		Thread.sleep(5000);

		/*** Canceling news ticks ***/
		//! [cancelNewsTicks]
		client.cancelMktData(startId++);
		//client.reqHistoricalData(startId++, ContractSamples.EurGbpFx(), formatted, "2 M", "1 day", "MIDPOINT", 1, 1, false, null);
		Thread.sleep(1000);
	
		//Thread.sleep(2000);
		/*** Canceling historical data requests ***/
		client.cancelHistoricalData(startId);
        //client.cancelHistoricalData(4002);
		//! [reqhistoricaldata]
		return;
		//! [reqHistogramData]
		/*client.reqHistogramData(4004, ContractSamples.USStock(), false, "3 days");
        //! [reqHistogramData]
		Thread.sleep(5);
		
		//! [cancelHistogramData]
        client.cancelHistogramData(4004);*/
		//! [cancelHistogramData]
	}
	
	private static void realTimeBars(EClientSocket client) throws InterruptedException {
		
		/*** Requesting real time bars ***/
        //! [reqrealtimebars]
        client.reqRealTimeBars(startId, ContractSamples.USStockAtSmart("AXP"), 5, "MIDPOINT", true, null);
        //! [reqrealtimebars]
        Thread.sleep(2000);
        /*** Canceling real time bars ***/
        //! [cancelrealtimebars]
        client.cancelRealTimeBars(startId++);
        //! [cancelrealtimebars]
		
	}
	
	private static void marketDepthOperations(EClientSocket client) throws InterruptedException {
		
		/*** Requesting the Deep Book ***/

		//! [reqMktDepthExchanges]
		client.reqMktDepthExchanges();
		//! [reqMktDepthExchanges]

        //! [reqmarketdepth]
        client.reqMktDepth(2001, ContractSamples.EurGbpFx(), 5, null);
        //! [reqmarketdepth]
        Thread.sleep(2000);
        /*** Canceling the Deep Book request ***/
        //! [cancelmktdepth]
        client.cancelMktDepth(2001);
        //! [cancelmktdepth]
		
	}
	
	private static void accountOperations(EClientSocket client) throws InterruptedException {
		
        //client.reqAccountUpdatesMulti(9002, null, "EUstocks", true);
		//! [reqpositionsmulti]
        client.reqPositionsMulti(9003, "DU74649", "EUstocks");
      //! [reqpositionsmulti]
        Thread.sleep(10000);

        /*** Requesting managed accounts***/
        //! [reqmanagedaccts]
        client.reqManagedAccts();
        //! [reqmanagedaccts]

		/*** Requesting family codes***/
		//! [reqfamilycodes]
		client.reqFamilyCodes();
		//! [reqfamilycodes]

        /*** Requesting accounts' summary ***/
        Thread.sleep(2000);
        //! [reqaaccountsummary]
        client.reqAccountSummary(9001, "All", "AccountType,NetLiquidation,TotalCashValue,SettledCash,AccruedCash,BuyingPower,EquityWithLoanValue,PreviousEquityWithLoanValue,GrossPositionValue,ReqTEquity,ReqTMargin,SMA,InitMarginReq,MaintMarginReq,AvailableFunds,ExcessLiquidity,Cushion,FullInitMarginReq,FullMaintMarginReq,FullAvailableFunds,FullExcessLiquidity,LookAheadNextChange,LookAheadInitMarginReq ,LookAheadMaintMarginReq,LookAheadAvailableFunds,LookAheadExcessLiquidity,HighestSeverity,DayTradesRemaining,Leverage");
        //! [reqaaccountsummary]
        
      //! [reqaaccountsummaryledger]
        client.reqAccountSummary(9002, "All", "$LEDGER");
        //! [reqaaccountsummaryledger]
        Thread.sleep(2000);
        //! [reqaaccountsummaryledgercurrency]
        client.reqAccountSummary(9003, "All", "$LEDGER:EUR");
        //! [reqaaccountsummaryledgercurrency]
        Thread.sleep(2000);
        //! [reqaaccountsummaryledgerall]
        client.reqAccountSummary(9004, "All", "$LEDGER:ALL");
        //! [reqaaccountsummaryledgerall]
		
		//! [cancelaaccountsummary]
		client.cancelAccountSummary(9001);
		client.cancelAccountSummary(9002);
		client.cancelAccountSummary(9003);
		client.cancelAccountSummary(9004);
		//! [cancelaaccountsummary]
        
        /*** Subscribing to an account's information. Only one at a time! ***/
        Thread.sleep(2000);
        //! [reqaaccountupdates]
        client.reqAccountUpdates(true, "U150462");
        //! [reqaaccountupdates]
		Thread.sleep(2000);
		//! [cancelaaccountupdates]
		client.reqAccountUpdates(false, "U150462");
		//! [cancelaaccountupdates]
		
        //! [reqaaccountupdatesmulti]
        client.reqAccountUpdatesMulti(9002, "U150462", "EUstocks", true);
        //! [reqaaccountupdatesmulti]
        Thread.sleep(2000);
        /*** Requesting all accounts' positions. ***/
        //! [reqpositions]
        client.reqPositions();
        //! [reqpositions]
		Thread.sleep(2000);
		//! [cancelpositions]
		client.cancelPositions();
		//! [cancelpositions]
    }

	private static void newsOperations(EClientSocket client) throws InterruptedException {

		/*** Requesting news ticks ***/
		//! [reqNewsTicks]
		client.reqMktData(10001, ContractSamples.USStockAtSmart("AXP"), "mdoff,292", false, false, null);
		//! [reqNewsTicks]

		Thread.sleep(10000);

		/*** Canceling news ticks ***/
		//! [cancelNewsTicks]
		client.cancelMktData(10001);
		//! [cancelNewsTicks]

		Thread.sleep(2000);

		/*** Requesting news providers ***/
		//! [reqNewsProviders]
		client.reqNewsProviders();
		//! [reqNewsProviders]

		Thread.sleep(2000);
		
		/*** Requesting news article ***/
		//! [reqNewsArticle]
		client.reqNewsArticle(10002, "BZ", "BZ$04507322", null);
		//! [reqNewsArticle]

		Thread.sleep(5000);

		/*** Requesting historical news ***/
		//! [reqHistoricalNews]
		client.reqHistoricalNews(10003, 8314, "BZ+FLY", "", "", 10, null);
		//! [reqHistoricalNews]
	}

	private static void conditionSamples(EClientSocket client, int nextOrderId) {
		
		//! [order_conditioning_activate]
		Order mkt = OrderSamples.MarketOrder("BUY", 100);
		//Order will become active if conditioning criteria is met
		mkt.conditionsCancelOrder(true);
		mkt.conditions().add(OrderSamples.PriceCondition(208813720, "SMART", 600, false, false));
		mkt.conditions().add(OrderSamples.ExecutionCondition("EUR.USD", "CASH", "IDEALPRO", true));
		mkt.conditions().add(OrderSamples.MarginCondition(30, true, false));
		mkt.conditions().add(OrderSamples.PercentageChangeCondition(15.0, 208813720, "SMART", true, true));
		mkt.conditions().add(OrderSamples.TimeCondition("20160118 23:59:59", true, false));
		mkt.conditions().add(OrderSamples.VolumeCondition(208813720, "SMART", false, 100, true));
		client.placeOrder(nextOrderId++, ContractSamples.EuropeanStock(), mkt);
		//! [order_conditioning_activate]
		
		//Conditions can make the order active or cancel it. Only LMT orders can be conditionally canceled.
		//! [order_conditioning_cancel]
		Order lmt = OrderSamples.LimitOrder("BUY", 100, 20);
		//The active order will be cancelled if conditioning criteria is met
		lmt.conditionsCancelOrder(true);
		lmt.conditions().add(OrderSamples.PriceCondition(208813720, "SMART", 600, false, false));
		client.placeOrder(nextOrderId++, ContractSamples.EuropeanStock(), lmt);
		//! [order_conditioning_cancel]
		
	}
	
	private static void contractOperations(EClientSocket client) {
		
		//! [reqcontractdetails]
		client.reqContractDetails(210, ContractSamples.OptionForQuery());
		client.reqContractDetails(211, ContractSamples.EurGbpFx());
		client.reqContractDetails(212, ContractSamples.Bond());
		client.reqContractDetails(213, ContractSamples.FuturesOnOptions());
		//! [reqcontractdetails]

		//! [reqmatchingsymbols]
		client.reqMatchingSymbols(211, "IB");
		//! [reqmatchingsymbols]

	}
	
	private static void contractNewsFeed(EClientSocket client) {
		
		//! [reqcontractdetailsnews]
		client.reqContractDetails(211, ContractSamples.NewsFeedForQuery());
		//! [reqcontractdetailsnews]
		
	}
	
	private static void hedgeSample(EClientSocket client, int nextOrderId) {
		
		//F Hedge order
		//! [hedgesubmit]
		//Parent order on a contract which currency differs from your base currency
		Order parent = OrderSamples.LimitOrder("BUY", 100, 10);
		parent.orderId(nextOrderId++);
		//Hedge on the currency conversion
		Order hedge = OrderSamples.MarketFHedge(parent.orderId(), "BUY");
		//Place the parent first...
		client.placeOrder(parent.orderId(), ContractSamples.EuropeanStock(), parent);
		//Then the hedge order
		client.placeOrder(nextOrderId++, ContractSamples.EurGbpFx(), hedge);
		//! [hedgesubmit]
		
	}
	
	private static void testAlgoSamples(EClientSocket client, int nextOrderId) throws InterruptedException {
		
		//! [algo_base_order]
		Order baseOrder = OrderSamples.LimitOrder("BUY", 1000, 1);
		//! [algo_base_order]
		
		//! [arrivalpx]
		AvailableAlgoParams.FillArrivalPriceParams(baseOrder, 0.1, "Aggressive", "09:00:00 CET", "16:00:00 CET", true, true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [arrivalpx]
		
		Thread.sleep(500);
		
		//! [darkice]
		AvailableAlgoParams.FillDarkIceParams(baseOrder, 10, "09:00:00 CET", "16:00:00 CET", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [darkice]
		
		Thread.sleep(500);
		
		//! [ad]
		// The Time Zone in "startTime" and "endTime" attributes is ignored and always defaulted to GMT
		AvailableAlgoParams.FillAccumulateDistributeParams(baseOrder, 10, 60, true, true, 1, true, true, "20161010-12:00:00 GMT", "20161010-16:00:00 GMT");
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [ad]
		
		Thread.sleep(500);
		
		//! [twap]
		AvailableAlgoParams.FillTwapParams(baseOrder, "Marketable", "09:00:00 CET", "16:00:00 CET", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [twap]
		
		Thread.sleep(500);
		
		//! [vwap]
		AvailableAlgoParams.FillVwapParams(baseOrder, 0.2, "09:00:00 CET", "16:00:00 CET", true, true, true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [vwap]
		
		Thread.sleep(500);
		
		//! [balanceimpactrisk]
		AvailableAlgoParams.FillBalanceImpactRiskParams(baseOrder, 0.1, "Aggressive", true);
		client.placeOrder(nextOrderId++, ContractSamples.USOptionContract(), baseOrder);
		//! [balanceimpactrisk]
		
		Thread.sleep(500);
		
		//! [minimpact]
		AvailableAlgoParams.FillMinImpactParams(baseOrder, 0.3);
		client.placeOrder(nextOrderId++, ContractSamples.USOptionContract(), baseOrder);
		//! [minimpact]
		
		//! [adaptive]
		AvailableAlgoParams.FillAdaptiveParams(baseOrder, "Normal");
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [adaptive]		
		
		//! [closepx]
		AvailableAlgoParams.FillClosePriceParams(baseOrder, 0.5, "Neutral", "12:00:00 EST", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [closepx]
                
		//! [pctvol]
		AvailableAlgoParams.FillPctVolParams(baseOrder, 0.5, "12:00:00 EST", "14:00:00 EST", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [pctvol]               
                
		//! [pctvolpx]
		AvailableAlgoParams.FillPriceVariantPctVolParams(baseOrder, 0.1, 0.05, 0.01, 0.2, "12:00:00 EST", "14:00:00 EST", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [pctvolpx]
                
		//! [pctvolsz]
		AvailableAlgoParams.FillSizeVariantPctVolParams(baseOrder, 0.2, 0.4, "12:00:00 EST", "14:00:00 EST", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [pctvolsz]
                
		//! [pctvoltm]
		AvailableAlgoParams.FillTimeVariantPctVolParams(baseOrder, 0.2, 0.4, "12:00:00 EST", "14:00:00 EST", true, 100000);
		client.placeOrder(nextOrderId++, ContractSamples.USStockAtSmart("AXP"), baseOrder);
		//! [pctvoltm]
                
		//! [jeff_vwap_algo]
		AvailableAlgoParams.FillJefferiesVWAPParams(baseOrder, "10:00:00 EST", "16:00:00 EST", 10, 10, "Exclude_Both", 130, 135, 1, 10, "Patience", false, "Midpoint");
		client.placeOrder(nextOrderId++, ContractSamples.JefferiesContract(), baseOrder);
		//! [jeff_vwap_algo]

		//! [csfb_inline_algo]
		AvailableAlgoParams.FillCSFBInlineParams(baseOrder, "10:00:00 EST", "16:00:00 EST", "Patient", 10, 20, 100, "Default", false, 40, 100, 100, 35 );
		client.placeOrder(nextOrderId++, ContractSamples.CSFBContract(), baseOrder);
		//! [csfb_inline_algo]

	}
	
	private static void bracketSample(EClientSocket client, int nextOrderId) {
		
		//BRACKET ORDER
        //! [bracketsubmit]
		List<Order> bracket = OrderSamples.BracketOrder(nextOrderId++, "BUY", 100, 30, 40, 20);
		for(Order o : bracket) {
			client.placeOrder(o.orderId(), ContractSamples.EuropeanStock(), o);
		}
		//! [bracketsubmit]
		
	}
	
	private static void bulletins(EClientSocket client) throws InterruptedException {
		
		//! [reqnewsbulletins]
		client.reqNewsBulletins(true);
		//! [reqnewsbulletins]
		
		Thread.sleep(2000);
		
		//! [cancelnewsbulletins]
		client.cancelNewsBulletins();
		//! [cancelnewsbulletins]
		
	}
	
	private static void reutersFundamentals(EClientSocket client) throws InterruptedException {
		
		//! [reqfundamentaldata]
		client.reqFundamentalData(8001, ContractSamples.USStock(), "ReportsFinSummary");
		//! [reqfundamentaldata]
		
		Thread.sleep(2000);
		
		//! [fundamentalexamples]
		client.reqFundamentalData(8002, ContractSamples.USStock(), "ReportSnapshot"); //for company overview
		client.reqFundamentalData(8003, ContractSamples.USStock(), "ReportRatios"); //for financial ratios
		client.reqFundamentalData(8004, ContractSamples.USStock(), "ReportsFinStatements"); //for financial statements
		client.reqFundamentalData(8005, ContractSamples.USStock(), "RESC"); //for analyst estimates
		client.reqFundamentalData(8006, ContractSamples.USStock(), "CalendarReport"); //for company calendar
		//! [fundamentalexamples]
		
		//! [cancelfundamentaldata]
		client.cancelFundamentalData(8001);
		//! [cancelfundamentaldata]
		
	}
	
	private static void marketScanners(EClientSocket client) throws InterruptedException {
		
		/*** Requesting all available parameters which can be used to build a scanner request ***/
        //! [reqscannerparameters]
        client.reqScannerParameters();
        //! [reqscannerparameters]
        Thread.sleep(2000);

        /*** Triggering a scanner subscription ***/
        //! [reqscannersubscription]
        client.reqScannerSubscription(7001, ScannerSubscriptionSamples.HighOptVolumePCRatioUSIndexes(), null);
        //! [reqscannersubscription]

        Thread.sleep(2000);
        /*** Canceling the scanner subscription ***/
        //! [cancelscannersubscription]
        client.cancelScannerSubscription(7001);
        //! [cancelscannersubscription]
		
	}
	
	private static void financialAdvisorOperations(EClientSocket client) {
		
		/*** Requesting FA information ***/
		//! [requestfaaliases]
		client.requestFA(FADataType.ALIASES.ordinal());
		//! [requestfaaliases]
		
		//! [requestfagroups]
		client.requestFA(FADataType.GROUPS.ordinal());
		//! [requestfagroups]
		
		//! [requestfaprofiles]
		client.requestFA(FADataType.PROFILES.ordinal());
		//! [requestfaprofiles]
		
		/*** Replacing FA information - Fill in with the appropriate XML string. ***/
		//! [replacefaonegroup]
		client.replaceFA(FADataType.GROUPS.ordinal(), FAMethodSamples.FA_ONE_GROUP);
		//! [replacefaonegroup]
		
		//! [replacefatwogroups]
		client.replaceFA(FADataType.GROUPS.ordinal(), FAMethodSamples.FA_TWO_GROUPS);
		//! [replacefatwogroups]
		
		//! [replacefaoneprofile]
		client.replaceFA(FADataType.PROFILES.ordinal(), FAMethodSamples.FA_ONE_PROFILE);
		//! [replacefaoneprofile]
		
		//! [replacefatwoprofiles]
		client.replaceFA(FADataType.PROFILES.ordinal(), FAMethodSamples.FA_TWO_PROFILES);
		//! [replacefatwoprofiles]
		
                //! [reqSoftDollarTiers]
                client.reqSoftDollarTiers(4001);
                //! [reqSoftDollarTiers]
	}
	
	private static void testDisplayGroups(EClientSocket client) throws InterruptedException {
		
		//! [querydisplaygroups]
		client.queryDisplayGroups(9001);
		//! [querydisplaygroups]
		
		Thread.sleep(500);
		
		//! [subscribetogroupevents]
		client.subscribeToGroupEvents(9002, 1);
		//! [subscribetogroupevents]
		
		Thread.sleep(500);
		
		//! [updatedisplaygroup]
		client.updateDisplayGroup(9002, "8314@SMART");
		//! [updatedisplaygroup]
		
		Thread.sleep(500);
		
		//! [subscribefromgroupevents]
		client.unsubscribeFromGroupEvents(9002);
		//! [subscribefromgroupevents]
		
	}
	
	private static void marketDataType(EClientSocket client) {
		
		//! [reqmarketdatatype]
        /*** Switch to live (1) frozen (2) delayed (3) or delayed frozen (4)***/
        client.reqMarketDataType(2);
        //! [reqmarketdatatype]
		
	}
	
	private static void optionsOperations(EClientSocket client) {
		
		//! [reqsecdefoptparams]
		client.reqSecDefOptParams(0, "IBM", "", "STK", 8314);
		//! [reqsecdefoptparams]
		
		//! [calculateimpliedvolatility]
		client.calculateImpliedVolatility(5001, ContractSamples.OptionAtBOX(), 5, 85);
		//! [calculateimpliedvolatility]
		
		//** Canceling implied volatility ***
		client.cancelCalculateImpliedVolatility(5001);
		
		//! [calculateoptionprice]
		client.calculateOptionPrice(5002, ContractSamples.OptionAtBOX(), 0.22, 85);
		//! [calculateoptionprice]
		
		//** Canceling option's price calculation ***
		client.cancelCalculateOptionPrice(5002);
		
		//! [exercise_options]
		//** Exercising options ***
		client.exerciseOptions(5003, ContractSamples.OptionWithTradingClass(), 1, 1, "", 1);
		//! [exercise_options]
	}

	private static void rerouteCFDOperations(EClientSocket client) throws InterruptedException {

		//! [reqmktdatacfd]
		client.reqMktData(16001, ContractSamples.USStockCFD(), "", false, false, null);
		Thread.sleep(1000);
		client.reqMktData(16002, ContractSamples.EuropeanStockCFD(), "", false, false, null);
		Thread.sleep(1000);
		client.reqMktData(16003, ContractSamples.CashCFD(), "", false, false, null);
		Thread.sleep(1000);
		//! [reqmktdatacfd]

		//! [reqmktdepthcfd]
		client.reqMktDepth(16004, ContractSamples.USStockCFD(), 10, null);
		Thread.sleep(1000);
		client.reqMktDepth(16005, ContractSamples.EuropeanStockCFD(), 10, null);
		Thread.sleep(1000);
		client.reqMktDepth(16006, ContractSamples.CashCFD(), 10, null);
		Thread.sleep(1000);
		//! [reqmktdepthcfd]
	}

	private static void marketRuleOperations(EClientSocket client) throws InterruptedException {
		client.reqContractDetails(17001, ContractSamples.USStock());
		client.reqContractDetails(17002, ContractSamples.Bond());

		Thread.sleep(2000);
		
		//! [reqmarketrule]
		client.reqMarketRule(26);
		client.reqMarketRule(240);
		//! [reqmarketrule]
	}

	private static void continuousFuturesOperations(EClientSocket client) throws InterruptedException {

		/*** Requesting continuous futures contract details ***/
		client.reqContractDetails(18001, ContractSamples.ContFut());

		/*** Requesting historical data for continuous futures ***/
		//! [reqhistoricaldatacontfut]
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String formatted = form.format(cal.getTime());
		client.reqHistoricalData(18002, ContractSamples.ContFut(), formatted, "1 Y", "1 month", "TRADES", 0, 1, false, null);
		Thread.sleep(10000);
		/*** Canceling historical data request for continuous futures ***/
		client.cancelHistoricalData(18002);
		//! [reqhistoricaldatacontfut]
	}
	
	private static void tickByTickOperations(EClientSocket client) throws InterruptedException {
		
		/*** Requesting tick-by-tick data (only refresh) ***/
		//! [reqtickbytick]
		//client.reqTickByTickData(19001, ContractSamples.USStockAtSmart(), "Last");
		//client.reqTickByTickData(19002, ContractSamples.USStockAtSmart(), "AllLast");
		//client.reqTickByTickData(19003, ContractSamples.USStockAtSmart(), "BidAsk");
		//client.reqTickByTickData(19004, ContractSamples.EurGbpFx(), "MidPoint");
		//! [reqtickbytick]

		Thread.sleep(10000);

		//! [canceltickbytick]
		client.cancelTickByTickData(19001);
		client.cancelTickByTickData(19002);
		client.cancelTickByTickData(19003);
		client.cancelTickByTickData(19004);
		//! [canceltickbytick]
	}	
}