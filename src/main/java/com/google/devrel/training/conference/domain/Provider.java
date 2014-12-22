package com.google.devrel.training.conference.domain;

import java.io.IOException;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.google.common.base.Preconditions;
import com.google.devrel.training.conference.form.ProviderForm;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * Provider class stores user's provider data.
 */
@Entity
public class Provider {

	/**
	 * The id for the datastore key.
	 *
	 * We use automatic id assignment for entities of Provider class.
	 */
	@Id
	private Long id;

	/**
	 * Any string user wants us to display him/her on this system.
	 */
	@Index
	private String name;

	/**
	 * Holds Profile key as the parent.
	 */
	@Parent
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Key<Profile> profileKey;

	/**
	 * The gplus_id of the creator.
	 */
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String creatorId;

	/**
	 * User's main e-mail address.
	 */
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

	private float latitude;

	private float longitude;

	// private GeoPt location;

	private Boolean inactive;

	/**
	 * Just making the default constructor private.
	 */
	private Provider() {
	}

	/**
	 * Public constructor for Provider.
	 *
	 * @param id
	 *            The datastore key.
	 * @param creatorId
	 * @param providerForm
	 */
	public Provider(final Long id, final String creatorId,
			final ProviderForm providerForm) {

		// TODO : check for other required fields

		this.id = id;
		this.profileKey = Key.create(Profile.class, creatorId);
		this.creatorId = creatorId;
		updateWithProviderForm(providerForm);
	}

	/**
	 * Updates the Provider with ProviderForm. This method is used upon object
	 * creation as well as updating existing Providers.
	 *
	 * @param providerForm
	 *            contains form data sent from the client.
	 */
	public void updateWithProviderForm(final ProviderForm providerForm) {

		Preconditions.checkNotNull(providerForm.getName(),
				"The name is required");
		Preconditions.checkNotNull(providerForm.getMainEmail(),
				"The email is required");
		Preconditions.checkNotNull(providerForm.getMainPhone(),
				"The phone is required");
		Preconditions.checkNotNull(providerForm.getStreetAddress1(),
				"The street address line 1 is required");
		Preconditions.checkNotNull(providerForm.getCity(),
				"The city is required");
		Preconditions.checkNotNull(providerForm.getStateCode(),
				"The state is required");
		Preconditions.checkNotNull(providerForm.getZipCode(),
				"The zip/pin code is required");
		Preconditions.checkNotNull(providerForm.getCountry(),
				"The country is required");

		this.name = providerForm.getName();
		this.shortBio = providerForm.getShortBio();

		this.mainEmail = providerForm.getMainEmail();
		this.streetAddress1 = providerForm.getStreetAddress1();
		this.streetAddress2 = providerForm.getStreetAddress2();
		this.city = providerForm.getCity();
		this.stateCode = providerForm.getStateCode();
		this.zipCode = providerForm.getZipCode();
		this.country = providerForm.getCountry();
		this.mainPhone = providerForm.getMainPhone();
		this.alternatePhone = providerForm.getAlternatePhone();
		this.inactive = providerForm.getInactive();

		setLocation(providerForm);

	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Profile> getProfileKey() {
		return this.profileKey;
	}

	public String getWebsafeKey() {
		return Key.create(this.profileKey, Provider.class, this.id).getString();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getCreatorId() {
		return this.creatorId;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getMainEmail() {
		return this.mainEmail;
	}

	public String getShortBio() {
		return this.shortBio;
	}

	public String getStreetAddress1() {
		return this.streetAddress1;
	}

	public String getStreetAddress2() {
		return this.streetAddress2;
	}

	public String getCity() {
		return this.city;
	}

	public String getStateCode() {
		return this.stateCode;
	}

	public String getZipCode() {
		return this.zipCode;
	}

	public String getCountry() {
		return this.country;
	}

	public String getMainPhone() {
		return this.mainPhone;
	}

	public String getAlternatePhone() {
		return this.alternatePhone;
	}

	public float getLatitude() {
		return this.latitude;
	}

	public float getLongitude() {
		return this.longitude;
	}

	/*
	 * public GeoPt getLocation() { return this.location; }
	 */

	public Boolean getInactive() {
		return this.inactive;
	}

	private void setLocation(final ProviderForm providerForm) {

		final Geocoder geocoder = new Geocoder();

		String address = providerForm.getStreetAddress1() + ", ";

		if ((providerForm.getStreetAddress2() != null)
				&& (providerForm.getStreetAddress2().trim() != "")) {
			address += providerForm.getStreetAddress2() + ", ";
		}

		address += providerForm.getCity() + ", ";
		address += providerForm.getStateCode() + ", ";
		address += providerForm.getCountry();

		final GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
				.setAddress(address).setLanguage("en").getGeocoderRequest();
		try {
			final GeocodeResponse geocoderResponse = geocoder
					.geocode(geocoderRequest);

			if (geocoderResponse.getResults().size() > 0) {

				final GeocoderResult geocoderResult = geocoderResponse
						.getResults().get(0);
				final LatLng latLng = geocoderResult.getGeometry()
						.getLocation();
				this.latitude = latLng.getLat().floatValue();
				this.longitude = latLng.getLng().floatValue();
				// this.location = new GeoPt(this.latitude, this.longitude);
			}

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
