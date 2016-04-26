package beacon.kontakt.main;

public class Venue {

	private String clientID;
	private String sourceID;
	private double rssi;
	private double distance;
	private long timestamp;

	
	
	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public double getRssi() {
		return rssi;
	}

	public void setRssi(double rssi) {
		this.rssi = rssi;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
