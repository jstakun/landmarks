package net.gmsworld.server.struts;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.persistence.Device;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.persistence.DevicePersistenceUtils;

public class DeviceAction extends ActionSupport implements ServletRequestAware {

	private HttpServletRequest request;
	private static final Logger logger = Logger.getLogger(DeviceAction.class.getName());
	private static final long serialVersionUID = 1L;
	
	private Long imei;
    private Integer pin;
    private String token;
    private String username;
    private String command;
    private String name;
    private String args;
    
    
	public String createDevice() {
		if (imei != null && pin != null) {
			try {
				Device device = new Device(imei, token, pin, username, name) ;
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");			    
				devicePersistenceUtils.save(device);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	//can update only pin, token and username
	public String updateDevice() {
		if (imei != null && pin != null) {
			try {
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");
				Device device = devicePersistenceUtils.findDeviceByImeiAndPin(imei, pin);
				if (token != null) {
					device.setToken(token);
				}
				if (pin != null) {
					device.setPin(pin);
				}
				if (username != null) {
					device.setUsername(username);
				}
				if (name != null) {
					device.setName(name);
				}
				devicePersistenceUtils.update(device);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	public String getDevice() {
		if (imei != null && pin != null) {
			try {
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");			    
				Device device = devicePersistenceUtils.findDeviceByImeiAndPin(imei, pin);
				if (device  != null) {
					request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
					return "json";
				} else {
					addActionError("No device found!");
					return ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	public String createOrUpdateDevice() {
		if (imei != null && pin != null) {
			try {
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");			    
				Device device = devicePersistenceUtils.findDeviceByImei(imei);
				if (device  != null) {
					if (token != null) {
						device.setToken(token);
					}
					if (pin != null) {
						device.setPin(pin);
					}
					if (username != null) {
						device.setUsername(username);
					}
					if (name != null) {
						device.setName(name);
					}
					devicePersistenceUtils.update(device);
				} else {
					device = new Device(imei, token, pin, username, name) ;
					devicePersistenceUtils.save(device);
				}
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	public String commandDevice() {
		if (imei != null && pin != null && command != null) {
			try {
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");			    
				Device device = devicePersistenceUtils.findDeviceByImeiAndPin(imei, pin);
				if (device  != null) {
					String url = "https://fcm.googleapis.com/v1/projects/" + Commons.getProperty(Property.FCM_PROJECT) + "/messages:send";
					String data = "{\"message\":{\"token\":\"" + token + "\",\"data\":{\"command\": \"" + command + "\",\"pin\":\"" + pin + "\",\"imei\":\"" + imei + "\"";
					if (StringUtils.isNotEmpty(args)) {
						data  += ",\"args\":\"" + args + "\"";
					}
					data += "}}}";
					logger.log(Level.INFO, "Sending: " + data);
					logger.log(Level.INFO, "To: " + url);
				    String response = HttpUtils.processFileRequestWithOtherAuthn(new URL(url), "POST", "application/json", data, "application/json", "Bearer " + getAccessToken());
					logger.log(Level.INFO, "Received following response: " + response);
					if (StringUtils.startsWith(response, "{")) {
						request.setAttribute("output", response);
						return SUCCESS;
					} else {
						addActionError("Failed to send command. Try again later!");
				    	return ERROR;
					}
				} else {
					addActionError("No device found!");
			    	return ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Internal error: " + e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}

	private static String getAccessToken() throws Exception {
		  String fcmConfig = System.getenv("FCM_CONFIG");
		  if (fcmConfig == null) {
			  throw new Exception("Please set environment variable FCM_CONFIG!");
		  } 
		  FileInputStream is = new FileInputStream(fcmConfig);
		  if (is.available() <= 0) {
			  try {
				  is.close();
			  } catch (Exception e) {}
			  throw new Exception("Unable to open " + fcmConfig);
		  } else {
			  logger.log(Level.INFO, fcmConfig + " found");
		  }
		  GoogleCredential googleCredential = GoogleCredential.fromStream(is).createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
		  googleCredential.refreshToken();
		  return googleCredential.getAccessToken();
	}
	
	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = arg0;
	}

	public Long getImei() {
		return imei;
	}

	public void setImei(Long imei) {
		this.imei = imei;
	}

	public Integer getPin() {
		return pin;
	}

	public void setPin(Integer pin) {
		this.pin = pin;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}
}
