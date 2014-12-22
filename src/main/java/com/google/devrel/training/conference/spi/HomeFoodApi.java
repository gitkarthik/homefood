package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.factory;
import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.AppEngineUser;
import com.google.devrel.training.conference.domain.Offer;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.domain.Provider;
import com.google.devrel.training.conference.form.OfferForm;
import com.google.devrel.training.conference.form.OfferQueryForm;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.google.devrel.training.conference.form.ProviderForm;
import com.google.devrel.training.conference.form.ProviderQueryForm;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

/**
 * Defines homefood APIs.
 */

@Api(name = "homefood", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
		Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE }, description = "Home Food API for creating and querying offers,"
		+ " and for creating and getting Providers")
public class HomeFoodApi {

	private static final Logger LOG = Logger.getLogger(HomeFoodApi.class
			.getName());

	private static String extractDefaultDisplayNameFromEmail(final String email) {
		return email == null ? null : email.substring(0, email.indexOf("@"));
	}

	private static Profile getProfileFromUser(final User user,
			final String userId) {
		// First fetch it from the datastore.
		Profile profile = ofy().load().key(Key.create(Profile.class, userId))
				.now();
		if (profile == null) {
			// Create a new Profile if not exist.
			final String email = user.getEmail();
			profile = new Profile(userId,
					extractDefaultDisplayNameFromEmail(email), email,
					TeeShirtSize.NOT_SPECIFIED);
		}
		return profile;
	}

	/**
	 * This is an ugly workaround for null userId for Android clients.
	 *
	 * @param user
	 *            A User object injected by the cloud endpoints.
	 * @return the App Engine userId for the user.
	 */
	private static String getUserId(final User user) {
		String userId = user.getUserId();
		if (userId == null) {
			LOG.info("userId is null, so trying to obtain it from the datastore.");
			final AppEngineUser appEngineUser = new AppEngineUser(user);
			ofy().save().entity(appEngineUser).now();
			// Begin new session for not using session cache.
			final Objectify objectify = ofy().factory().begin();
			final AppEngineUser savedUser = objectify.load()
					.key(appEngineUser.getKey()).now();
			userId = savedUser.getUser().getUserId();
			LOG.info("Obtained the userId: " + userId);
		}
		return userId;
	}

	/**
	 * Just a wrapper for Boolean.
	 */
	public static class WrappedBoolean {

		private final Boolean result;

		public WrappedBoolean(final Boolean result) {
			this.result = result;
		}

		public Boolean getResult() {
			return this.result;
		}
	}

	/**
	 * A wrapper class that can embrace a generic result or some kind of
	 * exception.
	 *
	 * Use this wrapper class for the return type of objectify transaction.
	 *
	 * <pre>
	 * {@code
	 * // The transaction that returns Conference object.
	 * TxResult<Conference> result = ofy().transact(new Work<TxResult<Conference>>() {
	 *     public TxResult<Conference> run() {
	 *         // Code here.
	 *         // To throw 404
	 *         return new TxResult<>(new NotFoundException("No such conference"));
	 *         // To return a conference.
	 *         Conference conference = somehow.getConference();
	 *         return new TxResult<>(conference);
	 *     }
	 * }
	 * // Actually the NotFoundException will be thrown here.
	 * return result.getResult();
	 * </pre>
	 *
	 * @param <ResultType>
	 *            The type of the actual return object.
	 */
	private static class TxResult<ResultType> {

		private ResultType result;

		private Throwable exception;

		private TxResult(final ResultType result) {
			this.result = result;
		}

		private TxResult(final Throwable exception) {
			if ((exception instanceof NotFoundException)
					|| (exception instanceof ForbiddenException)
					|| (exception instanceof ConflictException)) {
				this.exception = exception;
			} else {
				throw new IllegalArgumentException("Exception not supported.");
			}
		}

		private ResultType getResult() throws NotFoundException,
				ForbiddenException, ConflictException {
			if (this.exception instanceof NotFoundException) {
				throw (NotFoundException) this.exception;
			}
			if (this.exception instanceof ForbiddenException) {
				throw (ForbiddenException) this.exception;
			}
			if (this.exception instanceof ConflictException) {
				throw (ConflictException) this.exception;
			}
			return this.result;
		}
	}

	/**
	 * Returns a Profile object associated with the given user object. The cloud
	 * endpoints system automatically inject the User object.
	 *
	 * @param user
	 *            A User object injected by the cloud endpoints.
	 * @return Profile object.
	 * @throws UnauthorizedException
	 *             when the User object is null.
	 */
	@ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
	public Profile getProfile(final User user) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		return ofy().load().key(Key.create(Profile.class, getUserId(user)))
				.now();
	}

	/**
	 * Creates or updates a Profile object associated with the given user
	 * object.
	 *
	 * @param user
	 *            A User object injected by the cloud endpoints.
	 * @param profileForm
	 *            A ProfileForm object sent from the client form.
	 * @return Profile object just created.
	 * @throws UnauthorizedException
	 *             when the User object is null.
	 */
	@ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
	public Profile saveProfile(final User user, final ProfileForm profileForm)
			throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		String displayName = profileForm.getDisplayName();
		TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize();

		Profile profile = ofy().load()
				.key(Key.create(Profile.class, getUserId(user))).now();
		if (profile == null) {
			// Populate displayName and teeShirtSize with the default values if
			// null.
			if (displayName == null) {
				displayName = extractDefaultDisplayNameFromEmail(user
						.getEmail());
			}
			if (teeShirtSize == null) {
				teeShirtSize = TeeShirtSize.NOT_SPECIFIED;
			}
			profile = new Profile(getUserId(user), displayName,
					user.getEmail(), teeShirtSize);
		} else {
			profile.update(displayName, teeShirtSize);
		}
		ofy().save().entity(profile).now();
		return profile;
	}

	/**
	 * Creates a new Provider object and stores it to the datastore.
	 *
	 * @param user
	 *            A user who invokes this method, null when the user is not
	 *            signed in.
	 * @param providerForm
	 *            A ProviderForm object representing user's inputs.
	 * @return A newly created Provider Object.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 */
	@ApiMethod(name = "createProvider", path = "provider", httpMethod = HttpMethod.POST)
	public Provider createProvider(final User user,
			final ProviderForm providerForm) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		// Allocate Id first, in order to make the transaction idempotent.
		final Key<Profile> profileKey = Key.create(Profile.class,
				getUserId(user));

		final Key<Provider> providerKey = factory().allocateId(profileKey,
				Provider.class);

		final long providerId = providerKey.getId();
		// final Queue queue = QueueFactory.getDefaultQueue();
		final String userId = getUserId(user);

		// Start a transaction.
		final Provider provider = ofy().transact(new Work<Provider>() {
			@Override
			public Provider run() {
				// Fetch user's Profile.
				final Profile profile = getProfileFromUser(user, userId);
				final Provider provider = new Provider(providerId, userId,
						providerForm);

				// Save Provider and Profile.
				ofy().save().entities(provider, profile).now();

				/*
				 * queue.add( ofy().getTransaction(), TaskOptions.Builder
				 * .withUrl("/tasks/send_confirmation_email") .param("email",
				 * profile.getMainEmail()) .param("providerInfo",
				 * provider.toString()));
				 */

				return provider;
			}
		});

		return provider;

	}

	/**
	 * Updates the existing Provider with the given providerId.
	 *
	 * @param user
	 *            A user who invokes this method, null when the user is not
	 *            signed in.
	 * @param providerForm
	 *            A ProviderForm object representing user's inputs.
	 * @param websafeProviderKey
	 *            The String representation of the Provider key.
	 * @return Updated Provider object.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 * @throws NotFoundException
	 *             when there is no Provider with the given providerId.
	 * @throws ForbiddenException
	 *             when the user is not the owner of the Provider.
	 */
	@ApiMethod(name = "updateProvider", path = "provider/{websafeProviderKey}", httpMethod = HttpMethod.PUT)
	public Provider updateProvider(final User user,
			final ProviderForm providerForm,
			@Named("websafeProviderKey") final String websafeProviderKey)
			throws UnauthorizedException, NotFoundException,
			ForbiddenException, ConflictException {
		// If not signed in, throw a 401 error.
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		final String userId = getUserId(user);
		// Update the provider with the providerForm sent from the client.
		// Need a transaction because we need to safely preserve the number of
		// allocated seats.
		final TxResult<Provider> result = ofy().transact(
				new Work<TxResult<Provider>>() {
					@Override
					public TxResult<Provider> run() {
						// If there is no Provider with the id, throw a 404
						// error.
						final Key<Provider> providerKey = Key
								.create(websafeProviderKey);
						final Provider provider = ofy().load().key(providerKey)
								.now();
						if (provider == null) {
							return new TxResult<>(new NotFoundException(
									"No Provider found with the key: "
											+ websafeProviderKey));
						}
						// If the user is not the owner, throw a 403 error.
						final Profile profile = ofy().load()
								.key(Key.create(Profile.class, userId)).now();
						if ((profile == null)
								|| !provider.getCreatorId().equals(userId)) {
							return new TxResult<>(new ForbiddenException(
									"Only the owner can update the provider."));
						}
						provider.updateWithProviderForm(providerForm);
						ofy().save().entity(provider).now();
						return new TxResult<>(provider);
					}
				});
		// NotFoundException or ForbiddenException is actually thrown here.
		return result.getResult();
	}

	/**
	 * Returns a Provider object with the given providerId.
	 *
	 * @param websafeProviderKey
	 *            The String representation of the Provider Key.
	 * @return a Provider object with the given providerId.
	 * @throws NotFoundException
	 *             when there is no Provider with the given providerId.
	 */
	@ApiMethod(name = "getProvider", path = "provider/{websafeProviderKey}", httpMethod = HttpMethod.GET)
	public Provider getProvider(
			@Named("websafeProviderKey") final String websafeProviderKey)
			throws NotFoundException {
		final Key<Provider> providerKey = Key.create(websafeProviderKey);
		final Provider provider = ofy().load().key(providerKey).now();
		if (provider == null) {
			throw new NotFoundException("No Provider found with key: "
					+ websafeProviderKey);
		}
		return provider;
	}

	/**
	 * Queries against the datastore with the given filters and returns the
	 * result.
	 *
	 * Normally this kind of method is supposed to get invoked by a GET HTTP
	 * method, but we do it with POST, in order to receive providerQueryForm
	 * Object via the POST body.
	 *
	 * @param providerQueryForm
	 *            A form object representing the query.
	 * @return A List of Providers that match the query.
	 */
	@ApiMethod(name = "queryProviders", path = "queryProviders", httpMethod = HttpMethod.POST)
	public List<Provider> queryProviders(
			final ProviderQueryForm providerQueryForm) {
		final Iterable<Provider> providerIterable = providerQueryForm
				.getQuery();
		final List<Provider> result = new ArrayList<>(0);
		final List<Key<Profile>> creatorsKeyList = new ArrayList<>(0);
		for (final Provider provider : providerIterable) {
			creatorsKeyList.add(Key.create(Profile.class,
					provider.getCreatorId()));
			result.add(provider);
		}
		// To avoid separate datastore gets for each Provider, pre-fetch the
		// Profiles.
		ofy().load().keys(creatorsKeyList);
		return result;
	}

	/**
	 * Returns a list of Providers that the user created. In order to receive
	 * the websafeProviderKey via the JSON params, uses a POST method.
	 *
	 * @param user
	 *            An user who invokes this method, null when the user is not
	 *            signed in.
	 * @return a list of Providers that the user created.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 */
	@ApiMethod(name = "getProvidersCreated", path = "getProvidersCreated", httpMethod = HttpMethod.POST)
	public List<Provider> getProvidersCreated(final User user)
			throws UnauthorizedException {
		// If not signed in, throw a 401 error.
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		final String userId = getUserId(user);
		return ofy().load().type(Provider.class)
				.ancestor(Key.create(Profile.class, userId)).order("name")
				.list();
	}

	/**
	 * Creates a new Offer object and stores it to the datastore.
	 *
	 * @param user
	 *            A user who invokes this method, null when the user is not
	 *            signed in.
	 * @param offerForm
	 *            A OfferForm object representing user's inputs.
	 * @return A newly created Offer Object.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 */
	@ApiMethod(name = "createOffer", path = "offer", httpMethod = HttpMethod.POST)
	public Offer createOffer(final User user, final OfferForm offerForm)
			throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		if (offerForm == null) {
			throw new IllegalArgumentException("offerForm is null");
		}

		if (offerForm.getWebsafeProviderKey() == null) {
			throw new IllegalArgumentException(
					"offerForm.websafeProviderKey is null");
		}

		// Allocate Id first, in order to make the transaction idempotent.

		final Key<Provider> providerKey = Key.create(offerForm
				.getWebsafeProviderKey());

		final Key<Offer> offerKey = factory().allocateId(providerKey,
				Offer.class);

		final long offerId = offerKey.getId();

		// final Queue queue = QueueFactory.getDefaultQueue();
		final String userId = getUserId(user);

		// Start a transaction.
		final Offer offer = ofy().transact(new Work<Offer>() {
			@Override
			public Offer run() {
				// Fetch user's Profile.
				final Profile profile = getProfileFromUser(user, userId);
				final Provider provider = getProviderUsingKey(providerKey);
				final Offer offer = new Offer(offerId, userId, offerForm);

				// Save Offer and Profile.
				ofy().save().entities(offer, provider, profile).now();

				/*
				 * queue.add( ofy().getTransaction(), TaskOptions.Builder
				 * .withUrl("/tasks/send_confirmation_email") .param("email",
				 * profile.getMainEmail()) .param("offerInfo",
				 * offer.toString()));
				 */

				return offer;
			}

			private Provider getProviderUsingKey(final Key<Provider> providerKey) {
				final Provider provider = ofy().load().key(providerKey).now();
				return provider;
			}
		});

		return offer;

	}

	/**
	 * Updates the existing Offer with the given offerId.
	 *
	 * @param user
	 *            A user who invokes this method, null when the user is not
	 *            signed in.
	 * @param offerForm
	 *            A OfferForm object representing user's inputs.
	 * @param websafeOfferKey
	 *            The String representation of the Offer key.
	 * @return Updated Offer object.
	 * @throws UnauthorizedException
	 *             when the user is not signed in.
	 * @throws NotFoundException
	 *             when there is no Offer with the given offerId.
	 * @throws ForbiddenException
	 *             when the user is not the owner of the Offer.
	 */
	@ApiMethod(name = "updateOffer", path = "offer/{websafeOfferKey}", httpMethod = HttpMethod.PUT)
	public Offer updateOffer(final User user, final OfferForm offerForm,
			@Named("websafeOfferKey") final String websafeOfferKey)
			throws UnauthorizedException, NotFoundException,
			ForbiddenException, ConflictException {
		// If not signed in, throw a 401 error.
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}
		final String userId = getUserId(user);
		// Update the offer with the offerForm sent from the client.
		// Need a transaction because we need to safely preserve the number of
		// allocated seats.
		final TxResult<Offer> result = ofy().transact(
				new Work<TxResult<Offer>>() {
					@Override
					public TxResult<Offer> run() {
						// If there is no Offer with the id, throw a 404
						// error.
						final Key<Offer> offerKey = Key.create(websafeOfferKey);
						final Offer offer = ofy().load().key(offerKey).now();
						if (offer == null) {
							return new TxResult<>(new NotFoundException(
									"No Offer found with the key: "
											+ websafeOfferKey));
						}
						// If the user is not the owner, throw a 403 error.
						final Profile profile = ofy().load()
								.key(Key.create(Profile.class, userId)).now();
						if ((profile == null)
								|| !offer.getCreatorId().equals(userId)) {
							return new TxResult<>(new ForbiddenException(
									"Only the owner can update the offer."));
						}
						offer.updateWithOfferForm(offerForm);
						ofy().save().entity(offer).now();
						return new TxResult<>(offer);
					}
				});
		// NotFoundException or ForbiddenException is actually thrown here.
		return result.getResult();
	}

	/**
	 * Returns a Offer object with the given offerId.
	 *
	 * @param websafeOfferKey
	 *            The String representation of the Offer Key.
	 * @return a Offer object with the given offerId.
	 * @throws NotFoundException
	 *             when there is no Offer with the given offerId.
	 */
	@ApiMethod(name = "getOffer", path = "offer/{websafeOfferKey}", httpMethod = HttpMethod.GET)
	public Offer getOffer(@Named("websafeOfferKey") final String websafeOfferKey)
			throws NotFoundException {
		final Key<Offer> offerKey = Key.create(websafeOfferKey);
		final Offer offer = ofy().load().key(offerKey).now();
		if (offer == null) {
			throw new NotFoundException("No Offer found with key: "
					+ websafeOfferKey);
		} else {
			final Provider provider = ofy().load().key(offer.getProviderKey())
					.now();
			offer.setProvider(provider);
		}
		return offer;
	}

	/**
	 * Queries against the datastore with the given filters and returns the
	 * result.
	 *
	 * Normally this kind of method is supposed to get invoked by a GET HTTP
	 * method, but we do it with POST, in order to receive offerQueryForm Object
	 * via the POST body.
	 *
	 * @param offerQueryForm
	 *            A form object representing the query.
	 * @return A List of Offers that match the query.
	 */
	@ApiMethod(name = "queryOffers", path = "queryOffers", httpMethod = HttpMethod.POST)
	public List<Offer> queryOffers(final OfferQueryForm offerQueryForm) {

		Query<Offer> query = ofy().load().type(Offer.class);

		query = query.filter("offerDateNumber == ",
				offerQueryForm.getOfferDateNumber());

		final GeoPt geoLocG = new GeoPt(offerQueryForm.getMaxLatitude(),
				offerQueryForm.getMaxLongitude());

		final GeoPt geoLocL = new GeoPt(offerQueryForm.getMinLatitude(),
				offerQueryForm.getMinLongitude());

		query = query.filter("location >= ", geoLocL);
		query = query.filter("location <= ", geoLocG);

		final List<Offer> offers = query.list();

		final GeoPt callerLocation = new GeoPt(offerQueryForm.getLatitude(),
				offerQueryForm.getLongitude());

		for (final Offer offer : offers) {
			offer.setDistance(getDistanceInKm(offer.getLocation(),
					callerLocation));
		}

		return offers;
	}

	private double getDistanceInKm(final GeoPt fromLocation,
			final GeoPt toLocation) {

		final int RADIUS_OF_EARTH = 6371; // Radius of the earth in km

		final double dLat = Math.toRadians(toLocation.getLatitude()
				- fromLocation.getLatitude());
		final double dLon = Math.toRadians(toLocation.getLongitude()
				- fromLocation.getLongitude());

		final double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
				+ (Math.cos(Math.toRadians(toLocation.getLatitude()))
						* Math.cos(Math.toRadians(fromLocation.getLatitude()))
						* Math.sin(dLon / 2) * Math.sin(dLon / 2));

		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		final double distance = RADIUS_OF_EARTH * c; // Distance in km

		return distance;
	}

}
