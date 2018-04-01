package net.gmsworld.server.struts;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;

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
    private Long ttl;
    private Integer oldPin;
    
    private DevicePersistenceUtils getDevicePersistenceUtils() throws Exception {
    	return (DevicePersistenceUtils) ServiceLocator.getInstance().getService("bean/DevicePersistenceUtils");	
    }
    
	public String createDevice() {
		if (imei != null && pin != null) {
			try {
				if (name != null) {
					name = name.replace(" ", "-");
				}
				Device device = new Device(imei, token, pin, username, name) ;
				getDevicePersistenceUtils().save(device);
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
	
	//can update only pin, token, name and username
	public String updateDevice() {
		if (imei != null && pin != null) {
			try {
				Device device = null;
				if (oldPin != null) {
					device = getDevicePersistenceUtils().findDeviceByImeiAndPin(imei, oldPin);
				} else {
					device = getDevicePersistenceUtils().findDeviceByImeiAndPin(imei, pin);
				}
				if (token != null) {
					device.setToken(token);
				}
				if (oldPin != null && oldPin == device.getPin()) {
					device.setPin(pin);
				}
				if (username != null) {
					device.setUsername(username);
				}
				if (name != null) {
					device.setName(name.replace(" ", "-"));
				}
				device.setCreationDate(new Date());
				getDevicePersistenceUtils().update(device);
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
			return getDeviceByImei();
		} else if (name != null && username != null  && pin != null) {
			return getDeviceByName();
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	private String getDeviceByImei() {
		if (imei != null && pin != null) {
			try {
				Device device = getDevicePersistenceUtils().findDeviceByImeiAndPin(imei, pin);
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
	
	private String getDeviceByName() {
		if (name != null && username != null && pin != null) {
			try {
				Device device = getDevicePersistenceUtils().findDeviceByNameAndUsername(name, username, pin);
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
		if (imei != null && pin != null && pin >= 1000 && !StringUtils.equalsIgnoreCase(token, "BLACKLISTED")) {
			try {
				DevicePersistenceUtils devicePersistenceUtils = (DevicePersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/DevicePersistenceUtils!net.gmsworld.server.utils.persistence.DevicePersistenceUtils");			    
				Device device = null;
				if (oldPin != null) {
					device = devicePersistenceUtils.findDeviceByImeiAndPin(imei, oldPin);
				} else {
					device = devicePersistenceUtils.findDeviceByImeiAndPin(imei, pin);
				}
				if (device  != null) {
					//update existing device
					if (token != null) {
						device.setToken(token);
					}
					if (oldPin != null && oldPin == device.getPin()) {
						device.setPin(pin);
					}
					if (username != null) {
						device.setUsername(username);
					}
					if (name != null) {
						device.setName(name.replace(" ", "-"));
					}
					device.setCreationDate(new Date());
					devicePersistenceUtils.update(device);
				} else {
					device = devicePersistenceUtils.findDeviceByImei(imei);
					if (device != null && token != null) {
						//update existing device which has not been used for some time
						device.setToken(token);
						device.setPin(pin);
						if (username != null) {
							device.setUsername(username);
						}
						if (name != null) {
							device.setName(name.replace(" ", "-"));
						}
						device.setCreationDate(new Date());
						devicePersistenceUtils.update(device);
					} else if (device != null) {
						addActionError("Invalid device " + imei + " update!");
				    	return ERROR;
					} else {
						//create new device
						if (name != null) {
							name = name.replace(" ", "-");
						}
						device = new Device(imei, token, pin, username, name) ;
						devicePersistenceUtils.save(device);
					}
				}
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing or invalid required parameter!");
	    	return ERROR;
		}
	}
	
	public String commandDevice() {
		if ((imei != null  || (name != null && username != null)) && pin != null && command != null) {
			try {
				Device device = null;
				if (imei != null) {
					device = getDevicePersistenceUtils().findDeviceByImeiAndPin(imei, pin);
				} else if (name != null && username != null) {
					device = getDevicePersistenceUtils().findDeviceByNameAndUsername(name, username, pin);
				}
				if (device  != null) {
					String url = "https://fcm.googleapis.com/v1/projects/" + Commons.getProperty(Property.FCM_PROJECT) + "/messages:send";
					JSONObject data = new JSONObject().put("command", command).put("pin",  Integer.toString(pin));
					if (StringUtils.isNotEmpty(args)) {
						data .put("args", args);
					}
					if (ttl == null || ttl < 0) {
						ttl = 300L; //defaults to 300 seconds
					}
					JSONObject android = new JSONObject().put("ttl", Long.toString(ttl) + "s").put("priority","high");
					JSONObject webpush = new JSONObject().put("headers", new JSONObject().put("TTL", Long.toString(ttl)).put("Urgency","high"));
					JSONObject apns = new JSONObject().put("headers", new JSONObject().put("apns-expiration", Long.toString((((System.currentTimeMillis() + (ttl*1000L))/1000)))).put("apns-priority","10"));
					JSONObject content = new JSONObject().put("message", new JSONObject().put("token", device.getToken()).put("data", data).put("android", android).put("webpush", webpush).put("apns", apns));
					
					//logger.log(Level.INFO, "Sending: " + content.toString());
					//logger.log(Level.INFO, "To: " + url);
				    String response = HttpUtils.processFileRequestWithOtherAuthn(new URL(url), "POST", "application/json", content.toString(), "application/json", "Bearer " + getAccessToken());
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

	private String getAccessToken() throws Exception {
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

	public Long getTtl() {
		return ttl;
	}

	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}

	public Integer getOldPin() {
		return oldPin;
	}

	public void setOldPin(Integer oldPin) {
		this.oldPin = oldPin;
	}
}
