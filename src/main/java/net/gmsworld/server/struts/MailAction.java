package net.gmsworld.server.struts;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

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
	    
	    static Properties properties = new Properties();
	    static
	    {
	       String host = System.getenv("SMTP_HOST");
	       if (StringUtils.isEmpty(host)) {
	    	   host = "localhost";
	       }
	       String port = System.getenv("SMTP_PORT");
	       if (StringUtils.isEmpty(port)) {
	    	   port = "25";
	       }
	       properties.put("mail.smtp.host", host);
	       String sslport = System.getenv("SMTP_SSL_PORT");
	       if (StringUtils.isNotEmpty(sslport)) {
	    	   Logger.getLogger("MailAction").log(Level.INFO, "Mail agent will connect to " + host + ":" + sslport);
	    	   properties.put("mail.smtp.socketFactory.port", sslport);
	           //properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	           properties.put("mail.smtp.port", sslport);
	           properties.put("mail.smtp.ssl.enable", "true");
	           properties.put("mail.smtp.ssl.trust", host);
	       } else {
	    	   Logger.getLogger("MailAction").log(Level.INFO, "Mail agent will connect to " + host + ":" + port);
	    	   properties.put("mail.smtp.port", port);
	       }
	       properties.put("mail.smtp.auth", "true");      
	    }
	    
	    @Override
		public void setServletRequest(HttpServletRequest request) {
			this.request = request;
		}

	    public String execute() 
	    {
	       if (StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(to) && (StringUtils.isNotEmpty(body) || StringUtils.isNotEmpty(subject))) {	    	
	    	   try
	    	   {
	    		   Session session = Session.getDefaultInstance(properties,  
	    				   new javax.mail.Authenticator() {
	    			   protected PasswordAuthentication 
	    			   getPasswordAuthentication() {
	    				   return new 
	    						   PasswordAuthentication(from, password);
	    			   }});
	    		 
	    		   String debug = System.getenv("SMTP_DEBUG");
	    		   if (StringUtils.equalsIgnoreCase(debug, "true")) {
	    			   session.setDebug(true);
	    		   } else {
	    			   session.setDebug(false);
	    		   }
	          
	    		   MimeMessage message = new MimeMessage(session);
	          
	    		   message.setFrom(new InternetAddress(from, fromNick));
	          
	    		   if (StringUtils.isNotEmpty(to) && StringUtils.isNotEmpty(toNick)) {
	    			   message.setRecipient(Message.RecipientType.TO, new InternetAddress(to, toNick));
	    		   } else {
	    			   message.setRecipients(Message.RecipientType.TO,  InternetAddress.parse(to)); 
	    		   }
	          
	    		   if (StringUtils.isNotEmpty(cc) && StringUtils.isNotEmpty(ccNick)) {
	    			   message.setRecipient(Message.RecipientType.CC, new InternetAddress(cc, ccNick));
	    		   } else if (StringUtils.isNotEmpty(cc)) {
	    			   message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));  
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
	         
	    		   Transport.send(message);
	    		   String output = "{\"status\":\"Message " + message.getMessageID() + " sent to " + to + "\"}";
	    		   logger.log(Level.INFO, "Message " + message.getMessageID() + " sent to " + to);
	    		   request.setAttribute("output", output);
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

	    public static Properties getProperties() {
	       return properties;
	    }

	    public static void setProperties(Properties properties) {
	     MailAction.properties = properties;
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
	}
