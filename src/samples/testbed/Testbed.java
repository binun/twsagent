package samples.testbed;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
	
	private static String targets = "shlomidolev@gmail.com,binunalex@gmail.com";
	private static String header = "PREDICTION";
	
    private static EWrapperImpl wrapper = null;
	
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
		
		LocalTime start = LocalTime.parse("16:30:00");
		LocalTime stop = LocalTime.parse("22:55:00");
		LocalTime n = LocalTime.now();
		Boolean isTargetAfterStartAndBeforeStop = ( n.isAfter( start ) && n.isBefore( stop ) ) ;
		if (!isTargetAfterStartAndBeforeStop) {
			System.out.println("No trade");
			return;
		}
		
		
		if (args.length<1)
			return;
		
		File f = new File(args[0]);
		if (f.isFile()) {
			sendemail(f.getName());
			return;
		}
	
		if (!f.isDirectory()) {
			return;
		}
		
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
		
		//Thread.sleep(1000);

		//dataRequests(wrapper.getClient());
		instance.requestAllData();
		
		for (;;) {
			Thread.sleep(500);
			if (instance.allUpdated())
				break;
			
		}
		System.out.println("Done");
		m_client.eDisconnect();
	}
}
