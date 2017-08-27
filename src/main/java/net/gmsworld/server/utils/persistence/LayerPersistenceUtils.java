package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Layer;

@Stateless
public class LayerPersistenceUtils {
	@PersistenceContext
    private EntityManager entityManager;
	
	public void save(Layer l) {
		entityManager.persist(l);
		entityManager.flush();
	}
	
	public List<Layer> findAll() {
		TypedQuery<Layer> query = entityManager.createNamedQuery(Layer.FIND_ALL, Layer.class);
        return query.getResultList(); 
    }
	
	public Layer findByName(String name) {
		return entityManager.find(Layer.class, name);
    }
}
