package net.gmsworld.server.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "SCREENSHOT")
@SequenceGenerator(name="screenshot_id_seq", allocationSize=1) 

public class Screenshot implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "SCREENSHOT_ID") 
    @GeneratedValue(generator = "screenshot_id_seq", strategy = GenerationType.SEQUENCE)
    private int screenshotId;
	@Column(name = "CREATION_DATE") 
    private Date creationDate;
	@Column(name = "LATITUDE")     
    private double latitude;
	@Column(name = "LONGITUDE") 
    private double longitude;
	@Column(name = "USERNAME") 
    private String username;
	@Column(name = "FILENAME") 
    private String filename;
	@Column(name = "STORAGE_ID") 
    private int storageId;
	
	public Screenshot() {
		this.creationDate = new Date(System.currentTimeMillis());
	}
	
	public Screenshot(String filename, double latitude, double longitude, String username, int storageId) {
		this();
		this.filename = filename;
		this.latitude = latitude;
		this.longitude = longitude;
		this.username = username;
		this.storageId = storageId;		
	}
	
	public int getId() {
		return screenshotId;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getStorageId() {
		return storageId;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
}
