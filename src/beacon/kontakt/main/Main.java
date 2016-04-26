package beacon.kontakt.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.json.JSONArray;
import org.json.JSONObject;

import beacon.kontakt.trilateration.NonLinearLeastSquaresSolver;
import beacon.kontakt.trilateration.TrilaterationFunction;

public class Main {

	// Venue List ID for ajVv7, c8Vwl, nTQa3
	static final String VenueajVv7 = "0aca881a-14ed-4844-a8a1-3e77a5844ee0";
	static final String Venuec8Vwl = "d54435d4-8ccc-42d3-9db7-f3083cd0c99a";
	static final String VenuenTQa3 = "1baf857e-ce79-4746-a0ba-1bad03aaa703";

	// Cloud Beacons
	static CloudBeacon[] cloudBeaconList = { new CloudBeacon("ajVv7", 0, 4.2, VenueajVv7),
			new CloudBeacon("c8Vwl", 3.20, 3.21, Venuec8Vwl), new CloudBeacon("nTQa3", 1.48, 0, VenuenTQa3), };

	// Position
	static final double xPosajVv7 = cloudBeaconList[0].getxPos();
	static final double yPosajVv7 = cloudBeaconList[0].getyPos();

	static final double xPosc8Vwl = cloudBeaconList[1].getxPos();
	static final double yPosc8Vwl = cloudBeaconList[1].getyPos();

	static final double xPosnTQa3 = cloudBeaconList[2].getxPos();
	static final double yPosnTQa3 = cloudBeaconList[2].getyPos();

	// Beacon
	static Beacon[] beaconList = { new Beacon("rV0B"), new Beacon("W2xX"), new Beacon("wO6P") };

	// array for beacon labor setup | [CloudBeacons(scanner)] [Time] [Beacons in
	// each Time]
	static Venue[][][] venueList = new Venue[3][100][100];

	// default Kontakt.io Power Level
	static int txPower = -77;

	// time Minus Constant
	static final int timeMinusSeconds = 3600;

	// time Window for test purpose
	static final long testTime = 1460733000;

	// todo getter/setter or better
	static public String message = null;

	public static void main(String[] args) throws InterruptedException {
		String VenueaList[] = {VenueajVv7, Venuec8Vwl, VenuenTQa3};
		for(int i = 0; i < VenueaList.length; i++){
			String restResponse = restCall(VenueaList[i], testTime);
			if(restResponse.equals("error")){
				message = "error";
				return;
			}
			jsonToArray(restResponse, i);
		}
		 outputHomepage(4);
		 System.out.println(message);
	}

	/**
	 * Generates the JSON Object for the h view
	 */
	private static void outputHomepage(int time) {

		JSONObject json = new JSONObject();
		JSONArray beacons = new JSONArray();
		// Iterate Beacons
		for (int i = 0; i < 3; i++) {
			JSONObject item = new JSONObject();

			// give back int[] with position in venue p[0] = position in venue 0
			int p[] = searchSmartBeacon(time, beaconList[i].getClientID());
			item.put("clientID", beaconList[i].getClientID());
			// Positions
			JSONArray positions = new JSONArray();
			JSONObject itemPosition = new JSONObject();
			itemPosition.put("timestamp", venueList[0][time][p[i]].getTimestamp());
			double[] xy = position(time, beaconList[i].getClientID());
			itemPosition.put("xPos", xy[0]);
			itemPosition.put("yPos", xy[1]);
			positions.put(itemPosition);
			item.put("clientID", beaconList[i].getClientID());
			item.put("position", positions);

			// Distance from Stations
			// Search Beacons in venueList
			JSONArray distanceFromStations = new JSONArray();
			for (int j = 0; j < 3; j++) {

				JSONObject distanceStation = new JSONObject();

				distanceStation.put("sourceId", venueList[j][time][p[i]].getSourceID());
				distanceStation.put("distanceInRSSI", venueList[j][time][p[i]].getRssi());
				distanceStation.put("distanceInM", venueList[j][time][p[i]].getDistance());

				distanceFromStations.put(distanceStation);
				item.put("distanceFromStations", distanceFromStations);

			}
			beacons.put(item);

		}
		json.put("beacons", beacons);

		// create the Station (CloudBeacon) Objects for JSON
		JSONArray stations = new JSONArray();
		for (int n = 0; n < 3; n++) {
			JSONObject item2 = new JSONObject();
			item2.put("clientID", cloudBeaconList[n].getClientID());
			item2.put("xPos", cloudBeaconList[n].getxPos());
			item2.put("yPos", cloudBeaconList[n].getyPos());
			stations.put(item2);
		}

		json.put("stations", stations);
		message = json.toString();
	}

	/**
	 * search SmartBeacon position in VenueList with given time
	 * 
	 * @param time
	 * @param beacon
	 * @return
	 */
	private static int[] searchSmartBeacon(int time, String beacon) {
		int[] p = new int[3];
		// iterate venues
		for (int i = 0; i < 3; i++) {
			// iterate beacons
			for (int j = 0; j < 3; j++) {
				if (venueList[i][time][j].getClientID().equals(beacon)) {
					p[i] = j;
				}
			}
		}
		return p;
	}

	/**
	 * Uses the Trilateration function to calculate the position of the beacon
	 * 
	 * @param time
	 *            not real "time" yet
	 * @param beacon
	 * @return xPos and yPos from the beacon
	 */
	private static double[] position(int time, String beacon) {

		// CloudBeacon positions | ajvv7, c8vwl, ntqa3
		double[][] positions = new double[][] { { xPosajVv7, yPosajVv7 }, { xPosc8Vwl, yPosc8Vwl },
				{ xPosnTQa3, yPosnTQa3 } };
		// searches the distances information in the right order
		double[] distances = new double[] { searchSmartBeaconDistance(0, time, beacon),
				searchSmartBeaconDistance(1, time, beacon), searchSmartBeaconDistance(2, time, beacon) };

		NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
				new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();

		// the answer
		double[] centroid = optimum.getPoint().toArray();
		// System.out.println(centroid[0]);
		// System.out.println(centroid[1]);

		return centroid;
	}

	/**
	 * searches the smartBeacon in the venue List
	 * 
	 * @param venue
	 *            the Venue in which to search
	 * @param time
	 *            the "time" to search. till now there is no real time to
	 *            search....
	 * @param smartBeacon
	 *            the smartBeacon to search
	 * @return
	 */
	private static double searchSmartBeaconDistance(int venue, int time, String smartBeacon) {

		for (int i = 0; i < 3; i++) {
			if (venueList[venue][time][i].getClientID().equals(smartBeacon)) {
				return venueList[venue][time][i].getDistance();
			}
		}
		return -1;
	}

	private static String restCall(String venue, long time) {
		try {
			// Connection information from Kontakt
			// http://developer.kontakt.io/rest-api/version-8-EA/resources/#analytics-range-metrics
			URL url = new URL("http://api.kontakt.io/analytics/metrics/ranges?sourceId=" + venue + "&startTimestamp="
					+ time + "&sourceType=VENUE&maxResults=100&iso8601Timestamps=false");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/vnd.com.kontakt+json;version=7");
			conn.setRequestProperty("Api-Key", "PYTnfXWZhlweZFBqSZTSPLwBbWOpxewY");

			if (conn.getResponseCode() != 200) {
				// throw new RuntimeException("Failed : HTTP error code : " +
				// conn.getResponseCode());
				System.out.println("error");
				conn.disconnect();
				return "error";
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			// System.out.println("Output from Rest | Venue " + venue);
			while ((output = br.readLine()) != null) {

				// System.out.println(output);
				return output;
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return "error";
	}

	/**
	 * Converts the REST response to the array
	 * 
	 * @param json
	 *            Rest response
	 * @param venueSave
	 *            venue to which it will save
	 */
	private static void jsonToArray(String json, int venueSave) {
		JSONObject jsnobject = null;

		jsnobject = new JSONObject(json);

		JSONArray ranges = jsnobject.getJSONArray("ranges");

		// Range array
		for (int i = 0; i < ranges.length(); i++) {

			JSONObject explrObject = ranges.getJSONObject(i);
			// System.out.println("\n" + explrObject);
			JSONArray clients = explrObject.getJSONArray("clients");

			// iterate clients
			// For test purpose, i will only save the SmartBeacons
			int b = 0;

			for (int j = 0; j < clients.length(); j++) {
				try {
					String uniqueId = clients.getJSONObject(j).getJSONObject("clientId").getString("uniqueId");

					// Save only Test Beacons
					if (uniqueId.equals("W2xX") || uniqueId.equals("rV0B") || uniqueId.equals("wO6P")) {

						// System.out.println("\n---------\n" + uniqueId);
						// sourceID and value
						JSONArray sourceID = clients.getJSONObject(j).getJSONArray("rssis");

						// create object
						venueList[venueSave][i][b] = new Venue();

						// clientId
						venueList[venueSave][i][b].setClientID(uniqueId);
						// sourceId
						venueList[venueSave][i][b].setSourceID(sourceID.getJSONObject(0).getString("sourceId"));
						// Rssis
						venueList[venueSave][i][b].setRssi(sourceID.getJSONObject(0).getDouble("value"));
						// Distance
						venueList[venueSave][i][b]
								.setDistance(calculateAccuracy(txPower, venueList[venueSave][i][b].getRssi()));
						// System.out.println("RSSI " +
						// venueList[venueSave][i][b].getRssi() + "| Distance "
						// + venueList[venueSave][i][b].getDistance());
						// Timestamp
						// System.out.println("Unix Timestamp " +
						// explrObject.getInt("timestamp"));
						venueList[venueSave][i][b].setTimestamp(explrObject.getInt("timestamp"));

						// iterate b for SmartBeacon position
						b++;
					}
				} catch (Exception e) {
					// not perfect, but works
					// clean exceptions later
					// System.out.println(e);
				}
			}

		}

	}

	// Creates ISO Timezone
	private static long currentTime(int timeMinusSeconds) {
		Instant unixTime = Instant.now();
		unixTime = unixTime.minusSeconds(timeMinusSeconds);
		// System.out.println(unixTime);
		return unixTime.getEpochSecond();
	}

	// tsPower level Kontakt.io
	// https://support.kontakt.io/hc/en-gb/articles/201621521-Transmission-power-Range-and-RSSI
	// Distance Code
	// http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
	// http://developer.radiusnetworks.com/2014/12/04/fundamentals-of-beacon-ranging.html
	private static double calculateAccuracy(int txPower, double rssi) {

		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return accuracy;
		}
	}

	/**
	 * Output from venueList
	 */
	private static void consoleOutputArray() {
		for (int i = 0; i < 3; i++) {
			boolean stop = false;
			// System.out.println("Venue: " + i);

			for (int j = 0; stop != true; j++) {

				if (venueList[i][j][0] != null) {

					boolean stop2 = false;
					for (int m = 0; stop2 != true; m++) {
						if (venueList[i][j][m] != null) {
							System.out.println("Source ID: " + venueList[i][j][m].getSourceID()
									+ "| Client ID: " + venueList[i][j][m].getClientID()
									+ "| RSSI: " + venueList[i][j][m].getRssi() + "| Distance: "
									+ venueList[i][j][m].getDistance() + "| Timestampt: "
									+ venueList[i][j][m].getTimestamp());
						} else {
							stop2 = true;
						}
					}
				} else {
					stop = true;
				}
			}
		}
	}

}
