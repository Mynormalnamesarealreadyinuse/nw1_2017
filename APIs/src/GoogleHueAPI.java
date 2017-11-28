import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GoogleHueAPI {
	
    public static final String host = "www.google.de";
    public static final String API_KEY = "AIzaSyDMSLeqkP1k-rgL9Zh_Ah1p_BXFklrjHH8";
    public static ArrayList<String> arrivalTimes = new ArrayList<String>();
    public static ArrayList<String> origins = new ArrayList<String>();
    public static ArrayList<String> destinations = new ArrayList<String>();
    public static ArrayList<String> vehicles = new ArrayList<String>();
    public static ArrayList<String> travelTimes = new ArrayList<String>();
    
    
	public static void main(String[] args){
		readParameters();
//		System.out.println(arrivalTimes.toString());
//		System.out.println(origins.toString());
//		System.out.println(destinations.toString());
//		System.out.println(vehicles.toString());
		
		GoogleHueAPI client = new GoogleHueAPI();
		
		while (true) {
			for (int personIndex = 0; personIndex < 3; personIndex++) {
				String url = buildURL(personIndex);
				client.doRequest(url);
			}
//			System.out.println(travelTimes.toString());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	

	public void doRequest(String url){
		try(InputStream input = 
				new URL(url).openStream();
				BufferedReader fromServer =
						new BufferedReader(
								new InputStreamReader(
										input))){
			
			for(String line = fromServer.readLine(); line != null /*&& line.length()>0*/; line = fromServer.readLine()){
				
				System.out.println(line);
				if (line.contains("mins")) {
					int indexStart = line.indexOf("\"text\" : \"") + 10;
					int indexEnd = line.indexOf(" mins\"");
					String travelTime = line.substring(indexStart, indexEnd);
					travelTimes.add(travelTime);
				}
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	
	private static String buildURL(int personIndex) {
		String url = "";
		url += "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&";
		url += "origins=" + origins.get(personIndex).replace(" ", "+") + ",Muenchen&";
		url += "destinations=" + destinations.get(personIndex).replace(" ", "+") + ",Muenchen&";
		url += "mode="+ vehicles.get(personIndex) + "&";
		url += "key=" + API_KEY;
		return url;
	}
	
	
	private static void readParameters() {
		String filepath = "parameter.txt";
		
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (line.contains("Arbeitsbeginn")) {
					String arrivalTime = line.substring(15);
					arrivalTimes.add(arrivalTime);
				}
				if (line.contains("Start")) {
					String origin = line.substring(7);
					origins.add(origin);
				}
				if (line.contains("Ziel")) {
					String destination = line.substring(6);
					destinations.add(destination);
				}
				if (line.contains("Verkehrsmittel")) {
					String vehicle = line.substring(16);
					if (vehicle.equals("Auto")) {
						vehicles.add("driving");
					}
					else if (vehicle.equals("zu Fuß")) {
						vehicles.add("walking");
					}
					else if (vehicle.equals("Fahrrad")) {
						vehicles.add("bicycling");
					}
					else if (vehicle.equals("öffentlich")) {
						vehicles.add("transit");
					}
				}
//				System.out.println(line);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
