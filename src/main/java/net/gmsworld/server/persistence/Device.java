package net.gmsworld.server.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Device
 *
 */
@Entity
@Table(name = "DEVICE")

@NamedQueries({
	@NamedQuery(name = "Device.findByImei", query = "select d from Device d where d.imei = :imei order by d.creationDate desc"),
	@NamedQuery(name = "Device.findByNameAndUsername", query = "select d from Device d where d.name = :name and d.username = :username order by d.creationDate desc"),
	@NamedQuery(name = "Device.findByUsername", query = "select d from Device d where d.username = :username order by d.creationDate desc"),
})

public class Device implements Serializable {

	public final static String FIND_BY_IMEI= "Device.findByImei";
	public final static String FIND_BY_NAME_AND_USERNAME = "Device.findByNameAndUsername";
	public final static String FIND_BY_USERNAME = "Device.findByUsername";
	
	@Id
	@Column(name = "IMEI")
	private String imei;
	@Column(name = "TOKEN")
	private String token;
	@Column(name = "USERNAME")
	private String username;
	@Column(name = "CREATION_DATE")
	private Date creationDate;
	@Column(name = "NAME")
	private String name;
	@Column(name = "GEO")
	private String geo;
	@Column(name = "UPDATE_DATE")
	private Date updateDate;
	
	private static final long serialVersionUID = 1L;

	public Device(String imei, String token, String username, String name) {
		this();
		this.imei = imei;
		this.token = token;
		this.username = username;
		this.name = name;
	}
	
	public Device() {
		Date now = new Date(System.currentTimeMillis());
		 this.creationDate = now;
		 this.updateDate = now;
	}   
	
	public String getImei() {
		return this.imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}   
	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}   
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}   
	
	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
    
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeo() {
		return geo;
	}

	public void setGeo(String geo) {
		this.geo = geo;
	}
	
	//public Date getUpdateDate() {
	//	return this.updateDate;
	//}

	//public void setUpdateDate(Date updateDate) {
	//	this.updateDate = updateDate;
	//}
}
