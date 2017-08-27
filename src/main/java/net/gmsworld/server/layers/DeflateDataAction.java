package net.gmsworld.server.layers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.opensymphony.xwork2.ActionSupport;

public class DeflateDataAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {
	
	private static final Logger logger = Logger.getLogger(DeflateDataAction.class.getName());
	private Object output;
	private HttpServletRequest request;
	private HttpServletResponse response;
	public static final String DEFLATE_OUTPUT = "DEFLATE_OUTPUT";
	private static final long serialVersionUID = 1L;

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
		
	}
	
	public String execute() {
		output = request.getAttribute(DEFLATE_OUTPUT);
		response.setContentType("deflate");
		
		try {
	    	response.setContentType("deflate");
	    	LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).serialize((List<ExtendedLandmark>)output, response.getOutputStream(), 15);
	    } catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    		addActionError(e.getMessage());
    		return "error";
    	} 	
		
		return null;
	}
	
	public Object getOutput() {
		return output;
	}
}
