package net.gmsworld.server.struts;

import java.io.FileInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
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
    private String flex;  
    private Integer limit;
    
    private DevicePersistenceUtils getDevicePersistenceUtils() throws Exception {
    	return (DevicePersistenceUtils) ServiceLocator.getInstance().getService("bean/DevicePersistenceUtils");	
    }
    
    private boolean setGeo(Device device) {
    	if (StringUtils.isNotEmpty(flex)) {
			 String[] tokens = StringUtils.split(flex,",");
			 for (String token : tokens) {
				  if (StringUtils.startsWith(token, "geo:")) {
					   logger.log(Level.INFO, "Setting device geo location");
					   device.setGeo(token.substring(4) + " " + System.currentTimeMillis());
					   return true;
				  }
			 }
		}
    	return false;
    }
    
    private void setGeoFromFlex() {
    	 if (StringUtils.isNotEmpty(flex)) {
    	     String[] tokens = StringUtils.split(flex,",");
    	     String deviceId = null, geo = null;
			 for (String token : tokens) {
				  if (StringUtils.startsWith(token, "geo:")) {
					   geo = token.substring(4) + " " + System.currentTimeMillis();
				  } else if (StringUtils.startsWith(token, "deviceId:")) {
					   deviceId = token.substring(9);
				  }
				  if (StringUtils.isNotEmpty(geo) && StringUtils.isNotEmpty(deviceId)) {
					   EntityManager em = EMF.getEntityManager();
					   try {
							Device device = getDevicePersistenceUtils().findDeviceByImei(deviceId, em);
							if (device != null) {
							 	logger.log(Level.INFO, "Setting device geo location");
							 	device.setGeo(geo);
							 	getDevicePersistenceUtils().update(device, em);
							}
					   } catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);	 
					   } finally {
							em.close();
					   }
					   break;
				  } 
    		  }
    	 }
    }
    
	public String createDevice() {
		String result;
		if (imei != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				if (name != null) {
					name = name.replace(" ", "-").replace(",", "-");
				}
				Device device = new Device(imei, token, username, name) ;
				setGeo(device);
				getDevicePersistenceUtils().save(device, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: "  + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
					device.setName(name.replace(" ", "-").replace(",", "-"));
				}
				device.setUpdateDate(new Date());
				setGeo(device);
				getDevicePersistenceUtils().update(device, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: "  + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
			ServletActionContext.getResponse().setStatus(400);
	    	return ERROR;
		}
	}
	
	public String getUserDevices() {
		String result;
		if ( username != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				if (limit == null || limit <= 0 || limit > 100) {
					limit = 10;
				}
				List<Device> devices = getDevicePersistenceUtils().findDeviceByUsername(username, limit, em);
				if (devices  != null) {
					request.setAttribute(JSonDataAction.JSON_OUTPUT, devices);
					result = "json";
				} else {
					addActionError("No user " + username + " devices found!");
					ServletActionContext.getResponse().setStatus(404);
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				ServletActionContext.getResponse().setStatus(500);
				addActionError("User " + username + " devices error: " + e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
	    	result = ERROR;
		}
		return result;
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
					addActionError("Device with imei " + imei + " not found!");
					ServletActionContext.getResponse().setStatus(404);
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: " + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
					addActionError("Device with name " + name + " not found!");
					ServletActionContext.getResponse().setStatus(404);
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + name + " error: " + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
						device.setName(name.replace(" ", "-").replace(",", "-"));
					}
					device.setUpdateDate(new Date());
					setGeo(device);
					devicePersistenceUtils.update(device, em);
				} else {
				    //create new device
					if (name != null) {
						name = name.replace(" ", "-").replace(",", "-");
					}
					device = new Device(imei, token, username, name) ;
					setGeo(device);
					devicePersistenceUtils.save(device, em);
				}
				request.setAttribute(JSonDataAction.JSON_OUTPUT, device);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: " + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing or invalid required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
				if (device  != null && StringUtils.isNotEmpty(device.getToken())) {
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
					if (StringUtils.isNotEmpty(flex)) {
						data.put("flex", flex);
						setGeoFromFlex();
					}
					if (ttl == null || ttl < 0) {
						ttl = 300L; //defaults to 300 seconds
					}
					JSONObject android = new JSONObject().put("ttl", Long.toString(ttl) + "s").put("priority","high").put("collapseKey", device.getImei());
					JSONObject webpush = new JSONObject().put("headers", new JSONObject().put("TTL", Long.toString(ttl)).put("Urgency","high").put("Topic",device.getImei()));
					JSONObject apns = new JSONObject().put("headers", new JSONObject().put("apns-expiration", Long.toString((((System.currentTimeMillis() + (ttl*1000L))/1000)))).put("apns-priority","10").put("apns-collapse-id", device.getImei()));
					JSONObject content = new JSONObject().put("message", new JSONObject().put("token", device.getToken()).put("data", data).put("android", android).put("webpush", webpush).put("apns", apns));
					
					String auth  = "Bearer " + getAccessToken();
					String body = content.toString();
					
					if (System.getenv("FCM_DEBUG") != null) {
						logger.log(Level.INFO, "Sending: " + body);
						logger.log(Level.INFO, "To: " + url);
						logger.log(Level.INFO, "Auth: " + auth);
					}
					
					String response = HttpUtils.processFileRequestWithAuthn(new URL(url), "POST", "application/json", body, "application/json", auth);
					
					if (System.getenv("FCM_DEBUG") != null && response != null) {
						logger.log(Level.INFO, "Received following response: " + response);
					}
					Integer responseCode = HttpUtils.getResponseCode(url);
					if (StringUtils.startsWith(response, "{") && responseCode != null && responseCode == HttpServletResponse.SC_OK) {
						JSONObject responseJson = new JSONObject(response);
						if (responseJson.has("name")) {
							//check when device has been last seen
							int deviceLastSeen =  getDeviceLastSeen(device);
							if (deviceLastSeen <= 1) {
								request.setAttribute("output", response);
								result = SUCCESS;
							} else {
								addActionError("Device " + imei + " has been last seen " + deviceLastSeen + " days ago");
								ServletActionContext.getResponse().setStatus(410);
								result = ERROR;
							}
						} else {
							addActionError("Failed to send command to device " + imei + ". Try again later!");
							logger.log(Level.SEVERE, "Received invalid response " + response);
							result = ERROR;
						}
					} else if  (StringUtils.startsWith(response, "{") && responseCode != null && responseCode >= 400) {
						JSONObject responseJson = new JSONObject(response);
						if (responseJson.has("error")) {
							JSONObject error = responseJson.getJSONObject("error");
							addActionError("Device " + imei + " error: " + error.optString("message"));
						} else {
							addActionError("Failed to send command to device " + imei + ". Try again later!");
						}
						logger.log(Level.SEVERE, "Received error response " + response);
						ServletActionContext.getResponse().setStatus(responseCode);
						result = ERROR;
					} else {
						addActionError("Failed to send command to device " + imei + ". Try again later!");
						logger.log(Level.SEVERE, "Received invalid response " + response);
						ServletActionContext.getResponse().setStatus(500);
				    	result = ERROR;
					}
				} else {
					if (device != null && StringUtils.isEmpty(device.getToken())) {
						addActionError("Device " + device.getImei() + " has no saved token!");
						ServletActionContext.getResponse().setStatus(400);
					} else if (imei != null) {
						addActionError("Device with imei " + imei + " not found!");
						ServletActionContext.getResponse().setStatus(404);
					} else if (name != null) {
						addActionError("Device with name " + name + " not found!");
						ServletActionContext.getResponse().setStatus(404);
					}
			    	result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: " + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
	    	result = ERROR;
		}
		return result;
	}
	
	public String deleteDevice() {
		String result;
		if (imei != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Device device = getDevicePersistenceUtils().findDeviceByImei(imei, em);
				if (device  != null) {
					getDevicePersistenceUtils().remove(device, em);
					request.setAttribute("output", "{\"status\":\"ok\"}");
					result = SUCCESS; 
				} else {
					addActionError("Device with imei " + imei + " not found!");
					ServletActionContext.getResponse().setStatus(404);
					result = ERROR;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError("Device " + imei + " error: " + e.getMessage());
				ServletActionContext.getResponse().setStatus(500);
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
			ServletActionContext.getResponse().setStatus(400);
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
	
	private int getDeviceLastSeen(Device device) throws Exception {
		String response = HttpUtils.processFileRequestWithAuthn(new URL("https://iid.googleapis.com/iid/info/" + device.getToken() + "?details=true"), "GET", "application/json", null, "application/json", "key=" + Commons.getProperty(Property.FCM_APP_KEY));
		
		if  (StringUtils.startsWith(response, "{")) {
			if (System.getenv("FCM_DEBUG") != null) {
				logger.log(Level.INFO, "Received: " + response);
			}
			JSONObject responseJson = new JSONObject(response);
			String connectDate = responseJson.optString("connectDate");
			if (StringUtils.isNotEmpty(connectDate)) {
				Date deviceSeenDate = new SimpleDateFormat("yyyy-MM-dd").parse(connectDate);
				long diff = new Date().getTime() - deviceSeenDate.getTime();
			    long diffDays = diff / (24 * 60 * 60 * 1000);
			    return (int) diffDays;
			}
		} else {
			logger.log(Level.SEVERE, "Received following response: " + response);
		}
		return 0;
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
	
	public String getFlex() {
		return flex;
	}

	public void setFlex(String flex) {
		this.flex = flex;
	}
	
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}