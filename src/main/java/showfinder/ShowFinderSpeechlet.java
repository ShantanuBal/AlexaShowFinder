/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package showfinder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
 * This sample shows how to create a Lambda function for handling Alexa Skill requests that:
 * 
 * Alexa, start show finder.
 * onLaunch executed
 * 
 * What's happening in LA today?
 * TicketMasterFetchIntent - {city}, {day}
 * Response - Do you want me to go on or are you interested in an event?
 * 
 * Go on / yes / yup
 * TicketMasterContinueIntent -
 * Response - Would you like to get details for an Uber ride?
 * 
 * Get me more details for event number 2
 * TicketMasterDetailsIntent - {number}
 * Response - 
 * 
 * Get me Uber details for event number 2
 * UberDetailsIntent - {number}
 *  
 */
public class ShowFinderSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(ShowFinderSpeechlet.class);

    /**
     * URL prefix to download history content from Wikipedia.
     */
    /*private static final String URL_PREFIX =
            "https://en.wikipedia.org/w/api.php?action=query&prop=extracts"
                    + "&format=json&explaintext=&exsectionformat=plain&redirects=&titles=";
	*/
    
    /**
     * Constant defining number of events to be read at one time.
     */
    private static final int PAGINATION_SIZE = 2;

    /**
     * Length of the delimiter between individual events.
     */
    //private static final int DELIMITER_SIZE = 2;

    /**
     * Constant
     */
    private static final String SESSION_INDEX = "index";
    private static final String SESSION_TEXT = "text";
    private static final String SESSION_DESC = "desc";
	private static final String SESSION_LAT = "lat";
	private static final String SESSION_LON = "lon";

    /**
     * Constant defining session attribute key for the intent slot key for the date of events.
     */
    private static final String SLOT_DAY = "day";
    private static final String SLOT_CITY = "city";
    private static final String SLOT_NUMBER = "number";
    
    /**
     * Size of events from Ticketmaster response.
     */
    //private static final int SIZE_OF_EVENTS = 10;

    /**
     * Array of month names.
     */
    
    /*
    private static final String[] MONTH_NAMES = {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
    };*/

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = intent.getName();

        if ("GetTicketMasterFetchIntent".equals(intentName)) {
            try {
				return handleTicketmasterFetchRequest(intent, session);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else if ("GetTicketMasterContinueIntent".equals(intentName)) {
            return handleTicketmasterContinueRequest(session);
        } else if ("GetTicketMasterDetailsIntent".equals(intentName)) {
                return handleTicketmasterDetailsRequest(intent, session);
        } else if ("GetUberDetailsIntent".equals(intentName)) {
            try {
				return handleUberDetailsRequest(intent, session);
			} catch (IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else if ("GetUberBookingIntent".equals(intentName)) {
            return handleUberBookingRequest(intent, session);
        } else if ("HelpIntent".equals(intentName)) {
            // Create the plain text output.
            String speechOutput =
                    "With Show Finder, you can find events happening in your city and book a ride through Uber.";

            String repromptText = "Where do you want to find events?";

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
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = "Welcome to Show Finder. Give me a location and a date to look for events.";
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String repromptText =
                "With Show Finder, you can find out what movies are playing nearby and book a ride on Uber.";

        return newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
    }

    /**
     * Function to accept an intent containing a Day slot (date object) and return the Calendar
     * representation of that slot value. If the user provides a date, then use that, otherwise use
     * today. The date is in server time, not in the user's time zone. So "today" for the user may
     * actually be tomorrow.
     * 
     * @param intent
     *            the intent object containing the day slot
     * @return the Calendar representation of that date
     */
    private String getCalendar(Intent intent) {
        Slot daySlot = intent.getSlot(SLOT_DAY);
        Date date;
        /*Calendar calendar = Calendar.getInstance();*/
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
     * Prepares the speech to reply to the user. Obtain events from Wikipedia for the date specified
     * by the user (or for today's date, if no date is specified), and return those events in both
     * speech and SimpleCard format.
     * 
     * @param intent
     *            the intent object which contains the date slot
     * @param session
     *            the session object
     * @return SpeechletResponse object with voice/card response to return to the user
     * @throws JSONException 
     */
    private SpeechletResponse handleTicketmasterFetchRequest(Intent intent, Session session) throws JSONException {
        /*Calendar calendar*/ String date = getCalendar(intent);
        /*
        String month = MONTH_NAMES[calendar.get(Calendar.MONTH)];
        String date = Integer.toString(calendar.get(Calendar.DATE));
		*/
        
        String speechPrefixContent = "<p>For " + date + "</p> ";
        String cardPrefixContent = "For " + date + ", ";
        String cardTitle = "Events on " + date;
        String city = intent.getSlot(SLOT_CITY).getValue();
        
        Ticketmaster ticketmaster = new Ticketmaster();
		ArrayList<EventsEntity> events = ticketmaster.getEventDetails(city, date);
        //ArrayList<String> events = getEventsFromTicketMaster(date, city); /*getJsonEventsFromWikipedia(month, date);*/
        
		String speechOutput = "";
        if (events == null || events.isEmpty()) {
            speechOutput = "There are no events right now. Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            speechOutputBuilder.append(speechPrefixContent);
            StringBuilder cardOutputBuilder = new StringBuilder();
            cardOutputBuilder.append(cardPrefixContent);
            for (int i = 0; i < PAGINATION_SIZE; i++) {
                speechOutputBuilder.append("<p>");
                speechOutputBuilder.append(events.get(i).getEventName());
                speechOutputBuilder.append("</p>");
                cardOutputBuilder.append(events.get(i).getEventName());
                cardOutputBuilder.append(" ");
            }
            speechOutputBuilder.append(" Do you want more shows?");
            cardOutputBuilder.append(" Do you want more shows?");
            speechOutput = speechOutputBuilder.toString();

            String repromptText = "Do you want to find out about more shows?";

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle(cardTitle);
            card.setContent(cardOutputBuilder.toString());

            // After reading the first 3 events, set the count to 3 and add the events
            // to the session attributes
            session.setAttribute(SESSION_INDEX, PAGINATION_SIZE);
            ArrayList<String> name = new ArrayList<String>();
            ArrayList<String> description = new ArrayList<String>();
            ArrayList<String> latitude = new ArrayList<String>();
            ArrayList<String> longitude = new ArrayList<String>();
            
            for(int i=0; i < events.size(); i++) {
            	name.add(events.get(i).getEventName());
            	description.add(events.get(i).getDescription());
            	latitude.add(events.get(i).getLatitude());
            	longitude.add(events.get(i).getLongitude());
            }
            session.setAttribute(SESSION_TEXT, name);
            session.setAttribute(SESSION_DESC, description);
            session.setAttribute(SESSION_LAT, latitude);
            session.setAttribute(SESSION_LON, longitude);

            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
            response.setCard(card);
            return response;
        }
    }

    /**
     * Prepares the speech to reply to the user. Obtains the list of events as well as the current
     * index from the session attributes. After getting the next set of events, increment the index
     * and store it back in session attributes. This allows us to obtain new events without making
     * repeated network calls, by storing values (events, index) during the interaction with the
     * user.
     * 
     * @param session
     *            object containing session attributes with events list and index
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse handleTicketmasterContinueRequest(Session session) {
        String cardTitle = "More events on this day";
		@SuppressWarnings("unchecked")
		ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_TEXT);
        int index = (int) session.getAttribute(SESSION_INDEX);
        String speechOutput = "";
        String cardOutput = "";
        if (events == null) {
            speechOutput =
                    "With Show Finder, you can find out what movies are playing nearby and book a ride on Uber."
            			+ "Where would you like to watch a movie?";
        } else if (index >= events.size()) {
            speechOutput =
                    "There are no more shows in this city.";
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            StringBuilder cardOutputBuilder = new StringBuilder();
            for (int i = 0; i < PAGINATION_SIZE && index < events.size(); i++) {
                speechOutputBuilder.append("<p>");
                speechOutputBuilder.append(events.get(index));
                speechOutputBuilder.append("</p> ");
                cardOutputBuilder.append(events.get(index));
                cardOutputBuilder.append(" ");
                index++;
            }
            if (index < events.size()) {
                speechOutputBuilder.append(" Do you want to hear more?");
                cardOutputBuilder.append(" Do you want to hear more?");
            }
            session.setAttribute(SESSION_INDEX, index);
            speechOutput = speechOutputBuilder.toString();
            cardOutput = cardOutputBuilder.toString();
        }
        String repromptText = "Do you want to find more shows?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(cardOutput.toString());

        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
        response.setCard(card);
        return response;
    }
    
    private SpeechletResponse handleTicketmasterDetailsRequest(Intent intent, Session session) {
    	String cardTitle = "Details of selected event";
        
        
        @SuppressWarnings("unchecked")
		ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_DESC);
        int index = (int) session.getAttribute(SESSION_INDEX);
        
        int number = Integer.parseInt(intent.getSlot(SLOT_NUMBER).getValue());
    	String speechOutput = events.get(index-PAGINATION_SIZE+number-1);
    	String cardOutput = speechOutput;
    	
        String repromptText = "Do you want to find more shows?";
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(cardOutput.toString());
        
        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
        response.setCard(card);
        return response;
    }
    
    private SpeechletResponse handleUberDetailsRequest(Intent intent, Session session) throws IOException, JSONException {
    	String cardTitle = "More events on this day";
    	
    	String source_lat = "34.0223519"; 
    	String source_lon = "-118.2873057";
    	
    	@SuppressWarnings("unchecked")
		ArrayList<String> lat = (ArrayList<String>) session.getAttribute(SESSION_LAT);
    	@SuppressWarnings("unchecked")
		ArrayList<String> lon = (ArrayList<String>) session.getAttribute(SESSION_LON);
    	
    	int index = (int) session.getAttribute(SESSION_INDEX);
        
        int number = Integer.parseInt(intent.getSlot(SLOT_NUMBER).getValue());
    	String dest_lat = lat.get(index-PAGINATION_SIZE+number-1);
    	String dest_lon = lon.get(index-PAGINATION_SIZE+number-1);
    	
    	String speechOutput = Uber.getPriceEstimateAndTimeToDestinationOfUber(source_lat, source_lon, dest_lat, dest_lon, "uberX");
        String cardOutput = speechOutput;
        
        String repromptText = "Do you want to find more shows?";
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(cardOutput.toString());
        
        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
        response.setCard(card);
        return response;
    }
    
    private SpeechletResponse handleUberBookingRequest(Intent intent, Session session) {
    	String cardTitle = "More events on this day";
    	
    	String source_lat = "34.0223519"; 
    	String source_lon = "-118.2873057";
    	
    	String userId = session.getUser().getUserId();
    	
    	@SuppressWarnings("unchecked")
		ArrayList<String> lat = (ArrayList<String>) session.getAttribute(SESSION_LAT);
    	@SuppressWarnings("unchecked")
		ArrayList<String> lon = (ArrayList<String>) session.getAttribute(SESSION_LON);
    	
    	int index = (int) session.getAttribute(SESSION_INDEX);
        
        int number = Integer.parseInt(intent.getSlot(SLOT_NUMBER).getValue());
    	String dest_lat = lat.get(index-PAGINATION_SIZE+number-1);
    	String dest_lon = lon.get(index-PAGINATION_SIZE+number-1);
    	
    	String speechOutput, cardOutput;
		SimpleCard card = new SimpleCard();
		String repromptText = "Do you want to find more shows?";
		
    	if (isInDB(userId)) {
    		// Code snippet to book uber
        
    		
    		speechOutput = "Your Uber booking request has been made.";
    		cardOutput = speechOutput;
    	
    		// Create the Simple card content.
    		card.setTitle(cardTitle);
    		card.setContent(cardOutput.toString());
    	} else {
    		//Code snippet for generating login URL
    		String login_url = "";
    		
    		speechOutput = "Go to the Alexa app to allow me to book your ride.";
    		cardOutput = login_url;
    	
    		// Create the Simple card content.
    		card.setTitle(cardTitle);
    		card.setContent(cardOutput.toString());
    	}
        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
        response.setCard(card);
        return response;
    }
   

    private boolean isInDB(String userId) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
     * Wrapper for creating the Ask response from the input strings.
     * 
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
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
