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
import javax.mail.internet.MimeUtility;
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
	           properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	           properties.put("mail.smtp.port", sslport);
	           properties.put("mail.smtp.ssl.enable", "true");
	           properties.put("mail.smtp.ssl.trust", "*");
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
	          
	          Message message = new MimeMessage(session);
	          message.setFrom(new InternetAddress(from, fromNick));
	          message.setRecipients(Message.RecipientType.TO, 
	             InternetAddress.parse(to)); //new InternetAddress(to, toNick)
	          message.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));
	          //message.setText(body);
	          message.setContent(body, "text/plain; charset=UTF-8");
	          message.setSentDate(new Date());
	          Transport.send(message);
	          String output = "{\"status\":\"Message sent to " + to + "\"}";
	          request.setAttribute("output", output);
	          return SUCCESS;
	       }
	       catch(Exception e)
	       {
	          logger.log(Level.SEVERE, e.getMessage(), e);
	          addActionError(e.getMessage());
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
	}
