package beacon.kontakt.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Caller {
	public static void main(String[] args) throws IOException {
		URL url = new URL("localhost:8080/");
		String query = "INSERT_HERE_YOUR_URL_PARAMETERS";

		// make connection
		URLConnection urlc = url.openConnection();

		// use post mode
		urlc.setDoOutput(true);
		urlc.setAllowUserInteraction(false);

		// send query
		PrintStream ps = new PrintStream(urlc.getOutputStream());
		ps.print(query);
		ps.close();

		// get result
		BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String l2 = null;
		while ((l2 = br.readLine()) != null) {
			System.out.println(l2);
		}
		br.close();
	}

}
