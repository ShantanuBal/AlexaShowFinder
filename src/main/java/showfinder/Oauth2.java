package showfinder;
import java.util.Arrays;

import showfinder.OAuth2Credentials.Scope;

public class Oauth2 {

	public String getAuthorizationUrl() {
		OAuth2Credentials credentials = new OAuth2Credentials.Builder()
        .setClientSecrets("9x5d54-oGBfVr-3zC4wAnYyY9783iF9k", "-iREizzW6dqHtqdCZFIjbWTQYxTwiEUdKQxV7Hta")
        .setScopes(Arrays.asList(OAuth2Credentials.Scope.PROFILE, OAuth2Credentials.Scope.REQUEST,
                OAuth2Credentials.Scope.HISTORY))
        .setRedirectUri("https://alexauber.azurewebsites.net")
        .build();
		
		String authorizationUrl = credentials.getAuthorizationUrl();
		return authorizationUrl;
        
	}

}
