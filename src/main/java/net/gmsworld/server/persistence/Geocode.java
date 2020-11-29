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

@Entity
@Table(name = "GEOCODE")
@SequenceGenerator(name="geocode_id_seq", allocationSize=1)

@NamedQueries({
@NamedQuery(name = "Geocode.findNewest", query = "select g from Geocode g order by g.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
@NamedQuery(name = "Geocode.findAddress", query = "select g from Geocode g where lower(g.location) like lower(:address) order by g.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

public class Geocode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final static String FIND_NEWEST = "Geocode.findNewest";
	public final static String FIND_ADDRESS = "Geocode.findAddress";
	
	@Id
    @GeneratedValue(generator = "geocode_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "GEOCODE_ID")
    private int geocodeId;
	@Column(name = "CREATION_DATE") 
    private Date creationDate;
	@Column(name = "LATITUDE")     
	private double latitude;
	@Column(name = "LONGITUDE") 
	private double longitude;
	@Column(name = "STATUS")
	private int status;
	@Column(name = "LOCATION")
	private String location;
	@Column(name = "FLEX")
	private String flex;
	
	public Geocode(String location, int status, double latitude, double longitude)
	{
		  this();
	      this.location = location;
	      this.status = status;
	      this.latitude = latitude;
	      this.longitude = longitude;    
	 }

	  public Geocode() {
		  this.creationDate = new Date(System.currentTimeMillis());
	  }
	  
	  public double getLatitude()
	  {
	     return latitude;
	  }

	  public double getLongitude()
	  {
	     return longitude;
	  }

	  public Date getCreationDate()
	  {
	      return creationDate;
	  }

	  public String getLocation()
	  {
	      return location;
	  }

	  public void setCreationDate(Date creationDate) {
	       this.creationDate = creationDate;
	  }
	  
	  public int getId() {
		  return geocodeId;
	  }
	  
	  public long getCreationDateLong()
	  {
	      return creationDate.getTime();
	  }

	public String getFlex() {
		return flex;
	}

	public void setFlex(String flex) {
		this.flex = flex;
	}

}
