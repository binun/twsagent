package samples.testbed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;


import com.ib.client.Bar;
import com.ib.client.EClient;

import samples.testbed.contracts.ContractSamples;
import samples.testbed.orders.OrderSamples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Shared {
	
	private String dir = "";
	
	private ArrayList<String> stickers = new ArrayList<String>();
	public Map<String,Integer> blackList = new HashMap<String,Integer>();
	private Set<Integer> allStickers = new HashSet<Integer>();
    public  int startId=1;
    private volatile int curOrder=-1;
	
	private static Shared instance = null;
	private EClient mclient = null;
	private Map<String,Double> lastCloses = new HashMap<String,Double>();
	
	private Map<String,Double> position = new HashMap<String,Double>();
	private Map<String,Double> costs = new HashMap<String,Double>();
	
	private List<String> longs=new ArrayList<String>();
	private List<String> shorts=new ArrayList<String>();
	
    private static double investment = 500;
    
    public static int simyear = -1;
    public static boolean orderyes=false;
    
    private void decomposeLast(String msg) {
    	
		StringTokenizer st = new StringTokenizer(msg, ":// ");
		longs.clear();
		shorts.clear();
		boolean nowlong=false;
		boolean nowshort=false;
		
		while (st.hasMoreTokens()) {
			String tok = st.nextToken().toLowerCase();
			if (tok.equals("longs")) {
				nowlong=true;
				nowshort=false;
				continue;
			}
			
			if (tok.equals("shorts")) {
				nowlong=false;
				nowshort=true;
				continue;
			}
			
			if (nowlong && nowshort==false) 
				longs.add(tok);
			if (nowshort && nowlong==false)
				shorts.add(tok);
		}
    }
			
    public String getSticker(int id) {
    	return stickers.get(id-startId);
    }
    public void setOrderId(int orderId) {
    	curOrder = orderId;
    	System.out.println("Next Valid Id: ["+curOrder+"]");
    }
	
	public synchronized void reqPosition() {
		mclient.reqPositions();
		try {
			this.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void relPosition()  {
		this.notify();
	}

	
	public synchronized void updatePosition(String sticker,double pos, double avgCost) {
		System.out.println("Position for "+sticker+" is "+pos + " shares " + " share cost: " + avgCost);
	    
		position.put(sticker, pos);
		costs.put(sticker,avgCost);
	}
	
	public void runAsPlan(String what) {
		this.decomposeLast(what);
		Map<String, Double> plan = this.calculatePlan(longs, shorts);
		this.executeOrders(plan);
	}
	
	private Map<String,Double> calculatePlan(List<String> longs,List<String> shorts) {
		Map<String,Double> plan = new HashMap<String,Double>();
		for (String l:longs) {
			if (position.containsKey(l)) {
				Double q = position.get(l);
				if (q<0)
					plan.put(l, -2*q);
				position.remove(l);
			}
			else
			  plan.put(l, 1.0);
		}	
		
		for (String s:shorts) {
			if (position.containsKey(s)) {
				Double q = position.get(s);
				if (q>0)
					plan.put(s, -2*q);
				position.remove(s);
			}
			else
				plan.put(s, -1.0);
		}
		
		for (String k:position.keySet()) {
			Double q = position.get(k);
			plan.put(k, -q);
		}
		
		return plan;
	}
	
	public synchronized void placeOrder(String sticker,String action,double quantity) {
		
		System.out.println(sticker + " to " + action);
		if (orderyes==true)
		   mclient.placeOrder(curOrder++, ContractSamples.USStockAtSmart(sticker), OrderSamples.MarketOrder(action, quantity));
	}
	
	public synchronized void executeOrders(Map<String,Double> plan) {
		while (curOrder<=0) {}
		for (String key: plan.keySet()) {
			Double quantity = plan.get(key);
			String action="";
			if (quantity>0) {
				action = "BUY";
			}
			else {
				action="SELL";
				quantity = -quantity;
			}
	     this.placeOrder(key, action, quantity);	
		}
		

	}
	
	public synchronized int getStartID() {
		return startId;
	}
	
	public synchronized void setStartID(int st) {
		startId = st;
	}
	public void requestAllData(boolean last) {
		while (this.getStartID()==-1) {
			try {
				for (int i=0; i < 5; i++) Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (String sticker: stickers) {
			this.requestDataForSticker(sticker,last);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public synchronized void requestDataForSticker(String sticker,boolean last) {
		int i = stickers.indexOf(sticker);
		if (i<0)
			return;
		
		Calendar cal = null;
		String period = "12 M";
		if (simyear<0) {
			cal = Calendar.getInstance();
			if (last==false)
		     cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		else {
			cal = new GregorianCalendar(simyear,0,0);	
			period = "12 M";
		}
		
        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String formatted = form.format(cal.getTime());
        
        if (last==true) 
        	formatted="";
        
        
        //System.out.println("Requesting history for " + sticker);
        mclient.reqHistoricalData(startId+i, 
        		ContractSamples.USStockAtSmart(sticker), 
        		formatted, 
        		period, 
        		"1 day", 
        		"MIDPOINT", 
        		0, 
        		1, 
        		false, 
        		null);
    
	}
	
	public synchronized void reportError() {
		
	}
	
	
	public synchronized void updateLastData(int order, double high,double close, double volume) {
		int st = order-startId;
		String sticker = stickers.get(st);
		if (lastCloses.containsKey(sticker)==false) {
		   System.out.print(" LD " + sticker);
	       lastCloses.put(sticker, close);
		}
		 
		
		  
		//mclient.cancelRealTimeBars(order);
	}
	
	public synchronized void updateHistData(int order, Bar bar) {
		int st = order-startId;
	   
		this.updateStickerHist(stickers.get(st), bar);
		//System.out.println("            Sticker " + stickers.get(st) + " got history item ");
		//mclient.cancelHistoricalData(order);
		 
		
		//System.out.println("         History: " + allStickers + " against " + stickers.size());
	    
	}
	
	private void updateStickerHist(String st, Bar bar) {
		
		String path = this.dir+File.separator+st+".csv";
		
		try {
		       PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		       String datestr = bar.time().split(" ")[0];
		       double open = bar.open();
		       double high = bar.high();
		       double low = bar.low();
		       double close = bar.close();
		       double volume = bar.volume();
		       
		       out.println(datestr+" "+open+" "+high+" "+low+" "+close+" "+volume+"\n");
		       out.close();
		    } catch (Exception e) {
		            return;
		    }
		//}
	}
	
	public void flushLastData() throws IOException {
		
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
	     
	    int day = calendar.get(Calendar.DATE);      
	    int month = calendar.get(Calendar.MONTH) + 1;
	    int year = calendar.get(Calendar.YEAR);
	    String datestr = String.format("%d%02d%02d",year,month,day);
		
		for (String st : stickers) {
			String path = this.dir+File.separator+st+".csv";
			File p = new File(path); 
			if (p.exists()==false)
				continue;
	
			
			Double val=1.0;
			if (lastCloses.containsKey(st)) {
			   val = lastCloses.get(st);
			   //System.out.println(" Write Last data for " + st + " ");
			}
			else {
				  
				  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
				  String strLine = null, tmp="",lastLine="";
				  while ((tmp = br.readLine()) != null) {
				     strLine = tmp;
				     if (strLine.length()>3)
				    	 lastLine=strLine;
				  }
				  
				  if (lastLine.length()>3) {
				     String [] components = lastLine.split(" ");
				     val = Double.parseDouble(components[components.length-2]);
				  }
				  else {
				       System.out.println(":mockery");
				       val=1.0;
				  }
			}
		 
		   PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		   out.println(datestr+" "+1.0+" "+1.0+" "+1.0+" "+val+" "+1.0+"\n");
		   out.close();
	     }
	}
	
	public synchronized boolean allUpdated() {
		System.out.println(" Current " + allStickers.size()+ " Target " + stickers.size());
		return allStickers.size()==stickers.size();
	}
	
	public synchronized void oneMore(int orderId) {
		allStickers.add(new Integer(orderId));
		String s = stickers.get(orderId-startId);
		blackList.put(s, 1);
	}
	
	public void cancelAll() {
		for (Integer order: allStickers) {
			mclient.cancelHistoricalData(order);
		}
	}
	
	
	

	private Shared(EClient client, String index) {
		
		this.mclient = client;
		this.dir=index+"_csv";
	
		File theDir = new File(dir); 
		if (!theDir.exists())
		     theDir.mkdir();
		else {
			if (simyear<0) 
			  for(File file: theDir.listFiles()) 
			      if (!file.isDirectory()) 
			          file.delete();
		}
		
		String stickernames = index+"_stickers.csv";
		File f = new File(stickernames);
		
		if (!f.exists()) {
			System.out.println("No sticker set");
			System.exit(0);
		}
		
		
		try {
			stickers = (ArrayList<String>) Files.readAllLines(FileSystems.getDefault().getPath(stickernames), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("No sticker set");
			System.exit(0);
		}
		
		for (String s:stickers) {
			blackList.put(s, 0);
		}
	}
	
	public static Shared getInstance(EClient c, String d) {
		if (instance==null)
			instance=new Shared(c, d);
		return instance;
	}
}
