package net.gmsworld.server.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "LANDMARK")
@SequenceGenerator(name="landmark_id_seq", allocationSize=1)

@NamedQueries({
	@NamedQuery(name = "Landmark.findNewest", query = "select l from Landmark l order by l.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findByHash", query = "select l from Landmark l where l.hash = :hash", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findByUserAndLayer", query = "select l from Landmark l where l.username = :username and l.layer = :layer order by l.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.countByUserAndLayer", query = "select count(l) from Landmark l where l.username = :username and l.layer = :layer", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findByUser", query = "select l from Landmark l where l.username = :username order by l.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.countByUser", query = "select count(l) from Landmark l where l.username = :username", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findByLayer", query = "select l from Landmark l where l.layer = :layer order by l.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.countByLayer", query = "select count(l) from Landmark l where l.layer = :layer", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findByYearMonth", query = "select l from Landmark l where year(l.creationDate) = :year and month(l.creationDate) = :month order by l.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.countByYearMonth", query = "select count(l) from Landmark l where year(l.creationDate) = :year and month(l.creationDate) = :month", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Landmark.findNewer", query = "select l from Landmark l where l.creationDate > :date", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

public class Landmark implements Serializable {
	
	private static final long serialVersionUID = 1L;
    
	public final static String FIND_NEWEST = "Landmark.findNewest";
	public final static String FIND_BY_HASH = "Landmark.findByHash";
	public final static String FIND_USER_LAYER = "Landmark.findByUserAndLayer";
	public final static String COUNT_USER_LAYER = "Landmark.countByUserAndLayer";
	public final static String FIND_USER = "Landmark.findByUser";
	public final static String COUNT_USER = "Landmark.countByUser";
	public final static String FIND_LAYER = "Landmark.findByLayer";
	public final static String COUNT_LAYER = "Landmark.countByLayer";
	public final static String FIND_YEAR_MONTH = "Landmark.findByYearMonth";
	public final static String COUNT_YEAR_MONTH = "Landmark.countByYearMonth";
	public final static String FIND_NEWER = "Landmark.findNewer";
	
	@Id
    @GeneratedValue(generator = "landmark_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "LANDMARK_ID")
    private int landmarkId;
    @Column(name = "LATITUDE")     
    private double latitude;
	@Column(name = "LONGITUDE") 
    private double longitude;
	@Column(name = "ALTITUDE") 
    private Double altitude;
	@Column(name = "NAME") 
    private String name;
	@Column(name = "DESCRIPTION") 
    private String description;
	@Column(name = "USERNAME") 
    private String username;
	@Column(name = "CREATION_DATE") 
    private Date creationDate;
	@Column(name = "VALIDITY_DATE") 
    private Date validityDate;
	@Column(name = "LAYER") 
    private String layer;
	@Column(name = "HASH") 
    private String hash;
	@Column(name = "EMAIL") 
	private String email;
	@Column(name = "FLEX") 
	private String flex;

    public Landmark(double latitude, double longitude, double altitude, String name, String description, String username, Date validityDate, String layer, String email, String flex) {
        this();
    	this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = new Double(altitude);
        this.name = name;
        this.description = description;
        this.username = username;
        this.validityDate = validityDate;       
        this.layer = layer;
        this.email = email;
        this.flex = flex;
    }
    
    public Landmark() {
    	this.creationDate = new Date(System.currentTimeMillis());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double l) {
        this.latitude = l;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double l) {
        this.longitude = l;
    }

    public double getAltitude() {
        return altitude.doubleValue();
    }

    public void setAltitude(double l) {
        this.altitude = new Double(l);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date d) {
        this.creationDate = d;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(Date d) {
        this.validityDate = d;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String l) {
        this.layer = l;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public int getId() {
		return landmarkId;
	}
    
    public long getCreationDateLong()
	{
	    return creationDate.getTime();
	}
    
    public long getValidityDateLong()
	{
	     return validityDate.getTime();
	}
    
    public String getFlex() {
        return flex;
    }

    public void setFlex(String f) {
        this.flex = f;
    }
    
    public boolean compare(Landmark l) {
    	return StringUtils.endsWithIgnoreCase(l.getName(), getName()) &&
    			Math.abs(getLatitude() - l.getLatitude()) < 0.02d &&
    			Math.abs(getLongitude() - l.getLongitude()) < 0.02d &&
    			StringUtils.equals(getLayer(), l.getLayer());        		
    }
}
