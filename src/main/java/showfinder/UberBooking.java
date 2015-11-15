package showfinder;

import java.io.IOException;

import org.json.JSONException;

public class UberBooking {
	
	private static final String UBER_SERVER = "https://api.uber.com";
	
	public String requestUber(String startLatitude, String startLongitude, String endLatitude, String endLongitude, String productName) throws JSONException, IOException {
		String productId = Uber.getProducts(startLatitude, startLongitude, productName);
		
	}
}
