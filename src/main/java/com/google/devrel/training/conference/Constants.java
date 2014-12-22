package com.google.devrel.training.conference;

import com.google.api.server.spi.Constant;

/**
 * Contains the client IDs and scopes for allowed clients consuming the
 * conference API.
 */
public class Constants {
	// future-infusion-790
	// public static final String WEB_CLIENT_ID =
	// "376662991516-3hsgp1rkfscjqp2k7krc7cflvj1deu33.apps.googleusercontent.com";

	// hardy-ally-790
	public static final String WEB_CLIENT_ID = "104818564717-k78f7qf282tdp7qdv74qb5f3ku2qd4j7.apps.googleusercontent.com";

	public static final String ANDROID_CLIENT_ID = "replace this with your Android client ID";
	public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
	public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
	public static final String EMAIL_SCOPE = Constant.API_EMAIL_SCOPE;
	public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

	public static final String MEMCACHE_ANNOUNCEMENTS_KEY = "RECENT_ANNOUNCEMENTS";
}
