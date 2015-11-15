package showfinder;
import java.util.Arrays;

import showfinder.OAuth2Credentials.Scope;

public class Oauth2 {

	public String getAuthorizationUrl(String start_latitude,String start_longitude,String end_latitude,String end_longitude,String user_id, String product_id) {
		OAuth2Credentials credentials = new OAuth2Credentials.Builder()
        .setClientSecrets("9x5d54-oGBfVr-3zC4wAnYyY9783iF9k", "-iREizzW6dqHtqdCZFIjbWTQYxTwiEUdKQxV7Hta")
        .setScopes(Arrays.asList(OAuth2Credentials.Scope.PROFILE, OAuth2Credentials.Scope.REQUEST,
                OAuth2Credentials.Scope.HISTORY))
        .setRedirectUri("https://uberalexa.azurewebsites.net/?"+"user_id="+user_id+"&source_lat="+start_latitude+"&source_lon="+start_longitude+"&dest_lat="+end_latitude+"&dest_lon="+end_longitude+"&product_id="+product_id)
        .build();
		String authorizationUrl = credentials.getAuthorizationUrl();
		return authorizationUrl;
        
	}

}
