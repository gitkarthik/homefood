package com.google.devrel.training.conference.form;

import java.util.Date;

/**
 * A simple Java object (POJO) representing a Conference form sent from the
 * client.
 */
public class OfferForm {
	/**
	 * The name of the conference.
	 */
	private String title;

	/**
	 * The description of the conference.
	 */
	private String description;

	/**
	 * The date of the offer.
	 */
	private Date offerDate;

	/**
	 * The capacity of the offer.
	 */
	private int maximumQuantity;

	private Cuisine cuisine;

	private int priceInUnits;

	private String currencySymbol;

	private String websafeProviderKey;

	private OfferForm() {
	}

	/**
	 * Public constructor is solely for Unit Test.
	 *
	 * @param title
	 * @param description
	 * @param offerDate
	 * @param maximumQuantity
	 */
	public OfferForm(final String title, final String description,
			final Date offerDate, final int maximumQuantity,
			final Cuisine cuisine, final int priceInUnits,
			final String currencySymbol, final String websafeProviderKey) {
		this.title = title;
		this.description = description;
		this.offerDate = offerDate == null ? null : new Date(
				offerDate.getTime());
		this.maximumQuantity = maximumQuantity;

		if (cuisine == null) {
			this.cuisine = Cuisine.NOT_SPECIFIED;
		} else {
			this.cuisine = cuisine;
		}

		this.priceInUnits = priceInUnits;
		this.currencySymbol = currencySymbol;
		this.websafeProviderKey = websafeProviderKey;

	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public Date getOfferDate() {
		return this.offerDate;
	}

	public int getMaximumQuantity() {
		return this.maximumQuantity;
	}

	public int getPriceInUnits() {
		return this.priceInUnits;
	}

	public String getCurrencySymbol() {
		return this.currencySymbol;
	}

	public String getWebsafeProviderKey() {
		return this.websafeProviderKey;
	}

	public Cuisine getCuisine() {
		return this.cuisine;
	}

	/**
	 * Enum representing Cuisine types.
	 */
	public static enum Cuisine {
		NOT_SPECIFIED, SOUTH_INDIAN, NORTH_INDIAN, JAIN, GUJARATI, PUNJABI, BENGALI, ANDRHA
	}

}
