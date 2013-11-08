package com.recomdata.transmart.util
 
 

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties
import java.io.File;


class EmailViaSMTP {
		// To use a smtp host other than Gmail, simply change the following properties
		//Enter tha email ID from which mail is to be sent
		def from = "demouser2807@gmail.com"
		
	  
		def password="demo4321"
	  // according to the specifications of your host
	  // set the smtp host properties
		def host= "smtp.gmail.com"
	  //The location where files which will be generated before attaching it to the mail
		def location= "C:/Export/"
	
	 def sendEmail(messageBody,msgSubject,mailId,file) 
	 throws Exception{
		 
		 		 
		   String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		 
		 
		   //hostname of the machine that has smtp server
		 
		   
		 
		   //either turn on or turns off debugging during sending
		 
		 
		   boolean sessioDebug = true;
		 
		  
		   String messageText = messageBody;
		   
		  
		 
		 
		   String messageSubject = msgSubject;
		 
		 
		   // To use a smtp host other than Gmail, simply change the following properties
		 
		   // according to the specifications of your host
		 
		 
		 
		   // set the smtp host properties
		 
		 
		   Properties props = System.getProperties();
		   props.put("mail.smtp.host", host);
		   props.put("mail.transport.protocol.", "smtp");
		   props.put("mail.smtp.auth", "true");
		   props.put("mail.smtp.", "true");
		   props.put("mail.smtp.port", "465");
		   props.put("mail.smtp.socketFactory.fallback", "false");
		   props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
		 
		   Session mailSession = Session.getInstance(props, null);
		 
		   mailSession.setDebug(sessioDebug);
		 
		   try {
		 
		 
		 // create a message
		 
		 MimeMessage message = new MimeMessage(mailSession);
		 
		 //set message source
		 
		 message.setFrom( new InternetAddress("mohammad.u.shaikh@gmail.com"));
		 
		 //InternetAddress[] address = {new InternetAddress(to)};
		 
		 //set message recipients
		 
		 
		 message.setRecipients(Message.RecipientType.TO, mailId);
		 message.setSubject(messageSubject);
		 
		 
		 // create and fill the first message part
		 
		 MimeBodyPart messageBodyPart1 = new MimeBodyPart();
		 
		 messageBodyPart1.setText(messageText);
		 
		
		 // create the Multipart and add its parts to it
		 
		 
		 Multipart mpart = new MimeMultipart();
		 
		 mpart.addBodyPart(messageBodyPart1);
		 
		 // create the second message part
		 
		 if (file!=null) {
			 MimeBodyPart messageBodyPart2 = new MimeBodyPart();
		 
		 // attach the file to the message
		 
		 FileDataSource fdatasource = new FileDataSource(file);
		 messageBodyPart2.setDataHandler(new DataHandler(fdatasource));
		 messageBodyPart2.setFileName(fdatasource.getName());
		 
		 mpart.addBodyPart(messageBodyPart2);
		 }
		 // add the Multipart to the message
		 
		 message.setContent(mpart);
		 
		 
		 // set the Date: header
		 message.setSentDate(new Date());
		 
		 // send the message
		 
		 Transport transport = mailSession.getTransport("smtp");
		 transport.connect(host, from, password);
		 transport.sendMessage(message, message.getAllRecipients());
		   } catch (MessagingException mex) {
		 
		 mex.printStackTrace();
		 
		 
		 Exception ex = null;
		 
		 
		 if ((ex = mex.getNextException()) != null) {
		 
		 
			 ex.printStackTrace();
		 
		 }
		 
}}
}
