package samples.testbed;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import samples.testbed.contracts.ContractSamples;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;


import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class Testbed {
	
	private static String myemail = "astartradingltd@gmail.com";
	private static String mypass = "a*strading";
	
	//private static String myemail = "binunalex@gmail.com";
	//private static String mypass = "BW~35wc&";
	
	
	private static String targets = "";
	private static String reviewers = "";
	private static String header = "PREDICTION";
	public static String histlen = "115 D";
	
    private static EWrapperImpl wrapper = null;
    private static boolean isLast=true;
    
    private static void parseOptions() {
    	String filename = "twsopts.txt";
    	if (new File(filename).exists()==false) {
    		System.out.println("No options");
    		System.exit(0);
    	}
    	try {
    	  BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
	    
	      String strLine = null, tmp="";
		  while ((tmp = bufferedReader.readLine()) != null) {
		     strLine = tmp;
		     String[] opt = strLine.split("=");
		     String command = opt[0].toLowerCase();
		     if (command.equals("orders")) {
		    	 if (opt[1].toLowerCase().equals("true"))
		    		 Shared.orderyes=true;
		    	 else
		    		 Shared.orderyes=false;
		     }
		     
		     if (command.toLowerCase().equals("mails")) {
		    	 targets = opt[1];
		     }
		     
		     if (command.toLowerCase().equals("reviewers")) {
		    	 reviewers = opt[1];
		     }
		     
		     if (command.toLowerCase().equals("hist")) {
		    	 histlen = opt[1]+" M";
		     }
		     
		  }
	      bufferedReader.close();
	    
	   } catch (Exception e) {
		return;
	   }
    	
       if (reviewers.length()>3) {
	    	Date date = new Date() ;
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
	    	dateFormat.format(date);
            boolean good_time = false;
            try {
            	if(dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse("23:00")))
            		good_time=true;
            }
            catch (Exception e) {
            	
            }
	        if (good_time==true) {
	        	targets=targets+","+reviewers;
	        }
       }
    }
    
    private static String lastPrediction(String filename) {
    	try {
			
		    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
		    
		    String strLine = null, tmp="",lastLine="";
			while ((tmp = bufferedReader.readLine()) != null) {
			     strLine = tmp;
			     if (strLine.length()>3)
			    	 lastLine=strLine;
			  }
		    bufferedReader.close();
		    String[] llc = lastLine.split(",");
		    String what = llc[1];
		    return what;
		} catch (Exception e) {
			return "";
		}
    }
    
    private static String nearLastGain(String filename) {
    	try {
			
		    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
		    
		    String strLine = null, tmp="",g="";
			while ((tmp = bufferedReader.readLine()) != null) {
			     strLine = tmp;
			     if (strLine.length()>3) {
			    	 String[] llc = strLine.split(",");
			    	 String gain = llc[1];
			    	 if (gain.length()>1)
			    		 g = gain;
			     }
			    	 
			  }
		    bufferedReader.close();
		    
		    return g;
		} catch (Exception e) {
			return "";
		}
    }
	
	public static void sendemail(String filename,String addresses) {
		
		if (addresses.length()<=1)
			return;
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
		String what = lastPrediction(filename);
		//String prev = nearLastGain(filename);
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(myemail));
		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(addresses));
		
		message.setSubject(header);
		message.setText("Current prediction:"  + what);

		Transport.send(message);

		System.out.println("Done");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
    
	public static void main(String[] args) throws InterruptedException {
		
		if (args.length<1)
			return;
				
		if (args.length>1 && args[1].equals("hist")) {
			isLast=false;
		}
		
		parseOptions();
		
		header = args[0].toUpperCase() + " " + header;
		String maillog = args[0].toLowerCase()+ "csv_maillog.csv";
        wrapper = new EWrapperImpl(args[0]);
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		final Shared instance = Shared.getInstance(m_client, args[0]);
		
		
		//! [connect]
		m_client.eConnect("127.0.0.1", 7496, 1);
		
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
		
		instance.reqPosition();
		
		if (args.length>1 && args[1].equals("prev")) {
			Shared.simyear = Integer.parseInt(args[2]);
		}
		
		if (args.length>1 && args[1].equals("mail")) {
		    if (targets.length()>3)
			  sendemail(maillog,targets);
		    
		    
//			Map<String,Double> plan = new HashMap<String,Double>();
//			plan.put("CVX", 1.0);
//			plan.put("WMT", -1.0);
			String lp = lastPrediction(maillog);
			instance.runAsPlan(lp);
			for (int i=0; i <10;i++) {
				Thread.sleep(1000);
				//if (instance.allUpdated())
					//break;
				
			}
			instance.cancelAll();
			m_client.eDisconnect();
			return;
		}
		
		instance.requestAllData(isLast);
		
		for (int i=0; i <10;i++) {
			Thread.sleep(1000);
			//if (instance.allUpdated())
				//break;
		}
		
		File datasource = new File(args[0]+"csv");
		

		
        if (datasource.exists()) {
		for (String s: instance.blackList.keySet()) {
		
			  for(File file: datasource.listFiles())
			    {
		          String fname = file.getName();
		          if (s.length()>=1 && instance.blackList.get(s)==0 && fname.contains(s)) {
		        	 System.out.println("Waited for history for " + s + " in vain");
				     file.delete();
		          }
			    }
		    }
		}
		
		
		System.out.println("Done");
		
		instance.cancelAll();
		m_client.eDisconnect();
	}
}
