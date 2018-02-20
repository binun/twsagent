package samples.testbed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.ib.client.EClient;

import samples.testbed.contracts.ContractSamples;

import java.util.HashMap;

public class Shared {
	
	private String dir = "";
	private int errors=0;
	private ArrayList<String> stickers = new ArrayList<String>();
	private int [] updates = null;
	private int allStickers = 0;
    public  int startId=4001;
	//private ArrayList<HashMap<String, Double>> lastData = new ArrayList<HashMap<String,Double>>();
	private static Shared instance = null;
	private EClient mclient = null;
	
	public void requestAllData() {
		//this.requestDataForSticker("AXP");
		for (String sticker: stickers) {
			this.requestDataForSticker(sticker);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public synchronized void requestDataForSticker(String sticker) {
		int i = stickers.indexOf(sticker);
		if (i<0)
			return;
		mclient.reqRealTimeBars(startId+i, ContractSamples.USStockAtSmart(sticker), 5, "MIDPOINT", false, null);
		//mclient.reqMktData(startId+i, ContractSamples.USStockAtSmart(sticker), "100,221,233", false, false,null);
	}
	
	public synchronized void reportError() {
		errors++;
	}
	
	public synchronized void updateLastData(int order, double high,double close, double volume) {
		int st = order-startId;
	    if (updates[st]==0) {
		  this.updateSticker(stickers.get(st), high, close, volume);
		  System.out.println("            Sticker " + stickers.get(st) + " got update ");
		  updates[st]=1;
	
		//mclient.cancelMktData(order);
		  mclient.cancelRealTimeBars(order);
		  allStickers++;
		  
		  System.out.println("        Sticker Updates: " + allStickers + " against " + stickers.size());
	    }
	}
	
	private void updateSticker(String st, double lastHigh, double lastClose, double lastVolume) {
		String path = this.dir+File.separator+st+".csv";
		String [] lastR = this.lastRow(path);
	
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
	     
	    int day = calendar.get(Calendar.DATE);      
	    int month = calendar.get(Calendar.MONTH) + 1;
	    int year = calendar.get(Calendar.YEAR);
	    String datestr = String.format("%d%02d%02d",year,month,day);
	    //String date = lastR[1]; 20170830

		double open = Double.parseDouble(lastR[2]);
		double high = Double.parseDouble(lastR[3]);
		double low = Double.parseDouble(lastR[4]);
		double close = Double.parseDouble(lastR[5]);
		double volume = Double.parseDouble(lastR[6]);
		
		if (lastHigh<=0.0) {
			lastHigh=high;
		}
		
		if (lastClose<=0.0) {
			lastClose=close;
		}
		
		if (lastVolume<=0.0) {
			lastVolume=volume;
		}
		
		if (lastHigh!=high) {
			high=lastHigh;
		}
		if (lastClose!=close) {
			close=lastClose;
			
		}
			
		if (lastVolume !=volume) {
			volume = lastVolume;
		}
		
		//if (newDataArrived) {
		  try {
		       PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		       out.println(st+","+datestr+","+open+","+high+","+low+","+close+","+volume+"\n");
		       out.close();
		      } catch (Exception e) {
		            return;
		      }
		//}
	}
	
	public synchronized boolean allUpdated() {
		return allStickers==stickers.size();
	}
	
	private String[] lastRow(String fn) {
		String lastLine=null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fn));
			String line = null;
	        while((line = reader.readLine()) != null) {
	        	if (line.length()>1)
                   lastLine=line;
	        }
	        reader.close();
		}

	    catch (Exception x) {
	        return null;
	    }
		
		try {
			reader.close();
		} catch (Exception e) {
			
			return null;
		}
		
		if (lastLine==null)
			return null;
		return lastLine.split(",");
	}
	
	
	
	private Shared(EClient client, String dir) {
		this.dir = dir;
		this.mclient = client;
		
		File aDirectory = new File(dir);

	    String[] filesInDir = aDirectory.list();

	    for ( int i=0; i<filesInDir.length; i++ )
	    {
	       String f = filesInDir[i];
	       String extensionRemoved = f.split("\\.")[0];
	       stickers.add(extensionRemoved);
	       
	    }
		
		updates = new int[stickers.size()];
		for (int i=0; i < stickers.size();i++)
			updates[i] = 0;
			//lastData.add(new HashMap<String,Double>());
	}
	
	public static Shared getInstance(EClient c, String d) {
		if (instance==null)
			instance=new Shared(c, d);
		return instance;
	}
}
