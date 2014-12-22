package com.google.devrel.training.conference.domain;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.GeoPt;
import com.google.common.base.Preconditions;
import com.google.devrel.training.conference.form.OfferForm;
import com.google.devrel.training.conference.form.OfferForm.Cuisine;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfTrue;

/**
 * Offer class stores offer information.
 */
@Entity(name = "Offer")
public class Offer {

	/**
	 * The id for the datastore key.
	 *
	 * We use automatic id assignment for entities of Offer class.
	 */
	@Id
	private Long id;

	/**
	 * The short description of the offer.
	 */
	@Index
	private String title;

	/**
	 * The long description of the offer.
	 */
	private String description;

	/**
	 * Holds Provider key as the parent.
	 */
	@Parent
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Key<Provider> providerKey;

	/**
	 * The id of the provider.
	 */
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Long providerId;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String creatorId;

	/**
	 * cuisine type of this offer.
	 */
	@Index
	private Cuisine cuisine;

	/**
	 * The GeoPt location of the provider.
	 */
	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private GeoPt location;

	@Index
	private float latitude;

	@Index
	private float longitude;

	/**
	 * The offer date of this offer.
	 */
	@Index
	private Date offerDate;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String offerDateText;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private long offerDateNumber;

	/**
	 * The maximum quantity of this offer.
	 */
	@Index
	private int maximumQuantity;

	/**
	 * Number currently available.
	 */
	@Index
	private int availableQuantity;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private int priceInUnit; // $10.50 represented as 1050 , Rs 200.30
	// represented as 20030

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String currencySymbol; // $ , Rs , ...

	@Index({ IfTrue.class })
	private final Boolean active = Boolean.TRUE;

	@Ignore
	private static final Format sdf = new SimpleDateFormat("YYYY-MM-dd");

	private String providerName;

	@Ignore
	private Provider provider;

	@Ignore
	private Double distance;

	/**
	 * Just making the default constructor private.
	 */
	private Offer() {
	}

	public Offer(final long id, final String creatorId,
			final OfferForm offerForm) {
		Preconditions.checkNotNull(offerForm.getTitle(),
				"The title is required");
		Preconditions.checkNotNull(offerForm.getDescription(),
				"The description is required");
		Preconditions.checkNotNull(offerForm.getCuisine(),
				"The cuisine is required");
		Preconditions.checkArgument(offerForm.getMaximumQuantity() > 0,
				"The maximum quantity must be greater than zero");
		Preconditions.checkArgument(offerForm.getPriceInUnits() > 0,
				"The price in units must be greater than zero");
		Preconditions.checkNotNull(offerForm.getCurrencySymbol(),
				"The currency symbol is required");
		Preconditions.checkNotNull(offerForm.getOfferDate(),
				"The offer date is required");
		Preconditions.checkNotNull(offerForm.getWebsafeProviderKey(),
				"The WebSafe Provider Key is required");

		this.id = id;

		// this.providerKey = Key.create(Provider.class, providerId);
		// this.providerId = providerId;

		this.providerKey = Key.create(offerForm.getWebsafeProviderKey());
		this.providerId = this.providerKey.getId();

		this.creatorId = creatorId;
		updateWithOfferForm(offerForm);
	}

	public long getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Provider> getProviderKey() {
		return this.providerKey;
	}

	public String getWebsafeKey() {
		return Key.create(this.providerKey, Offer.class, this.id).getString();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Long getProviderId() {
		return this.providerId;
	}

	/**
	 * Returns Provider's name.
	 *
	 * @return provider's name. If there is no Provider, return his/her Id.
	 */
	public String getProviderName() {

		return this.providerName;
	}

	/**
	 * Returns a defensive copy of offerDate if not null.
	 *
	 * @return a defensive copy of offerDate if not null.
	 */
	public Date getOfferDate() {
		return this.offerDate == null ? null : new Date(
				this.offerDate.getTime());
	}

	public int getMaximumQuantity() {
		return this.maximumQuantity;
	}

	public int getAvailableQuantity() {
		return this.availableQuantity;
	}

	/**
	 * Updates the Offer with OfferForm. This method is used upon object
	 * creation as well as updating existing Offers.
	 *
	 * @param offerForm
	 *            contains form data sent from the client.
	 */
	public void updateWithOfferForm(final OfferForm offerForm) {

		Preconditions.checkNotNull(offerForm.getTitle(),
				"The title is required");
		Preconditions.checkNotNull(offerForm.getDescription(),
				"The description is required");
		Preconditions.checkNotNull(offerForm.getCuisine(),
				"The cuisine is required");
		Preconditions.checkArgument(offerForm.getMaximumQuantity() > 0,
				"The maximum quantity must be greater than zero");
		Preconditions.checkArgument(offerForm.getPriceInUnits() > 0,
				"The price in units must be greater than zero");
		Preconditions.checkNotNull(offerForm.getCurrencySymbol(),
				"The currency symbol is required");
		Preconditions.checkNotNull(offerForm.getOfferDate(),
				"The offer date is required");

		this.title = offerForm.getTitle();
		this.description = offerForm.getDescription();

		this.offerDate = new Date(offerForm.getOfferDate().getTime());

		// this.active

		this.cuisine = offerForm.getCuisine();

		this.currencySymbol = offerForm.getCurrencySymbol();

		this.priceInUnit = offerForm.getPriceInUnits();

		// Check maximumQuantity value against the number of already allocated
		// quantity.
		final int allocatedQuantity = this.maximumQuantity
				- this.availableQuantity;

		if (offerForm.getMaximumQuantity() < allocatedQuantity) {
			throw new IllegalArgumentException(allocatedQuantity
					+ " quantity are already allocated, "
					+ "but you tried to set maximumQuantity to "
					+ offerForm.getMaximumQuantity());
		}
		// The initial number of availableQuantity is the same as
		// maximumQuantity.
		// However, if there are already some quantity allocated, we should
		// subtract that numbers.
		this.maximumQuantity = offerForm.getMaximumQuantity();
		this.availableQuantity = this.maximumQuantity - allocatedQuantity;

		final Provider provider = getPrivateProvider();

		this.providerName = provider.getName();
		// this.location = getProvider().getLocation();
		this.latitude = provider.getLatitude();
		this.longitude = provider.getLongitude();
		this.location = new GeoPt(this.latitude, this.longitude);

		this.offerDateText = formattedDateText(this.offerDate);

		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(this.offerDate);

		// calendar.get(Calendar.YEAR);
		// calendar.get(Calendar.MONTH);
		// calendar.get(Calendar.DAY_OF_MONTH);

		this.offerDateNumber = calendar.get(Calendar.DAY_OF_MONTH)
				+ (100 * (calendar.get(Calendar.MONTH) + 1))
				+ (10000 * calendar.get(Calendar.YEAR));
	}

	private String formattedDateText(final Date date) {

		return sdf.format(date);
	}

	public void buyOffer(final int number) {
		if (this.availableQuantity < number) {
			throw new IllegalArgumentException(
					"There are not enough quantity available.");
		}
		this.availableQuantity = this.availableQuantity - number;
	}

	public void givebackOffer(final int number) {
		if ((this.availableQuantity + number) > this.maximumQuantity) {
			throw new IllegalArgumentException(
					"The quantity of offer will exceed the maximum quantity.");
		}
		this.availableQuantity = this.availableQuantity + number;
	}

	public String getPrice() {

		final int tens = this.priceInUnit / 100;
		final int units = this.priceInUnit - (tens * 100);

		if (units >= 10) {
			return this.currencySymbol + " " + tens + "." + units;
		} else {
			return this.currencySymbol + " " + tens + ".0" + units;
		}

	}

	public Object getCreatorId() {
		return this.creatorId;
	}

	public Cuisine getCuisine() {
		return this.cuisine;
	}

	public float getLatitude() {
		return this.latitude;
	}

	public float getLongitude() {
		return this.longitude;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getOfferDateText() {
		return this.offerDateText;
	}

	public int getPriceInUnit() {
		return this.priceInUnit;
	}

	public String getCurrencySymbol() {
		return this.currencySymbol;
	}

	public Boolean getActive() {
		return this.active;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public long getOfferDateNumber() {
		return this.offerDateNumber;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public GeoPt getLocation() {
		return this.location;
	}

	public Double getDistance() {
		return this.distance;
	}

	public void setDistance(final Double distance) {
		this.distance = distance;
	}

	public void setProvider(final Provider provider) {
		this.provider = provider;
	}

	public Provider getProvider() {
		return this.provider;
	}

	/**
	 * Returns Provider's details.
	 *
	 * @return provider.
	 */
	private Provider getPrivateProvider() {

		final Key<Profile> profileKey = Key.create(Profile.class,
				this.creatorId);

		final Provider provider = ofy().load()
				.key(Key.create(profileKey, Provider.class, this.providerId))
				.now();

		return provider;
	}

}
