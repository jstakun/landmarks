package net.gmsworld.server.struts;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
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
import net.gmsworld.server.utils.persistence.EMF;

public class DeviceAction extends ActionSupport implements ServletRequestAware {

	private HttpServletRequest request;
	private static final Logger logger = Logger.getLogger(DeviceAction.class.getName());
	private static final long serialVersionUID = 1L;
	
	private String imei;
    private Integer pin;
    private String token;
    private String username;
    private String command;
    private String name;
    private String args;
    private Long ttl;
    private String correlationId;
    
    private DevicePersistenceUtils getDevicePersistenceUtils() throws Exception {
    	return (DevicePersistenceUtils) ServiceLocator.getInstance().getService("bean/DevicePersistenceUtils");	
    }
    
	public String createDevice() {
		String result;
		if (imei != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				if (name != null) {
					name = name.replace(" ", "-");
				}
				Device device = new Device(imei, token, username, name) ;
				getDevicePersistenceUtils().save(device, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	//can update only pin, token, name and username
	public String updateDevice() {
		String result;
		if (imei != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Device device = getDevicePersistenceUtils().findDeviceByImei(imei, em);
				if (token != null) {
					device.setToken(token);
				}
				if (username != null) {
					device.setUsername(username);
				}
				if (name != null) {
					device.setName(name.replace(" ", "-"));
				}
				device.setCreationDate(new Date());
				getDevicePersistenceUtils().update(device, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	public String getDevice() {
		if (imei != null) {
			return getDeviceByImei();
		} else if (name != null && username != null) {
			return getDeviceByName();
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	private String getDeviceByImei() {
		String result;
		if (imei != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Device device = getDevicePersistenceUtils().findDeviceByImei(imei, em);
				if (device  != null) {
					request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
					result = "json";
				} else {
					addActionError("No device found!");
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	private String getDeviceByName() {
		String result;
		if (name != null && username != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Device device = getDevicePersistenceUtils().findDeviceByNameAndUsername(name, username, em);
				if (device  != null) {
					request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
					result = "json";
				} else {
					addActionError("No device found!");
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	public String createOrUpdateDevice() {
		String result = null;
		if (imei != null && !StringUtils.equalsIgnoreCase(token, "BLACKLISTED")) {
			EntityManager em = EMF.getEntityManager();
			try {
				DevicePersistenceUtils devicePersistenceUtils =  getDevicePersistenceUtils();			    
				Device device = devicePersistenceUtils.findDeviceByImei(imei, em);
				if (device  != null) {
				    logger.log(Level.INFO, "Updating existing device " + device.getImei());
					if (StringUtils.isNotBlank(token)) {
						device.setToken(token);
					}
					if (username != null) {
						device.setUsername(username);
					}
					if (name != null) {
						device.setName(name.replace(" ", "-"));
					}
					device.setCreationDate(new Date());
					devicePersistenceUtils.update(device, em);
				} else {
					device = devicePersistenceUtils.findDeviceByImei(imei, em);
					if (device != null && StringUtils.isNotBlank(token) ) {
						logger.log(Level.INFO, "Updating existing device " + device.getImei() + " which has not been used for some time");
						device.setToken(token);
						if (username != null) {
							device.setUsername(username);
						}
						if (name != null) {
							device.setName(name.replace(" ", "-"));
						}
						device.setCreationDate(new Date());
						devicePersistenceUtils.update(device, em);
					} else if (device != null) {
						addActionError("Invalid device " + imei + " update!");
				    	result = ERROR;
					} else {
						//create new device
						if (name != null) {
							name = name.replace(" ", "-");
						}
						device = new Device(imei, token, username, name) ;
						devicePersistenceUtils.save(device, em);
					}
				}
				if (result == null) {
					request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
					result = "json";
				} 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing or invalid required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	public String commandDevice() {
		String result;
		if ((imei != null  || (name != null && username != null)) && pin != null && command != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Device device = null;
				if (imei != null) {
					device = getDevicePersistenceUtils().findDeviceByImei(imei, em);
				} else if (name != null && username != null) {
					device = getDevicePersistenceUtils().findDeviceByNameAndUsername(name, username, em);
				}
				if (device  != null) {
					String url = "https://fcm.googleapis.com/v1/projects/" + Commons.getProperty(Property.FCM_PROJECT) + "/messages:send";
					String pinString;
					if (pin < 1000) {
						pinString = String.format ("%04d", pin);
					} else {
						pinString = Integer.toString(pin);
					}
					JSONObject data = new JSONObject().put("command", command).put("pin",  pinString);
					if (StringUtils.isNotEmpty(args)) {
						data.put("args", args);
					}
					if (StringUtils.isNotEmpty(correlationId)) {
						data.put("correlationId", correlationId);
					}
					if (ttl == null || ttl < 0) {
						ttl = 300L; //defaults to 300 seconds
					}
					JSONObject android = new JSONObject().put("ttl", Long.toString(ttl) + "s").put("priority","high");
					JSONObject webpush = new JSONObject().put("headers", new JSONObject().put("TTL", Long.toString(ttl)).put("Urgency","high"));
					JSONObject apns = new JSONObject().put("headers", new JSONObject().put("apns-expiration", Long.toString((((System.currentTimeMillis() + (ttl*1000L))/1000)))).put("apns-priority","10"));
					JSONObject content = new JSONObject().put("message", new JSONObject().put("token", device.getToken()).put("data", data).put("android", android).put("webpush", webpush).put("apns", apns));
					
					String auth  = "Bearer " + getAccessToken();
					String body = content.toString();
					
					if (System.getenv("FCM_DEBUG") != null) {
						logger.log(Level.INFO, "Sending: " + body);
						logger.log(Level.INFO, "To: " + url);
						logger.log(Level.INFO, "Auth: " + auth);
					}
					
					String response = HttpUtils.processFileRequestWithOtherAuthn(new URL(url), "POST", "application/json", body, "application/json", auth);
					
					if (System.getenv("FCM_DEBUG") != null) {
						logger.log(Level.INFO, "Received following response: " + response);
					}
					
					if (StringUtils.startsWith(response, "{")) {
						request.setAttribute("output", response);
						result = SUCCESS;
					} else {
						addActionError("Failed to send command. Try again later!");
				    	result = ERROR;
					}
				} else {
					addActionError("No device found!");
			    	result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Internal error: " + e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
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

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
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

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
}
