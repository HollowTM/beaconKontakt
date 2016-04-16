
public class CloudBeacon {

	private String clientID;
	private double xPos;
	private double yPos;
	private String venue;

	public CloudBeacon(String clientID, double xPos, double yPos, String venue) {
		this.clientID = clientID;
		this.xPos = xPos;
		this.yPos = yPos;
		this.venue = venue;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public double getxPos() {
		return xPos;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public double getyPos() {
		return yPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

}
