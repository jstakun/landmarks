package net.gmsworld.server.utils.persistence;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Device;

public class DevicePersistenceUtils {

	private EntityManager entityManager  = Persistence.createEntityManagerFactory("landmarksdb").createEntityManager();
	
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
	
	public Device findDeviceByNameAndUsername(String name, String username, Integer pin) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_NAME_AND_USERNAME, Device.class);
		query.setParameter("name", name);
		query.setParameter("username", username);
		query.setParameter("pin", pin);
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
	
	
	/*public Device findDeviceByImei(Long imei) {
		TypedQuery<Device> query = entityManager.createNamedQuery(Device.FIND_BY_IMEI, Device.class);
		query.setParameter("imei", imei);
		Device d = null;
		try {
			d= query.getSingleResult();
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No device found with imei {0}", imei);
		}					
	    return d;
	}*/
}
