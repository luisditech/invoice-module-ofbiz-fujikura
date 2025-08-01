package com.fae.EDI.util;

import java.io.File;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Emailer {

       private static Logger logger = LoggerFactory.getLogger(Emailer.class);
       
       // CONFIGURACION 
       private String TIPO_EMAIL     = "SMTP";
       private String mailHost       = "smtp.services.fae.net";
       private int mailSmtpPort      = 25; 
       private String mailFrom       = "no-reply@eu.fujikura.com";
       //String mailReturnPath = PropertyContainer.EMAILER_MAILRETURNPATH;
       private String mailPasswd     = "";
       private final String username = mailFrom;
       private final String password = mailPasswd;       

       public void sendEmail(String aFromEmailAddr, String aToEmailAddr, String aSubject, String aBody) {

             if (TIPO_EMAIL.equalsIgnoreCase("TLS")) {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", mailHost);
                    props.put("mail.smtp.port", mailSmtpPort);
                    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                           protected PasswordAuthentication getPasswordAuthentication() {
                                  return new PasswordAuthentication(username, password);
                           }
                    });

                    try {
                           Message message = new MimeMessage(session);
                           message.setFrom(new InternetAddress(mailFrom));
                           message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                           message.setSubject(aSubject);
                           message.setText(aBody);

                           Transport.send(message);

                           System.out.println("TLS Mail sent succesfully!");

                    } catch (MessagingException e) {
                           logger.error("Error en envio de mail TLS. ");
                           e.printStackTrace();
                    }
             } else if (TIPO_EMAIL.equalsIgnoreCase("SSL")) {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", mailHost);
                    props.put("mail.smtp.socketFactory.port", mailSmtpPort);
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", mailSmtpPort);

                    Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                           protected PasswordAuthentication getPasswordAuthentication() {
                                  return new PasswordAuthentication(username, password);
                           }
                    });

                    try {
                           Message message = new MimeMessage(session);
                           message.setFrom(new InternetAddress(mailFrom));
                           message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                           message.setSubject(aSubject);
                           message.setText(aBody);

                           Transport.send(message);

                           System.out.println("SSL Mail sent succesfully!");

                    } catch (MessagingException e) {
                           logger.error("Error en envio de mail SSL. ");
                           e.printStackTrace();
                    }
             } else if (TIPO_EMAIL.equalsIgnoreCase("EXCHANGE")){
                    logger.info("inicio EXCHANGE");
                    Properties properties = new Properties();
                    properties.put("mail.transport.protocol", "smtp");
                    properties.put("mail.smtp.host", "mail.eu.fujikura.com");
                    properties.put("mail.smtp.port", "443"); //"2525");
                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.smtp.starttls.enable", "true");
                    properties.put("mail.smtp.connectiontimeout", "10000");

                    final String usernameEX = "no_reply@eu.fujikura.com";
                    final String passwordEX = "";                  
                    
                    /*Authenticator authenticator = new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(usernameEX, passwordEX);
                        }
                    };*/

                    Transport transport = null;

                    try {
                        //Session session = Session.getDefaultInstance(properties, authenticator);
                        //MimeMessage mimeMessage = createMimeMessage(session, mimeMessageData);
                        
                           Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(
                                                      usernameEX,passwordEX);
                                       }
                              });                                  
                           
                           
                        Message message = new MimeMessage(session);
                           message.setFrom(new InternetAddress(mailFrom));
                           message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                           message.setSubject(aSubject);
                           message.setText(aBody);
                        
                        transport = session.getTransport();
                        transport.connect(username, password);
                        //transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                        transport.sendMessage(message, message.getAllRecipients());
                    }catch (Exception e){
                           e.printStackTrace();
                    } finally {
                        if (transport != null) try { transport.close(); } catch (MessagingException logOrIgnore) {}
                    }                   
                    logger.info("Fin EXCHANGE");
             } else {  
                    // SMTP
                    Properties properties = System.getProperties();
                    properties.setProperty("mail.smtp.host", mailHost);

                    //
                    // Here, no Authenticator argument is used (it is null).
                    // Authenticators are used to prompt the user for user
                    // name and password.
                    Session session = Session.getDefaultInstance(properties);
                    MimeMessage message = new MimeMessage(session);
                    try {
                           message.setFrom(new InternetAddress(aFromEmailAddr));
                           message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
                           message.setSubject(aSubject);
                           message.setText(aBody);
                           Transport.send(message);
                    } catch (MessagingException ex) {
                           System.err.println("SMTP Cannot send email. " + ex);
                           logger.error("SMTP Cannot send email. " + ex);
                    }
             }

       }    
       
       public void sendEmailwAttach(String aFromEmailAddr, String aToEmailAddr, String aSubject, String aBody,String fileName) {

           if (TIPO_EMAIL.equalsIgnoreCase("TLS")) {
                  Properties props = new Properties();
                  props.put("mail.smtp.auth", "true");
                  props.put("mail.smtp.starttls.enable", "true");
                  props.put("mail.smtp.host", mailHost);
                  props.put("mail.smtp.port", mailSmtpPort);
                  Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                         protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                         }
                  });

                  try {
                         Message message = new MimeMessage(session);
                         message.setFrom(new InternetAddress(mailFrom));
                         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                         message.setSubject(aSubject);
                         message.setText(aBody);

                         Transport.send(message);

                         System.out.println("TLS Mail sent succesfully!");

                  } catch (MessagingException e) {
                         logger.error("Error en envio de mail TLS. ");
                         e.printStackTrace();
                  }
           } else if (TIPO_EMAIL.equalsIgnoreCase("SSL")) {
                  Properties props = new Properties();
                  props.put("mail.smtp.host", mailHost);
                  props.put("mail.smtp.socketFactory.port", mailSmtpPort);
                  props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                  props.put("mail.smtp.auth", "true");
                  props.put("mail.smtp.port", mailSmtpPort);

                  Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                         protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                         }
                  });

                  try {
                         Message message = new MimeMessage(session);
                         message.setFrom(new InternetAddress(mailFrom));
                         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                         message.setSubject(aSubject);
                         message.setText(aBody);

                         Transport.send(message);

                         System.out.println("SSL Mail sent succesfully!");

                  } catch (MessagingException e) {
                         logger.error("Error en envio de mail SSL. ");
                         e.printStackTrace();
                  }
           } else if (TIPO_EMAIL.equalsIgnoreCase("EXCHANGE")){
                  logger.info("inicio EXCHANGE");
                  Properties properties = new Properties();
                  properties.put("mail.transport.protocol", "smtp");
                  properties.put("mail.smtp.host", "mail.eu.fujikura.com");
                  properties.put("mail.smtp.port", "443"); //"2525");
                  properties.put("mail.smtp.auth", "true");
                  properties.put("mail.smtp.starttls.enable", "true");
                  properties.put("mail.smtp.connectiontimeout", "10000");

                  final String usernameEX = "no_reply@eu.fujikura.com";
                  final String passwordEX = "";                  
                  
                  /*Authenticator authenticator = new Authenticator() {
                      protected PasswordAuthentication getPasswordAuthentication() {
                          return new PasswordAuthentication(usernameEX, passwordEX);
                      }
                  };*/

                  Transport transport = null;

                  try {
                      //Session session = Session.getDefaultInstance(properties, authenticator);
                      //MimeMessage mimeMessage = createMimeMessage(session, mimeMessageData);
                      
                         Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                                  protected PasswordAuthentication getPasswordAuthentication() {
                                      return new PasswordAuthentication(
                                                    usernameEX,passwordEX);
                                     }
                            });                                  
                         
                         
                      Message message = new MimeMessage(session);
                         message.setFrom(new InternetAddress(mailFrom));
                         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aToEmailAddr));
                         message.setSubject(aSubject);
                         message.setText(aBody);
                      
                      transport = session.getTransport();
                      transport.connect(username, password);
                      //transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                      transport.sendMessage(message, message.getAllRecipients());
                  }catch (Exception e){
                         e.printStackTrace();
                  } finally {
                      if (transport != null) try { transport.close(); } catch (MessagingException logOrIgnore) {}
                  }                   
                  logger.info("Fin EXCHANGE");
           } else {  
                  // SMTP
                  Properties properties = System.getProperties();
                  properties.setProperty("mail.smtp.host", mailHost);

                  //
                  // Here, no Authenticator argument is used (it is null).
                  // Authenticators are used to prompt the user for user
                  // name and password.
                  Session session = Session.getDefaultInstance(properties);
                  MimeMessage message = new MimeMessage(session);
                  try {
                         message.setFrom(new InternetAddress(aFromEmailAddr));
                         message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
                         message.setSubject(aSubject);                          
                         message.setText(aBody);
                         
                         if (!Util.isNullOrBlank(fileName)){
	                         File f = new File(fileName);
	                         if(f.exists() && !f.isDirectory()) {     
	                        	 
	                        	 MimeBodyPart messageBodyPart = new MimeBodyPart();
		                         Multipart multipart = new MimeMultipart();
		                         
	                        	 BodyPart messageBodyPart1 = new MimeBodyPart();
	                             messageBodyPart1.setText(aBody);
	                             //messageBodyPart1.setContent(aBody, "text/html");	                                                     			                         		                         
		                         multipart.addBodyPart(messageBodyPart1);
		                         
		                         //messageBodyPart = new MimeBodyPart();		                         		                         
		                         String fileNameAttch = FilenameUtils.getName(fileName); //"attachmentName";
		                         DataSource source = new FileDataSource(fileName);
		                         messageBodyPart.setDataHandler(new DataHandler(source));
		                         messageBodyPart.setFileName(fileNameAttch);
		                         multipart.addBodyPart(messageBodyPart);
		                         message.setContent(multipart);
		                         logger.info("File: "+fileName+" Attached!!!");
	                  		 }else{
	                  			logger.error("sendEmailwAttach: Attachment "+fileName+" Not found!!!");
	                  			//sBody = sBody+(char) 10 + (char) 13 +"Attachment: "+fileName+" Not found!!!";
	                  		 }
                         }else{
                        	 logger.info("No attached File!!!");
                         }
                         //System.out.println("Body:"+sBody);
                         //message.setText(sBody);
                         
                         //System.out.println("Sending.....");
                         
                         Transport.send(message);
                  } catch (MessagingException ex) {
                         System.err.println("SMTP Cannot send email. " + ex);
                         logger.error("SMTP Cannot send email. " + ex);
                  }
           }

     }        
                    
}
