package net.gmsworld.server.struts;

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

	    static Properties properties = new Properties();
	    static
	    {
	       String host = System.getenv("SMTP_HOST");
	       String port = System.getenv("SMTP_PORT");	
	       Logger.getLogger("MailAction").log(Level.INFO, "Mail agent will connect to " + host + ":" + port);
	       properties.put("mail.smtp.host", host);
	       //properties.put("mail.smtp.socketFactory.port", port);
	       //properties.put("mail.smtp.socketFactory.class",
	       //             "javax.net.ssl.SSLSocketFactory");
	       properties.put("mail.smtp.auth", "true");
	       properties.put("mail.smtp.port", port);
	    }
	    
	    @Override
		public void setServletRequest(HttpServletRequest request) {
			this.request = request;
			
		}

	    public String execute() 
	    {
	       try
	       {
	          Session session = Session.getInstance(properties,  
	             new javax.mail.Authenticator() {
	              protected PasswordAuthentication 
	              getPasswordAuthentication() {
	              return new 
	              PasswordAuthentication(from, password);
	             }});
	          
	          session.setDebug(true);

	          Message message = new MimeMessage(session);
	          message.setFrom(new InternetAddress(from));
	          message.setRecipients(Message.RecipientType.TO, 
	             InternetAddress.parse(to));
	          message.setSubject(subject);
	          message.setText(body);
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
	}
