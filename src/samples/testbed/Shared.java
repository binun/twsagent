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
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


import com.ib.client.Bar;
import com.ib.client.EClient;

import samples.testbed.contracts.ContractSamples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Shared {
	
	private String dir = "";
	private int errors=0;
	private ArrayList<String> stickers = new ArrayList<String>();
	
	private Set<Integer> allStickers = new HashSet<Integer>();
    public  int startId=1;
    private int curOrder=0;
	//private ArrayList<HashMap<String, Double>> lastData = new ArrayList<HashMap<String,Double>>();
	private static Shared instance = null;
	private EClient mclient = null;
	private Map<String,Double> lastCloses = new HashMap<String,Double>();
	
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
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String formatted = form.format(cal.getTime());
        System.out.println("Requesting history for " + sticker);
        if (last==false)
        mclient.reqHistoricalData(startId+i, 
        		ContractSamples.USStockAtSmart(sticker), 
        		formatted, 
        		"4 M", 
        		"1 day", 
        		"MIDPOINT", 
        		0, 
        		1, 
        		false, 
        		null);
		
        else
        	mclient.reqRealTimeBars(startId+i, ContractSamples.USStockAtSmart(sticker), 5, "MIDPOINT", false, null);
	}
	
	public synchronized void reportError() {
		errors++;
	}
	
	public synchronized void updateLastData(int order, double high,double close, double volume) {
		int st = order-startId;
		String sticker = stickers.get(st);
		if (lastCloses.containsKey(sticker)==false)
	       lastCloses.put(sticker, close);
		 
		System.out.println("            Sticker " + sticker + " got update ");
		  
		//mclient.cancelRealTimeBars(order);
	}
	
	public synchronized void updateHistData(int order, Bar bar) {
		int st = order-startId;
	   
		this.updateStickerHist(stickers.get(st), bar);
		System.out.println("            Sticker " + stickers.get(st) + " got history item ");
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
			
			System.out.print(" Saving data for " + st + " ");
			
			Double val=1.0;
			if (lastCloses.containsKey(st)) {
			   val = lastCloses.get(st);
			   System.out.println(":real last data");
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
	}
	
	public void cancelAll() {
		for (Integer order: allStickers) {
			mclient.cancelHistoricalData(order);
		}
	}
	
	
	

	private Shared(EClient client, String index,boolean last) {
		
		this.mclient = client;
		this.dir=index+"_csv";
	
		File theDir = new File(dir); 
		if (!theDir.exists())
		     theDir.mkdir();
		else {
			if (last==false) 
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
	}
	
	public static Shared getInstance(EClient c, String d, boolean last) {
		if (instance==null)
			instance=new Shared(c, d,last);
		return instance;
	}
}