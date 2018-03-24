package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Layer;

public class LayerPersistenceUtils {
	
	public void save(Layer l) {
		EMF.save(l);
	}
	
	public List<Layer> findAll() {
		TypedQuery<Layer> query = EMF.getEntityManager().createNamedQuery(Layer.FIND_ALL, Layer.class);
        return query.getResultList(); 
    }
	
	public Layer findByName(String name) {
		return EMF.getEntityManager().find(Layer.class, name);
    }
}
