package showfinder;

public class EventsEntity {
	private String eventName;
	private String description; // eg: this event starts at <startTime> and it
								// is happening at <address>
	private String latitude;
	private String longitude;

	public String getEventName() {
		return eventName;
	}

	public String getDescription() {
		return description;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public EventsEntity(String eventName, String description, String latitude, String longitude) {
		this.eventName = eventName;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
