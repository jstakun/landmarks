package net.gmsworld.server.persistence;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * Entity implementation class for Entity: Route
 *
 */
@Entity
@Table(name="Routes")
@SequenceGenerator(name="route_id_seq", allocationSize=1)

@NamedQueries({
	@NamedQuery(name = "Route.findByName", query = "select r from Route r where r.name = :name", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

@TypeDefs({
	@TypeDef( name= "StringJsonObject", typeClass = net.gmsworld.server.persistence.StringJsonUserType.class)
})

public class Route implements Serializable {
  
	public final static String FIND_BY_NAME = "Route.findByName";
   	
	private String name;
	@Type(type = "StringJsonObject")
	private String route;   
	@Id
	@GeneratedValue(generator = "route_id_seq", strategy = GenerationType.SEQUENCE)
	private Long id;
	private Date creationDate;
	private static final long serialVersionUID = 1L;

	public Route() {
		this.setCreationDate(new Date(System.currentTimeMillis()));
	}
	
	public Route(String name, String route) {
		this();
		this.name = name;
		this.route = route;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}   
	public String getRoute() {
		return this.route;
	}

	public void setRoute(String route) {
		this.route = route;
	}   
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
   
}
