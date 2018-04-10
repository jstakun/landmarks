package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Layer;

public class LayerPersistenceUtils {
	
	public void save(Layer l, EntityManager entityManager) {
		EMF.save(l, entityManager);
	}
	
	public List<Layer> findAll(EntityManager entityManager) {
		TypedQuery<Layer> query = entityManager.createNamedQuery(Layer.FIND_ALL, Layer.class);
        return query.getResultList(); 
    }
	
	public Layer findByName(String name, EntityManager entityManager) {
		return entityManager.find(Layer.class, name);
    }
}
