package net.gmsworld.server.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CHECKIN")
@SequenceGenerator(name="checkin_id_seq", allocationSize=1)

@NamedQueries({
	@NamedQuery(name = "Checkin.findByLandmark", query = "select c from Checkin c where c.landmark.landmarkId = :id order by c.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Checkin.countNewer", query = "select count(c) from Checkin c where c.creationDate > :date and c.venueid = :venueid and c.username = :username", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

public class Checkin {
	
	  public final static String FIND_BY_LANDMARK = "Checkin.findByLandmark";
	  public final static String COUNT_NEWER = "Checkin.countNewer";
	  
	  @Id
      @GeneratedValue(generator = "checkin_id_seq", strategy = GenerationType.SEQUENCE)
      @Column(name = "CHECKIN_ID")
	  private int id;
	  @Column(name = "CREATION_DATE")
	  private Date creationDate;
	  @ManyToOne
	  @JoinColumn(name = "landmarkId")
	  private Landmark landmark;
	  @Column(name = "USERNAME")
	  private String username;
	  @Column(name = "TYPE") 
	  private int type; //0 qr, 1 web, 2 social
	  @Column(name = "VENUEID") 
	  private String venueid;
	  
	  public Checkin(String username, Landmark landmark, String venueid, int type)
	  {
		  this();
	      this.username = username;
	      this.landmark = landmark;
	      this.type = type;
	      this.venueid = venueid;
	  }
	  
	  public Checkin() {
		  this.creationDate = new Date(System.currentTimeMillis());
	  }

	  public Date getCreationDate()
	  {
	      return creationDate;
	  }

	  public String getUsername()
	  {
	      return username;
	  }
	  
	  public int getId() {
		  return id;
	  }
	  
	  public long getCreationDateLong()
	  {
	      return creationDate.getTime();
	  }
}
