package net.gmsworld.server.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

public class JSonDataAction extends ActionSupport implements ServletRequestAware {
	
	private Object output;
	private HttpServletRequest request;
	public static final String JSON_OUTPUT = "JSON_OUTPUT";
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	private static final long serialVersionUID = 1L;

	public String execute() {
		output = request.getAttribute(JSON_OUTPUT);
		return SUCCESS;
	}
	
	public Object getOutput() {
		return output;
	}

}
