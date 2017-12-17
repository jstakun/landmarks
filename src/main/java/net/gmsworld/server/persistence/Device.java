package net.gmsworld.server.persistence;

import java.io.Serializable;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Device
 *
 */
@Entity
@Table(name = "DEVICE")

@NamedQueries({
	@NamedQuery(name = "Device.findByImei", query = "select d from Device d where d.imei = :imei"),
	@NamedQuery(name = "Device.findByImeiAndPin", query = "select d from Device d where d.imei = :imei and d.pin = :pin")
})

public class Device implements Serializable {

	 public final static String FIND_BY_IMEI= "Device.findByImei";
	 public final static String FIND_BY_IMEI_AND_PIN = "Device.findByImeiAndPin";
	
	 @Id
	 @Column(name = "IMEI")
	private Long imei;
	 @Column(name = "TOKEN")
	private String token;
	 @Column(name = "PIN")
	private Integer pin;
	 @Column(name = "USERNAME")
	private String username;
	 @Column(name = "CREATION_DATE")
	private Date creationDate;
	 @Column(name = "NAME")
	private String name;
	
	 private static final long serialVersionUID = 1L;

	public Device(Long imei, String token, Integer pin, String username, String name) {
		this();
		this.imei = imei;
		this.token = token;
		this.pin = pin;
		this.username = username;
		this.name = name;
	}
	
	public Device() {
		 this.creationDate = new Date(System.currentTimeMillis());
	}   
	public Long getImei() {
		return this.imei;
	}

	public void setImei(Long imei) {
		this.imei = imei;
	}   
	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}   
	public Integer getPin() {
		return this.pin;
	}

	public void setPin(Integer pin) {
		this.pin = pin;
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
}
