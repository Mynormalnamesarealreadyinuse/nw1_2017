import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class GoogleHueAPI {
	
    public static final String API_KEY = "AIzaSyDMSLeqkP1k-rgL9Zh_Ah1p_BXFklrjHH8";
    public static ArrayList<LocalTime> arrivalTimes = new ArrayList<LocalTime>();
    public static ArrayList<String> origins = new ArrayList<String>();
    public static ArrayList<String> destinations = new ArrayList<String>();
    public static ArrayList<String> vehicles = new ArrayList<String>();
    public static ArrayList<String> travelTimes = new ArrayList<String>();
    
    
	public static void main(String[] args){
		readParameters();
		
		GoogleHueAPI client = new GoogleHueAPI();
		HUE hue = new HUE();
		hue.initialize();
		
		while (true) {
			// index i steht für eine Person
			for (int i = 0; i < 3; i++) {
				String url = buildURL(i);
				client.doRequest(url, i);
				
				LocalTime timeNow = LocalTime.now();
				LocalTime departureTime = arrivalTimes.get(i).minusMinutes(Long.parseLong(travelTimes.get(i)));
				boolean timeExceeded = false;
				if (timeNow.plusMinutes(Long.parseLong(travelTimes.get(i))).isAfter(arrivalTimes.get(i))) {
					timeExceeded = true;
				}
				long remainingTime = timeNow.until(departureTime, ChronoUnit.SECONDS);
				
				System.out.println("jetzige Zeit: " + timeNow);
				System.out.println("Arbeitsbeginn: " + arrivalTimes.get(i));
				System.out.println("Reisezeit: " + travelTimes.get(i) + " Minuten");
				System.out.println("Abfahrtszeit: " + departureTime);
				System.out.println("Zeit überschritten: " + timeExceeded);
				System.out.println("verbleibende Zeit: " + remainingTime + " Sekunden");
				
				hue.setLights(i, timeExceeded, remainingTime);
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	

	public void doRequest(String url, int i){
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
					travelTimes.add(i, travelTime);
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
					LocalTime arrivalTimeObject = LocalTime.parse(arrivalTime);
					arrivalTimes.add(arrivalTimeObject);
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
