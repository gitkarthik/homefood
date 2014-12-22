package com.google.devrel.training.conference.form;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

/**
 * A simple Java object (POJO) representing a query options for Offer.
 */
public class OfferQueryForm {

	private static final Logger LOG = Logger.getLogger(OfferQueryForm.class
			.getName());

	private float latitude;
	private float longitude;
	private Date offerDate = new Date();

	// private final Cuisine cuisine;
	private int distanceInKm = 10; // default to 10 km
	// private final SortField sortField;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private static final float ONE_KM_TO_DEGREE = 0.009f;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private static final Format sdf = new SimpleDateFormat("YYYY-MM-dd");

	// default constructor
	public OfferQueryForm() {
	}

	public OfferQueryForm(final float latitude, final float longitude,
			final int distanceInKm, final Date offerDate /*
														 * , final Cuisine
														 * cuisine, final
														 * SortField sortField
														 */) {

		// default sort by distance - ascending order

		this.latitude = latitude;
		this.longitude = longitude;

		if (offerDate != null) {
			this.offerDate = offerDate;
		}

		// this.cuisine = cuisine;

		if (distanceInKm > 0) {
			this.distanceInKm = distanceInKm;
		}

		// this.sortField = sortField;

	}

	public float getLatitude() {
		return this.latitude;
	}

	public float getLongitude() {
		return this.longitude;
	}

	public Date getOfferDate() {
		return this.offerDate;
	}

	// public Cuisine getCuisine() {
	// return this.cuisine;
	// }

	public int getDistanceInKm() {
		return this.distanceInKm;
	}

	// public SortField getSortField() {
	// return this.sortField;
	// }

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public float getMinLatitude() {
		return this.latitude - (ONE_KM_TO_DEGREE * this.distanceInKm);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public float getMaxLatitude() {
		return this.latitude + (ONE_KM_TO_DEGREE * this.distanceInKm);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public float getMinLongitude() {
		return this.longitude - (ONE_KM_TO_DEGREE * this.distanceInKm);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public float getMaxLongitude() {
		return this.longitude - (ONE_KM_TO_DEGREE * this.distanceInKm);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getOfferDateText() {
		return sdf.format(this.offerDate);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public long getOfferDateNumber() {

		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(this.offerDate);

		return calendar.get(Calendar.DAY_OF_MONTH)
				+ (100 * (calendar.get(Calendar.MONTH) + 1))
				+ (10000 * calendar.get(Calendar.YEAR));
	}

}
