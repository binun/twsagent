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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import samples.testbed.contracts.ContractSamples;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;


import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Testbed {
	
	private static String myemail = "astartradingltd@gmail.com";
	private static String mypass = "a*strading";
	
//	private static String myemail = "binunalex@gmail.com";
//	private static String mypass = "BW~35wc&";
	
	private static String targets = "binunalex@gmail.com,shlomidolev@gmail.com";
	private static String header = "PREDICTION";
	
    private static EWrapperImpl wrapper = null;
    private static boolean isLast=false;
    
   
    
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
		    String what = llc[2];
		    return what;
		} catch (Exception e) {
			return "";
		}
    }
	
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
		String what = lastPrediction(filename);
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(myemail));
		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(targets));
		
		message.setSubject(header);
		message.setText(what);

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
				
		if (args[1].equals("last")) 
			isLast=true;
		
        wrapper = new EWrapperImpl(args[0],isLast);
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		final Shared instance = Shared.getInstance(m_client, args[0],isLast);
		
		
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
		
		if (args[1].equals("prev")) {
			Shared.simyear = Integer.parseInt(args[2]);
		}
		
		if (args[1].equals("mail")) {
			sendemail("maillog.csv");
//			Map<String,Double> plan = new HashMap<String,Double>();
//			plan.put("CVX", 1.0);
//			plan.put("WMT", -1.0);
			String lp = lastPrediction("maillog.csv");
			instance.runAsPlan(lp);
			return;
		}
		
		instance.requestAllData(isLast);
		
		for (int i=0; i <120;i++) {
			Thread.sleep(500);
			if (instance.allUpdated())
				break;
			
		}
		
		
		System.out.println("Done");
		try {
			if (isLast)
			   instance.flushLastData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		instance.cancelAll();
		m_client.eDisconnect();
	}
}
