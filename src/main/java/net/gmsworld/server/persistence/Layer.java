package net.gmsworld.server.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

@Entity
@Table(name = "LAYER")

@NamedQueries({
	@NamedQuery(name = "Layer.findAll", query = "select l from Layer l", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})	
			
public class Layer {
	
	public static final String FIND_ALL = "Layer.findAll";
	
	@Id
    @Column(name = "NAME")
	private String layerName;
	@Column(name = "DESCRIPTION")
	private String desc;
	@Column(name = "ENABLED")
    private boolean enabled;
	@Column(name = "MANAGEABLE")
    private boolean manageable;
	@Column(name = "CHECKINABLE")
    private boolean checkinable;
	@Column(name = "FORMATTED_NAME")
    private String formatted;
	@Column(name = "VERSION")
    private int version;
    
    public Layer(String name, String desc, boolean enabled, boolean manageable, boolean checkinable, String formatted) {
        this();
    	this.layerName = name;
        this.enabled = enabled;
        this.manageable = manageable;
        this.checkinable = checkinable;
        this.formatted = formatted;
        this.desc = desc;
    }
    
    public Layer() {
    	this.version = 2;
    }

    public String getName() {
        return layerName;
    }

    public void setName(String name) {
        this.layerName = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isManageable() {
        return manageable;
    }

    public boolean isCheckinable()
    {
        return checkinable;
    }

    public String getFormatted() {
        return formatted;
    }

    public String getDesc() {
        return desc;
    }
    
    public int getVersion() {
    	return version;
    }

}
