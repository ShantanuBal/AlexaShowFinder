/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package showfinder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

/**
 * This sample shows how to create a Lambda function for handling Alexa Skill
 * requests that:
 * 
 * Alexa, start beacon box. onLaunch executed
 *
 * 
 */
public class ShowFinderSpeechlet implements Speechlet {
	private static final Logger log = LoggerFactory.getLogger(ShowFinderSpeechlet.class);

	/**
	 * Constant defining number of events to be read at one time.
	 */
	private static final int PAGINATION_SIZE = 2;

	/**
	 * Length of the delimiter between individual events.
	 */
	// private static final int DELIMITER_SIZE = 2;

	/**
	 * Constant
	 */
	private static final String SESSION_INDEX = "index";
	private static final String SESSION_TEXT = "text";
	private static final String SESSION_DESC = "desc";
	private static final String SESSION_LAT = "lat";
	private static final String SESSION_LON = "lon";
	private static final String SESSION_STORES = "stores";
	private static final String SESSION_DEALS = "deals";
	private static final String SESSION_DEAL_DETAILS = "details";

	/**
	 * Constant defining session attribute key for the intent slot key for the
	 * date of events.
	 */
	private static final String SLOT_DAY = "day";
	private static final String SLOT_CITY = "city";
	private static final String SLOT_NUMBER = "number";
	private static final String SLOT_CATEGORY ="category";
	private static final String SLOT_INDEX = "index";

	private static List<String> BEACON_CATEGORIES = Arrays.asList("breakfast", "lunch", "and Dinner");
	private static List<String> DEAL_NUMBERS = Arrays.asList(" one. ", " two. ", " three. ");

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any initialization logic goes here
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		Intent intent = request.getIntent();
		String intentName = intent.getName();

		if ("GetCategoryIntent".equals(intentName)) {
			try {
				return handleBeaconsCategoryRequest(intent, session);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("GetDealsIntent".equals(intentName)) {
			return handleBeaconDealsRequest(intent, session);
		} else if ("GetDetailsIntent".equals(intentName)) {
				return handleBeaconDetailsRequest(intent, session);
		} else if ("HelpIntent".equals(intentName)) {
			// Create the plain text output.
			String speechOutput = "With Beacon Box, you can know the beacons you collected and get to know more details on a interested beacon";
			String repromptText = "Would you like to list the categories of beacons you collected?";

			return newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
		} else if ("FinishIntent".equals(intentName)) {
			SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
			outputSpeech.setSsml("<speak> Goodbye </speak>");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			throw new SpeechletException("Invalid Intent");
		}
		return null;
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any session cleanup logic would go here
	}

	/**
	 * Function to handle the onLaunch skill behavior.
	 * 
	 * @return SpeechletResponse object with voice/card response to return to
	 *         the user
	 */

	private List<String> getBeaconListByKey(List<Map<String, String>> beaconValueList, String key) {
		List<String> beaconListByKey = new ArrayList<>();
		for (Map<String, String> beaconMap : beaconValueList) {
			beaconListByKey.add(beaconMap.get(key));
		}

		return beaconListByKey;
	}

	private SpeechletResponse getWelcomeResponse() {
		String speechOutput = "Welcome to the Beacon Box. Today you have collected food deals";
		// If the user either does not reply to the welcome message or says
		// something that is not
		// understood, they will be prompted again with this text.
		String repromptText = "With Beacon box, you can get to learn about the deals from your beacon collections";

		return newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
	}

	/**
	 * Function to accept an intent containing a Day slot (date object) and
	 * return the Calendar representation of that slot value. If the user
	 * provides a date, then use that, otherwise use today. The date is in
	 * server time, not in the user's time zone. So "today" for the user may
	 * actually be tomorrow.
	 * 
	 * @param intent
	 *            the intent object containing the day slot
	 * @return the Calendar representation of that date
	 */
	private String getCalendar(Intent intent) {
		Slot daySlot = intent.getSlot(SLOT_DAY);
		Date date;
		/* Calendar calendar = Calendar.getInstance(); */
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (daySlot != null && daySlot.getValue() != null) {
			try {
				date = dateFormat.parse(daySlot.getValue());
			} catch (ParseException e) {
				date = new Date();
			}
		} else {
			date = new Date();
		}

		return dateFormat.format(date);
	}

	/**
	 * Prepares the speech to reply to the user. Obtain events from Wikipedia
	 * for the date specified by the user (or for today's date, if no date is
	 * specified), and return those events in both speech and SimpleCard format.
	 * 
	 * @param intent
	 *            the intent object which contains the date slot
	 * @param session
	 *            the session object
	 * @return SpeechletResponse object with voice/card response to return to
	 *         the user
	 * @throws JSONException
	 */
	private SpeechletResponse handleBeaconsCategoryRequest(Intent intent, Session session) throws JSONException {
		String cardTitle = "Beacon Categories";
		String speechPrefix = "<p> Here the categories from your beacon collection. </p>";
		String cardPrefixContent = "Here the categories from your beacon collection. ";

		StringBuilder speechOutputBuilder = new StringBuilder();
		speechOutputBuilder.append(speechPrefix);
		for (String beaconCategory : BEACON_CATEGORIES) {
			speechOutputBuilder.append("<p>");
			speechOutputBuilder.append(beaconCategory);
			speechOutputBuilder.append("</p>");
		}
		StringBuilder cardOutputBuilder = new StringBuilder();
		cardOutputBuilder.append(cardPrefixContent);

		final String speechOutput = speechOutputBuilder.toString();
		final String repromptText = "<p> You can check deals on a particular category </p>";

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(cardTitle);
		card.setContent(cardOutputBuilder.toString());

		SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>",
				"<speak>" + repromptText + "</speak>");
		response.setCard(card);
		return response;
	}

	private BeaconDao getBeaconDao() {
		final AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient();
		final BeaconDynamoDbClient beaconDynamoDbClient = new BeaconDynamoDbClient(amazonDynamoDBClient);
		final BeaconDao beaconDao = new BeaconDao(beaconDynamoDbClient);

		return beaconDao;

	}

	private void saveBeaconData(List<String> storeList, List<String> dealList, List<String> detailsList, Session session) {
		session.setAttribute(SESSION_STORES, storeList);
		session.setAttribute(SESSION_DEALS, dealList);
		session.setAttribute(SESSION_DEAL_DETAILS, detailsList);
	}

	private SpeechletResponse handleBeaconDealsRequest(Intent intent, Session session) throws JSONException {

		BeaconDao beaconDao = getBeaconDao();
		String category = intent.getSlot(SLOT_CATEGORY).getValue();

		String cardTitle = "Beacons deal Request";
		String speechPrefix = "Here are the " + category + "deals. ";

		final List<Map<String, String>> beaconsList = beaconDao.getBeaconValue(category);
		List<String> storeList = getBeaconListByKey(beaconsList, "store");
		List<String> dealsList = getBeaconListByKey(beaconsList, "deal");
		List<String> detailsList = getBeaconListByKey(beaconsList, "details");

		saveBeaconData(storeList, dealsList, detailsList, session);

		StringBuilder storeDeailsBuilder = new StringBuilder();
		for (int index = 0; index < storeList.size(); index++) {
			storeDeailsBuilder.append(" <p> ");
			storeDeailsBuilder.append(DEAL_NUMBERS.get(index));
			storeDeailsBuilder.append(dealsList.get(index));
			storeDeailsBuilder.append(" at ");
			storeDeailsBuilder.append(storeList.get(index));
			storeDeailsBuilder.append(" </p> ");
		}
		String speechOutput = speechPrefix + storeDeailsBuilder.toString();
		String cardOutput = speechOutput;

		String repromptText = "Do you want more details on any of the deal?";
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(cardTitle);
		card.setContent(cardOutput.toString());

		SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>",
				"<speak>" + repromptText + "</speak>");
		response.setCard(card);
		return response;

	}

	private SpeechletResponse handleBeaconDetailsRequest(Intent intent, Session session) {
		String cardTitle = "More events on this day";

		int dealIndex = Integer.parseInt(intent.getSlot(SLOT_INDEX).getValue()) - 1;

		StringBuilder detailsBuilder = new StringBuilder();
		List<String> storeList = (List<String>) session.getAttribute(SESSION_STORES);
		detailsBuilder.append("<p>");
		detailsBuilder.append(storeList.get(dealIndex));
		detailsBuilder.append(" is offering ");
		List<String> detailsList = (List<String>) session.getAttribute(SESSION_DEAL_DETAILS);
		detailsBuilder.append(detailsList.get(dealIndex));
		detailsBuilder.append(" for this week. ");
		detailsBuilder.append("</p>");

		String speechOutput = detailsBuilder.toString();
		String cardOutput = speechOutput;

		String repromptText = "Are you interested in other beacon categories?";
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(cardTitle);
		card.setContent(cardOutput.toString());

		SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>",
				"<speak>" + repromptText + "</speak>");
		response.setCard(card);
		return response;
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml(stringOutput);
		SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
		repromptOutputSpeech.setSsml(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

}
