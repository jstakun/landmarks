package net.gmsworld.server.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

@Entity
@Table(name = "TOKEN")

@NamedQueries({
	@NamedQuery(name = "Token.isValidToken", query = "select count(t) from Token t where t.key = :key and t.validityDate > current_date and t.scope = :scope", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Token.getToken", query = "select t from Token t where t.key = :key and t.validityDate > current_date and t.scope = :scope", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@NamedQuery(name = "Token.topTokens", query = "select t from Token t order by t.count desc", hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
})

public class Token {

	public static final String IS_VALID_TOKEN = "Token.isValidToken";
	public static final String GET_TOKEN = "Token.getToken";
	public static final String GET_TOP_TOKENS = "Token.topTokens";
	public static final String DA_SCOPE = "da";
	public static final String LM_SCOPE = "lm";
	
	@Id
	@Column(name = "KEY")
	private String key;
	@Column(name = "CREATION_DATE")
	private Date creationDate;
	@Column(name = "VALIDITY_DATE") 
    private Date validityDate;
	@Column(name = "SCOPE")
	private String scope;
	@Column(name = "USERNAME")
	private String user;
	@Column(name = "LAST_USAGE_DATE") 
    private Date lastUsageDate;
	@Column(name = "COUNT")
	private int count = 0;
	
	public Token(String key, Date validityDate, String scope, String user) {
		this();
		this.key = key;
		this.validityDate = validityDate;
		this.scope = scope;
		this.user = user;
	}
	
	public Token() {
    	this.creationDate = new Date(System.currentTimeMillis());
    }
	
	public String getKey() {
		return key;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setLastUsageDate(Date lastUsageDate) {
		this.lastUsageDate = lastUsageDate;
	}
	
	public String getUser() {
		return user;
	}
	
	public Date getCreationDate() {
        return creationDate;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public long getCreationDateLong()
	{
	    return creationDate.getTime();
	}
    
    public long getValidityDateLong()
	{
	     return validityDate.getTime();
	}
}
