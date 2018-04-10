package net.gmsworld.server.struts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.persistence.Layer;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.LayerPersistenceUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;

public class LayerFormAction extends ActionSupport implements ServletRequestAware, Preparable {
	private static final Logger logger = Logger.getLogger(LayerFormAction.class.getName());
	private static final long serialVersionUID = 1L;
    private HttpServletRequest request;
    private LayerForm layerForm;
    private Map<String, String> layers;
	
    @Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
    
    public String populate() {
    	return "populate";
    }
    
    public String execute() {
    	try {
    		request.setAttribute("layerForm", BeanUtils.describe(layerForm));
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	return SUCCESS;
    }
    
    public void validate() {
    	if (layerForm != null) {
    		if (StringUtils.isEmpty(layerForm.getLayer())) {
    			addFieldError("layerForm.layer", "Layer is required!");
    		}
    	
    		try {
    			double l = Double.parseDouble(layerForm.getLatitude());
    			if (l > 90.0 || l < -90.0) {
    				addFieldError("layerForm.latitude", "Latitude must be in [-90, 90]!");
    			}
    		} catch (Exception e) {
    			addFieldError("layerForm.latitude", "Wrong latitude format!");
    		}
    	
    		try {
    			double l = Double.parseDouble(layerForm.getLongitude());
    			if (l > 180.0 || l < -180.0) {
    				addFieldError("layerForm.longitude", "Longitude must be in [-180, 180]!");
    			}
    		} catch (Exception e) {
    			addFieldError("layerForm.longitude", "Wrong longitude format!");
    		}
    	
    		if (StringUtils.isEmpty(layerForm.getFormat())) {
    			addFieldError("layerForm.format", "Format is required!");
    		}
    	
    		try {
    			int i = Integer.parseInt(layerForm.getRadius());
    			if (i > 100 || i < 1) {
    				addFieldError("layerForm.radius", "Radius must be in [1, 100]!");
    			}
    		} catch (Exception e) {
    			addFieldError("layerForm.radius", "Wrong radius format!");
    		}
    	
    		try {
    			int i = Integer.parseInt(layerForm.getLimit());
    			if (i > 100 || i < 1) {
    				addFieldError("layerForm.limit", "Limit must be in [1, 100]!");
    			}
    		} catch (Exception e) {
    			addFieldError("layerForm.limit", "Wrong limit format!");
    		}
    	}
    }

	public LayerForm getLayerForm() {
		return layerForm;
	}

	public void setLayerForm(LayerForm layerForm) {
		this.layerForm = layerForm;
	}
	
	public Map<String, String> getLayers() {
		return layers;
	}

	public void setLayers(Map<String, String> layers) {
		this.layers = layers;
	}
	
	public String getDefaultLayer() {
		return Commons.FACEBOOK_LAYER;
	}
	
	public String getDefaultDisplay() {
		return "xl";
	}
	
	public String getDefaultFormat() {
		return "json";
	}

	@Override
	public void prepare() throws Exception {
		if (layers == null) {
    		layers = new HashMap<String, String>();
    		for (String layer : LayerHelperFactory.getInstance().getEnabledLayers()) {
    			layers.put(layer, layer);
    		}
    		EntityManager em = EMF.getEntityManager();
    		try {
				LayerPersistenceUtils layerPeristenceUtils = (LayerPersistenceUtils) ServiceLocator.getInstance().getService("bean/LayerPersistenceUtils");
				List<Layer> gmsLayers = layerPeristenceUtils.findAll(em);
				for (Layer l : gmsLayers) {
					layers.put(l.getName(), l.getFormatted());
				}
			} catch (Exception e) {
			} finally {
				em.close();
			}
		}		
	}
}
