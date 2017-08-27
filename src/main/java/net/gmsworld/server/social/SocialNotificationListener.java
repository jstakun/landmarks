package net.gmsworld.server.social;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@MessageDriven(name = "SocialNotificationMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/GMSNotificationQueue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
	}
)

public class SocialNotificationListener implements MessageListener {

	private static final Logger logger = Logger.getLogger(SocialNotificationListener.class.getName());
	
	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			logger.log(Level.INFO, "Received Social Notification Request: {0}", textMessage);
		} else {
			logger.log(Level.INFO, "Received Social Notification Request: {0}", message.toString());
		}		
	}
}
