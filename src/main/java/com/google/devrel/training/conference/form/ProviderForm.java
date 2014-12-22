package com.google.devrel.training.conference.form;



/**
 * A simple Java object (POJO) representing a Provider form sent from the client.
 */
public class ProviderForm {
    /**
     * The name of the provider.
     */
    private String name;

    private String mainEmail;

    private String shortBio;
    
    private String streetAddress1;
    
    private String streetAddress2;
    
    private String city;
    
    private String stateCode;
    
    private String zipCode;
    
    private String country;
    
    private String mainPhone;
    
    private String alternatePhone;
    
    private Boolean inactive;

    private ProviderForm() {}

    /**
     * Public constructor is solely for Unit Test.
     * @param name
     */
    public ProviderForm( String name,
     String shortBio,
     String mainEmail,
     String streetAddress1,
     String streetAddress2,
     String city,
     String stateCode,
     String zipCode,
     String country,
     String mainPhone,
     String alternatePhone,
     Boolean inactive) {
        
    	this.name = name;
        this.shortBio = shortBio;
  
        this.mainEmail = mainEmail;
        this.streetAddress1 = streetAddress1;
        this.streetAddress2=  streetAddress2 ;
        this.city= city  ;
        this.stateCode= stateCode   ;
        this.zipCode=  zipCode ;
        this.country=  country  ;
        this.mainPhone=  mainPhone  ;
        this.alternatePhone=  alternatePhone ;
        this.inactive = inactive == null? Boolean.FALSE: this.inactive;
        
    
    }

	public String getName() {
		return name;
	}

	public String getMainEmail() {
		return mainEmail;
	}

	public String getShortBio() {
		return shortBio;
	}

	public String getStreetAddress1() {
		return streetAddress1;
	}

	public String getStreetAddress2() {
		return streetAddress2;
	}

	public String getCity() {
		return city;
	}

	public String getStateCode() {
		return stateCode;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getCountry() {
		return country;
	}

	public String getMainPhone() {
		return mainPhone;
	}

	public String getAlternatePhone() {
		return alternatePhone;
	}

	public Boolean getInactive() {
		return inactive;
	}

    
    
    
    
    
    
}
