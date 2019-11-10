package net.gmsworld.server.utils.persistence;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Device;
import net.gmsworld.server.persistence.Route;

public class RoutePersistenceUtils {

	public Route persist(String name, String route, EntityManager entityManager) {
		Route r = new Route(name, route);
		EMF.save(r, entityManager);
		return r;
	}
	
	public Route findByName(String name, EntityManager entityManager) {
		try {
			TypedQuery<Route> query = entityManager.createNamedQuery(Route.FIND_BY_NAME, Route.class);
			query.setParameter("name", name);
			return query.getSingleResult();
		} catch (NoResultException ne) {
			return null;
		}
    }
	
	public void update(Route route, EntityManager entityManager) {
		route.setCreationDate(new Date(System.currentTimeMillis()));
		EMF.update(route, entityManager);
	}
}
