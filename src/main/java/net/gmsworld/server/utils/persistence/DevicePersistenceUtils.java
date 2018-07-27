package net.gmsworld.server.utils.persistence;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Device;

public class DevicePersistenceUtils {

	private final Logger logger = Logger.getLogger(DevicePersistenceUtils.class.getName());
	
	public void save(Device device, EntityManager entityManager) {
		EMF.save(device, entityManager);	
	}
	
	public void update(Device device, EntityManager entityManager) {
		EMF.update(device, entityManager);
	}
	
	public void remove(Device device, EntityManager entityManager) {
		EMF.remove(device, entityManager);
	}
	
	public Device findDeviceByNameAndUsername(String name, String username, EntityManager entityManager) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_NAME_AND_USERNAME, Device.class);
		query.setParameter("name", name);
		query.setParameter("username", username);
		Device d = null;
		try {
			List<Device> dl = query.getResultList();
			if (!dl.isEmpty()) {
				d = dl.get(0);
			}
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No device found with name {0}", name);
		}					
	    return d;
	}
	
	public Device findDeviceByImei(String imei, EntityManager entityManager) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_IMEI, Device.class);
		query.setParameter("imei", imei);
		Device d = null;
		try {
			d= query.getSingleResult();
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No device found with imei {0}", imei);
		}					
	    return d;
	}
	
	public List<Device> findDeviceByUsername(String username, int limit, EntityManager entityManager) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_USERNAME, Device.class);
		query.setParameter("username", username);
		query.setMaxResults(limit);
		List<Device> dl = null;
		try {
			dl = query.getResultList();
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No device found with username {0}", username);
		}					
	    return dl;
	}
}
