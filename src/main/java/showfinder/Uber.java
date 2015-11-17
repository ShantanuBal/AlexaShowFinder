package showfinder;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class Uber {

	static String getTimeEstimateOfUber(String start_latitude, String start_longitude, String product_id)
			throws IOException, JSONException {

		String url = "https://api.uber.com/v1/estimates/time?server_token=BKYQvDBHqGST82NeyoAY9gaTC3E85jxN6IpK7tRY&"
				+ "start_latitude=" + start_latitude + "&start_longitude=" + start_longitude + "&product_id="
				+ product_id;
		StringBuffer response = getResponseFromUrl(url);
		JSONObject jsonObj = new JSONObject(response.toString());
		JSONArray arr = jsonObj.getJSONArray("times");
		StringBuilder time = new StringBuilder();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject each = arr.getJSONObject(i);

			int tim = each.getInt("estimate");
			time.append(" estimated arrival time is ");
			if (tim / 60 != 0) {
				time.append(tim / 60);
				time.append(" minutes ");
			}
			if (tim % 60 != 0) {
				time.append(tim % 60);
				time.append(" seconds ");
			}
		}
		return time.toString();
	}

	static String getPriceEstimateAndTimeToDestinationOfUber(String start_latitude, String start_longitude,
			String end_latitude, String end_longitude, String product_name) throws IOException, JSONException {
		String product_id = getProducts(start_latitude, start_longitude, product_name);
		if (product_id.equals("")) {
			return "I am Sorry, Not a valid location For Uber";
		}
		String url = "https://api.uber.com/v1/estimates/price?server_token=BKYQvDBHqGST82NeyoAY9gaTC3E85jxN6IpK7tRY&"
				+ "start_latitude=" + start_latitude + "&start_longitude=" + start_longitude + "&end_latitude="
				+ end_latitude + "&end_longitude=" + end_longitude;

		StringBuffer response = getResponseFromUrl(url);
		if (response.toString().equals("-1")) {
			return "Sorry, Distance between source and destination exceeds 100 miles";
		}
		JSONObject jsonObj = new JSONObject(response.toString());
		// System.out.println(jsonObj);

		JSONArray arr = jsonObj.getJSONArray("prices");
		JSONObject jsonoutput = new JSONObject();
		StringBuilder time = new StringBuilder();
		StringBuilder first = new StringBuilder();
		first.append("Your ");
		first.append(product_name);
		String retvalue = "";
		for (int i = 0; i < arr.length(); i++) {
			JSONObject each = arr.getJSONObject(i);

			if (product_id.equals(each.getString("product_id"))) {
				double low_estimate = each.getInt("low_estimate");
				double high_estimate = each.getInt("high_estimate");
				double surge = each.getDouble("surge_multiplier");
				time.append(", time to destination is ");
				if (((int) surge) != 1) {
					low_estimate = low_estimate * surge;
					high_estimate = high_estimate * surge;

				}
				jsonoutput.put("display_name", product_name);
				jsonoutput.put("price_estimate", low_estimate + " dollars to " + high_estimate + " dollars");

				int tim = each.getInt("duration");
				if (tim / 60 != 0) {
					time.append(tim / 60);
					time.append(" minutes ");
				}
				if (tim % 60 != 0) {
					time.append(tim % 60);
					time.append(" seconds ");
				}

				time.append(", distance is ");
				time.append(each.getDouble("distance"));
				time.append(" miles");
				time.append(" and fare is ");
				time.append(low_estimate + " dollars to " + high_estimate + " dollars");
				jsonoutput.put("timetodestination", time.toString());
				jsonoutput.put("distance", each.getDouble("distance") + " miles");
			}
			retvalue = getTimeEstimateOfUber(start_latitude, start_longitude, product_id);
			retvalue = first.toString() + retvalue + time.toString();

		}

		return retvalue;
	}

	static StringBuffer getResponseFromUrl(String url) throws IOException {
		StringBuffer response = new StringBuffer();
		try {
			Client client = Client.create();
			WebResource webResource = client.resource(url);

			response.append(webResource.get(String.class).toString());
		} catch (Exception ex) {
			return response.append(url);
		}

		return response;
	}

	static String getProducts(String start_latitude, String start_longitude, String productName)
			throws IOException, JSONException {
		String url = "https://api.uber.com/v1/products?server_token=BKYQvDBHqGST82NeyoAY9gaTC3E85jxN6IpK7tRY&"
				+ "latitude=" + start_latitude + "&longitude=" + start_longitude;
		StringBuffer response = getResponseFromUrl(url);
		JSONObject jsonObj = new JSONObject(response.toString());
		JSONArray arr = jsonObj.getJSONArray("products");
		String productId = "";
		for (int i = 0; i < arr.length(); i++) {
			JSONObject each = arr.getJSONObject(i);

			if (productName.equals(each.getString("display_name"))) {
				productId = each.getString("product_id");
			}

		}
		return productId;
	}

}
