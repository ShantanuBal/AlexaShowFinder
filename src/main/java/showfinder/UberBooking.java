package showfinder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONException;
import org.json.JSONObject;

public class UberBooking {
	
	private static final String UBER_SANDBOX_SERVER = "https://sandbox-api.uber.com";
	private static final String UBER_SERVER = "https://api.uber.com";
	

	private String generateJsonString(String start_latitude,String start_longitude,String end_latitude, String end_longitude, String productId) throws JSONException 
	{
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("start_latitude", start_latitude);
		jsonObj.put("start_longitude", start_longitude);
		jsonObj.put("end_latitude", end_latitude);
		jsonObj.put("end_longitude", end_longitude);
		jsonObj.put("product_id", productId);
		
		return jsonObj.toString();
	}

	public String requestUber(String start_latitude,String start_longitude,String end_latitude, String end_longitude, String productName) throws JSONException, IOException {
		
		String accessToken = "zhWsbVFfiE0zOJUqi0EHJgyqtARA0g";
		final String ERROR = "-1";

		String productId = Uber.getProducts(start_latitude, start_longitude, productName); 
		String requestUrl = UBER_SANDBOX_SERVER + "/v1/requests";
		String requestJsonString = generateJsonString(start_latitude, start_longitude, end_latitude, end_longitude, productId);
		String response = null;
		
		try {
			
			Client client = Client.create();
            WebResource webResource = 
                    client.resource(requestUrl);
		
            String authToken = "Bearer " + accessToken;

            ClientResponse clientResponse = webResource.header("Content-Type", "application/json")
		    		.header("Authorization", authToken)
	                .post(ClientResponse.class, requestJsonString);
		    
		    System.out.println(clientResponse.toString());
            response = clientResponse.getEntity(String.class); 
            
		} catch(Exception ex) {
			return ERROR;
		}
		
		return response.toString();
	}
}
