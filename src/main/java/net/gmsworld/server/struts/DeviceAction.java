package net.gmsworld.server.struts;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.persistence.Device;
import net.gmsworld.server.utils.persistence.DevicePersistenceUtils;

public class DeviceAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	 @Inject
	 private DevicePersistenceUtils devicePersistenceUtils;
	 private Map<String, String[]> parameters;
	 private HttpServletRequest request;
	private static final Logger logger = Logger.getLogger(DeviceAction.class.getName());
	 private static final long serialVersionUID = 1L;
			
	@Override
	public void setParameters(Map<String, String[]> arg0) {
		this.parameters = arg0;
	}
	
	private String getParameter(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key)[0];
		} else {
			return null;
		}
	}
	
	public String createDevice() {
		if (getParameter("imei") != null && getParameter("pin") != null) {
			try {
				Long imei = Long.valueOf(getParameter("imei"));
				String token = getParameter("token");
				Integer pin = Integer.valueOf(getParameter("pin") );
				String username = getParameter("username");
				Device device = new Device(imei, token, pin, username) ;
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
	
	public String updateDevice() {
		if (getParameter("imei") != null) {
			try {
				Long imei = Long.valueOf(getParameter("imei"));
				String token = getParameter("token");
				Integer pin = Integer.valueOf(getParameter("pin") );
				Device device = devicePersistenceUtils.findDeviceByImei(imei);
				if (token != null) {
					device.setToken(token);
				}
				if (pin != null) {
					device.setPin(pin);
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

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = arg0;
	}
}
