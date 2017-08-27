package net.gmsworld.server.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "COMMENT")
@SequenceGenerator(name="comment_id_seq", allocationSize=1)

@NamedQueries({
@NamedQuery(name = "Comment.findByLandmark", query = "select c from Comment c where c.landmark.landmarkId = :id order by c.creationDate desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

public class Comment {
	
	  public final static String FIND_BY_LANDMARK = "Comment.findByLandmark";
	
	  @Id
      @GeneratedValue(generator = "comment_id_seq", strategy = GenerationType.SEQUENCE)
      @Column(name = "COMMENT_ID")
	  private int id;
	  @Column(name = "CREATION_DATE")
	  private Date creationDate;
	  @ManyToOne
	  @JoinColumn(name = "landmarkId")
	  private Landmark landmark;
	  @Column(name = "MESSAGE")
	  private String message;
	  @Column(name = "USERNAME")
	  private String username;

	  public Comment(String username, Landmark landmark, String message) {
	     this();
	     this.landmark = landmark;
	     this.message = message;
	     this.username = username;
	  }
	  
	  public Comment() {
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

	  public String getMessage() {
	      return message;
	  }
	  
	  public int getId() {
		  return id;
	  }
	  
	  public long getCreationDateLong()
	  {
	      return creationDate.getTime();
	  }
}
