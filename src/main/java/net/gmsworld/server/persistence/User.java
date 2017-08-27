package net.gmsworld.server.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GMSWORLD_USER")

public class User {

	@Id
	@Column(name = "LOGIN")
	private String login;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "FIRSTNAME")
	private String firstname;

	@Column(name = "LASTNAME")
	private String lastname;

	@Column(name = "CREATION_DATE")
	private Date regDate;

	@Column(name = "CONFIRMED")
	private boolean confirmed;

	@Column(name = "CONFIRMATION_DATE")
	private Date confirmDate;

	@Column(name = "PERSONAL_INFO")
	private String personalInfo;

	@Column(name = "LAST_LOGON_DATE")
	private Date lastLogonDate;
	
	public User(String login, String password, String email, String firstname, String lastname)
	  {
		  this();
	      this.email = email;
	      this.login = login;
	      this.password = password;
	      this.firstname = firstname;
	      this.lastname = lastname;
	      this.confirmed = false;
	  }
	  
	  public User() {
		  this.regDate = new Date(System.currentTimeMillis());
	  }

	  //public String getPassword()
	  //{
	  //    return password;
	  //}

	    /**
	     * @return the confirmed
	     */
	    public boolean isConfirmed() {
	        return confirmed;
	    }

	    /**
	     * @param confirmed the confirmed to set
	     */
	    public void setConfirmed(Boolean confirmed) {
	        this.confirmed = confirmed;
	    }

	    /**
	     * @param confirmDate the confirmDate to set
	     */
	    public void setConfirmDate(Date confirmDate) {
	        this.confirmDate = confirmDate;
	    }

	    /**
	     * @return the login
	     */
	    public String getLogin() {
	        return login;
	    }

	    /**
	     * @return the email
	     */
	    //public String getEmail() {
	    //    return email;
	    //}

	    /**
	     * @param personalInfo the personalInfo to set
	     */
	    public void setPersonalInfo(String personalInfo) {
	        this.personalInfo = personalInfo;
	    }

	    /**
	     * @return the personalInfo
	     */
	    public String getPersonalInfo() {
	        return personalInfo;
	    }

	    /**
	     * @return the firstname
	     */
	    public String getFirstname() {
	        return firstname;
	    }

	    /**
	     * @return the lastname
	     */
	    public String getLastname() {
	        return lastname;
	    }

	    
	   /**
	     * @param lastLogonDate the lastLogonDate to set
	     */
	    public void setLastLogonDate(Date lastLogonDate) {
	        this.lastLogonDate = lastLogonDate;
	    }
	    
	    public void setPassword(String password) {
	    	this.password = password;
	    }
	    
	    public String getPassword() {
	        return password;
	    }
	    
	    public String getEmail() {
	    	return email;
	    }
}
