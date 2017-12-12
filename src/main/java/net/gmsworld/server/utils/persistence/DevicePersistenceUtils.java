package net.gmsworld.server.utils.persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Device;

@Stateless
public class DevicePersistenceUtils {

	@PersistenceContext
    private EntityManager entityManager;
	
	private final Logger logger = Logger.getLogger(DevicePersistenceUtils.class.getName());
			
	public void save(Device device) {
		entityManager.persist(device);
		entityManager.flush();
	}
	
	public void update(Device device) {
		entityManager.merge(device);
		entityManager.flush();
	}
	
	public Device findDeviceByImeiAndPin(Long imei, Integer pin) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_IMEI_AND_PIN, Device.class);
		query.setParameter("imei", imei);
		query.setParameter("pin", pin);
		Device d = null;
		try {
			d= query.getSingleResult();
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No device found with imei {0}", imei);
		}					
	    return d;
	}
	
	public Device findDeviceByImei(Long imei) {
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
}
