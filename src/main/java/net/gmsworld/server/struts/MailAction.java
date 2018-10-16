package net.gmsworld.server.struts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.config.ConfigurationManager;

public class MailAction extends ActionSupport implements ServletRequestAware {

	    private static final long serialVersionUID = 1L;
	    private Logger logger = Logger.getLogger(getClass().getName());
	 
	    private HttpServletRequest request;
		
	    private String from;
	    private String password;
	    private String to;
	    private String subject;
	    private String body;
	    private String fromNick;
	    private String toNick;
	    private String contentType;
	    private String cc;
	    private String ccNick;
	    private String recipients;
	      	    
	    @Override
		public void setServletRequest(HttpServletRequest request) {
			this.request = request;
		}

	    public String sendEmail() 
	    {
	       if (StringUtils.isNotEmpty(from) && (StringUtils.isNotEmpty(to) || StringUtils.isNotEmpty(recipients)) && (StringUtils.isNotEmpty(body) || StringUtils.isNotEmpty(subject))) {	    	
	    	   try
	    	   {
	    		   Properties properties = new Properties();
	    	
	    		   String host = System.getenv("SMTP_HOST");
	    	       if (StringUtils.isEmpty(host)) {
	    	    	   host = "localhost";
	    	       }
	    	       
	    	       int port = 25; 
	    	       String portStr = System.getenv("SMTP_PORT");
	    	       if (StringUtils.isNotEmpty(portStr)) {
	    	    	   try {
	    	    		   port = Integer.valueOf(portStr);
	    	    	   } catch (Exception e) {}
	    	       }
	    	       
	    	       String sslport = System.getenv("SMTP_SSL_PORT");
	    	       if (StringUtils.isNotEmpty(sslport)) {
	    	    	   try {
	    	    		   logger.info("Using SSL");
	    	    		   port = Integer.valueOf(sslport);
	    	    		   properties.put("mail.smtp.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    	    		   properties.put("mail.smtp.ssl.trust", host); //,"*");
	    	    		   properties.put("mail.smtp.ssl.enable", "true");
	    	    	   } catch (Exception e) {}
	    	       } 
	    	       
	    		   Session session = Session.getInstance(properties);
	    		 
	    		   String debug = System.getenv("SMTP_DEBUG");
	    		   if (StringUtils.equalsIgnoreCase(debug, "true")) {
	    			   session.setDebug(true);
	    		   } else {
	    			   session.setDebug(false);
	    		   }
	    		   
	    		   Transport t = session.getTransport("smtp"); 
	    		   
	    		   t.connect(host, port, from, password);
	    		   
	    		   logger.log(Level.INFO, "Mail client connected to " + t.getURLName().getHost() + ":" + t.getURLName().getPort());
	    		   
	    		   MimeMessage message = new MimeMessage(session);
	          
	    		   if (StringUtils.isNotEmpty(fromNick)) {
	    			   message.setFrom(new InternetAddress(from, fromNick));
	    		   } else {
	    			   message.setFrom(new InternetAddress(from));
	    		   }
	          
	    		   if (StringUtils.isNotEmpty(to) && StringUtils.isNotEmpty(toNick)) {
	    			   message.addRecipient(Message.RecipientType.TO, new InternetAddress(to, toNick));
	    		   } else if (StringUtils.isNotEmpty(to)) {
	    			   message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); 
	    		   }
	          
	    		   if (StringUtils.isNotEmpty(cc) && StringUtils.isNotEmpty(ccNick)) {
	    			   message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc, ccNick));
	    		   } else if (StringUtils.isNotEmpty(cc)) {
	    			   message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));  
	    		   }
	    		   
	    		   if (StringUtils.isNotEmpty(recipients)) {
	    			   String[] allrecipients = StringUtils.split(recipients, "|");
	    			   for (int i = 0; i<allrecipients.length; i++) {
	    				   String[] r = StringUtils.split(allrecipients[i], ":");
	    				   if (r.length == 2) {
	    					    if (StringUtils.equalsIgnoreCase(r[0], "to")) {
	    					    	message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(r[1]));
	    					    } else if (StringUtils.equalsIgnoreCase(r[0], "cc")) {
	    					    	message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(r[1]));
	    					    } else if (StringUtils.equalsIgnoreCase(r[0], "bcc")) {
	    					    	message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(r[1]));
	    					    } 
	    				   }
	    			   }
	    		   }
	          
	    		   if (StringUtils.isNotEmpty(subject)) {
	    			   message.setSubject(subject, "UTF-8");
	    		   }
	          
	    		   if (StringUtils.endsWith(contentType, "html") && StringUtils.isNotEmpty(body)) {
	    			   message.setContent(body, "text/html; charset=UTF-8");
	    		   } else if (StringUtils.isNotEmpty(body)) {
	    			   message.setText(body, "UTF-8");
	    		   }
	          
	    		   message.setSentDate(new Date());
	         
	    		   t.sendMessage(message, message.getAllRecipients());
	    		   
	    		   String output = null;
	    		   if (to != null) {
	    			   output = "{\"status\":\"Message " + message.getMessageID() + " sent to " + to + "\"}";
	    		   } else {
	    			   output = "{\"status\":\"Message " + message.getMessageID() + " sent to " + recipients + "\"}"; 
	    		   }
	    		   logger.log(Level.INFO, output);
	    		   request.setAttribute("output", output);
	    		   
	    		   t.close();
	    		   
	    		   return SUCCESS;
	    	   }
	    	   catch(Exception e)
	    	   {
	    		   logger.log(Level.SEVERE, e.getMessage(), e);
	    		   addActionError(e.getMessage());
	    		   return ERROR;  
	    	   }
	       } else {
	    	   addActionError("Missing required parameters!");
		       return ERROR; 
	       }
	    }

	    public String getFrom() {
	       return from;
	    }

	    public void setFrom(String from) {
	       this.from = from;
	    }

	    public String getPassword() {
	       return password;
	    }

	    public void setPassword(String password) {
	       this.password = password;
	    }

	    public String getTo() {
	       return to;
	    }

	    public void setTo(String to) {
	       this.to = to;
	    }

	    public String getSubject() {
	       return subject;
	    }

	    public void setSubject(String subject) {
	       this.subject = subject;
	    }

	    public String getBody() {
	       return body;
	    }

	    public void setBody(String body) {
	       this.body = body;
	    }

	    public String getFromNick() {
			return fromNick;
		}

		public void setFromNick(String fromNick) {
			this.fromNick = fromNick;
		}

		public String getToNick() {
			return toNick;
		}

		public void setToNick(String toNick) {
			this.toNick = toNick;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public String getCc() {
			return cc;
		}

		public void setCc(String cc) {
			this.cc = cc;
		}

		public String getCcNick() {
			return ccNick;
		}

		public void setCcNick(String ccNick) {
			this.ccNick = ccNick;
		}

		public String getRecipients() {
			return recipients;
		}

		public void setRecipients(String recipients) {
			this.recipients = recipients;
		}

		 private int hear( BufferedReader in ) throws IOException {
	          String line = null;
	          int res = 0;
	          while ( (line = in.readLine()) != null ) {
	        	  String pfx = line.substring( 0, 3 );
	        	  logger.info("Received: " + line);
	        	  try {
	        		  res = Integer.parseInt( pfx );
	        	  } 
	        	  catch (Exception ex) {
	        		  logger.log(Level.SEVERE, ex.getMessage(), ex);
	        		  res = -1;
	        	  }
	        	  if ( line.charAt( 3 ) != '-' ) break;
	          }
	          return res;
	      }
	    
	      private void say( BufferedWriter wr, String text )  throws IOException {
	    	  wr.write( text + "\r\n" );
	    	  wr.flush();
	    	  logger.info("Sending: " + text);
	    	  return;
	      }
	      
	      private ArrayList<String> getMX( String hostName ) throws NamingException {
	    	  Hashtable<String, String> env = new Hashtable<String, String>();
	    	  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
	          //env.put(Context.PROVIDER_URL, "dns://8.8.8.8 dns://8.8.4.4");
	    	  DirContext ictx = new InitialDirContext( env );
	    	  Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" });
	    	  Attribute attr = attrs.get( "MX" );
	    	  
	    	  if (( attr == null ) || ( attr.size() == 0 )) {
	    		  attrs = ictx.getAttributes( hostName, new String[] { "A" });
	    		  attr = attrs.get( "A" );
	    		  if ( attr == null ) {
	    			  throw new NamingException( "No match for name '" + hostName + "'" );
	    		  }
	    	  }
	    	  
	    	  ArrayList<String> res = new ArrayList<String>();
	    	  NamingEnumeration<?> en = attr.getAll();
	    	  while ( en.hasMore() ) {
	    		  	String x = (String) en.next();
	    		  	String f[] = x.split( " " );
	    		  	if ( f[1].endsWith( "." )) { 
	    		  		f[1] = f[1].substring( 0, (f[1].length() - 1));
	    		  	}
	    		  	res.add( f[1] );
	    	  }
	    	  
	    	  logger.info( "Found " + res.size() + " MX servers");
	    	  
	    	  return res;
	      }
	      
	      public String emailAccountExists() {
	    	  if (StringUtils.isNotEmpty(to) ) {
	    		  // Find the separator for the domain name
	    		  int pos = to.indexOf( '@' );
	    		  // If the address does not contain an '@', it's not valid
	    		  if ( pos == -1 ) {
	    			  addActionError("Invalid address format");
	    			  return ERROR; 
	    		  }
	    		  // Isolate the domain/machine name and get a list of mail exchangers
	    		  String domain = to.substring( ++pos );
	    		  ArrayList<String> mxList = null;
	    		  try {
	    			  mxList = getMX( domain );
	    		  } catch (NamingException ex) {
	    			  logger.severe(ex.getMessage());
	    			  addActionError(ex.getMessage());
	    			  return ERROR; 
	    		  }
	    	  
	    		  if ( mxList.size() == 0 ) {
	    			  addActionError("No mail servers found");
	    			  return ERROR; 
	    		  }
	        
	    		  for ( int mx = 0 ; mx < mxList.size() ; mx++ ) {
	    			  boolean valid = false;
	    			  Socket skt = null;
	    			  BufferedReader rdr = null;
	    			  BufferedWriter wtr = null;
	    			  try {
	    				  int res;
	    				  skt = new Socket( (String) mxList.get( mx ), 25 );
	    				  rdr = new BufferedReader( new InputStreamReader( skt.getInputStream() ) );
	    				  wtr = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream() ) );
	    				  res = hear( rdr );
	    				  if ( res != 220 ) throw new Exception( "Invalid header" );
	    				  say( wtr, "EHLO gms-world.net");
	    				  res = hear( rdr );
	    				  if ( res != 250 ) throw new Exception( "Not ESMTP" );
	    				  // validate the sender address  
	    				  say( wtr, "MAIL FROM: <" + ConfigurationManager.DL_MAIL + ">" );
	    				  res = hear( rdr );
	    				  if ( res != 250 ) throw new Exception( "Sender rejected" );
	    				  say( wtr, "RCPT TO: <" + to + ">" );
	    				  res = hear( rdr );
	    				  say( wtr, "RSET" ); hear( rdr );
	    				  say( wtr, "QUIT" ); hear( rdr );
	    				  if ( res != 250 && res != 451) {
	    					  throw new Exception("Received following SMTP server response: " + res + " from " + mxList.get( mx ));
	    				  };
	    				  valid = true;
	    			  } catch (Exception ex) {
	    				  logger.severe(ex.getMessage());
	    				  addActionError(ex.getMessage());
	    			  } finally {
	    				  if (rdr != null) {
	    					  try {
	    						  rdr.close();
	    					  } catch (Exception e) {}
	    				  }
	    				  if (wtr != null) {
	    					  try { 
	    						  wtr.close();
	    					  } catch (Exception e) {}
	    				  }
	    				  if (skt != null)  {
	    					  try {
	    						  skt.close();
	    					  } catch (Exception e) {}
	    				  }
	    			  }
	    			  if ( valid ) {
	    				  request.setAttribute("output", "{\"status\":\"ok\"}");
    					  return SUCCESS;
    				  }
	    		  }
	    		  if (getActionErrors().isEmpty()) {
	    			  addActionError("Failed to verify email address");
	    		  }
			      return ERROR; 
	    	  } else {
	    		  addActionError("Missing required parameters!");
			       return ERROR; 
	    	  }
	      }
}

