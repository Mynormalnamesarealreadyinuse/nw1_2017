import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class HUE {
    public static final String bridge = "10.28.9.121";
    public static final String authorizedUser = "3dc1d8f23e55321f3c049c03ac88dff";
//	public static final String bridge = "localhost";
//	public static final String authorizedUser = "newdeveloper";
    ArrayList<String> lampStates = new ArrayList<String>();
	
	
	public void initialize() {
		lampStates.add(0, "white");
		lampStates.add(1, "white");
		lampStates.add(2, "white");
		this.doRequest(1, "{\"on\":true, \"sat\":0, \"bri\":150,\"hue\":5000}");
		this.doRequest(2, "{\"on\":true, \"sat\":0, \"bri\":150,\"hue\":5000}");
		this.doRequest(3, "{\"on\":true, \"sat\":0, \"bri\":150,\"hue\":5000}");
	}
	
	
	public void doRequest(int i, String body){
		try(Socket s = new Socket(bridge, 80);
				BufferedWriter toServer = 
						new BufferedWriter(
								new OutputStreamWriter(
										s.getOutputStream()));
				BufferedReader fromServer =
						new BufferedReader(
								new InputStreamReader(
										s.getInputStream()))){
			
			toServer.write("PUT /api/" + authorizedUser + "/lights/" + i + "/state HTTP/1.0" + "\r\n");
//			toServer.write("Content-Type: application/json" + "\r\n");
//			toServer.write("Accept: application/json" + "\r\n");
			toServer.write("Content-Length: " + body.length() + "\r\n");
			toServer.write("\r\n");

			toServer.write(body);
			toServer.write("\r\n");
			toServer.flush();
			
			for(String line = fromServer.readLine(); line != null /*&& line.length()>0*/; line = fromServer.readLine()){
				System.out.println(line);
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	public void setLights(int i, boolean timeExceeded, long remainingTime) {
		if (remainingTime > 2*60 && !timeExceeded) {
			if (lampStates.get(i).equals("white")) {
				return;
			}
			else {
				lampStates.set(i, "white");
				this.doRequest(i + 1, "{\"on\":true, \"sat\":0, \"bri\":150,\"hue\":5000}");
			}
		}
		else if (remainingTime <= 2*60 && remainingTime > 1*60) {
			if (lampStates.get(i).equals("orange")) {
				return;
			}
			else {
				lampStates.set(i, "orange");
				this.doRequest(i + 1, "{\"on\":true, \"sat\":254, \"bri\":150,\"hue\":6000}");
			}
		}
		else if (remainingTime <= 1*60 && remainingTime >= 0 && !timeExceeded) {
			if (lampStates.get(i).equals("red")) {
				return;
			}
			else {
				lampStates.set(i, "red");
				this.doRequest(i + 1, "{\"on\":true, \"sat\":254, \"bri\":150,\"hue\":1000}");
			}
		}
		else if (timeExceeded) {
			if (lampStates.get(i).equals("blink")) {
				return;
			}
			else {
				lampStates.set(0, "blink");
				lampStates.set(1, "blink");
				lampStates.set(2, "blink");
				this.blink();
			}
		}
	}


	private void blink() {
		this.doRequest(1, "{\"on\":false}");		// aus
		this.doRequest(2, "{\"on\":false}");
		this.doRequest(3, "{\"on\":false}");
		
		this.doRequest(1, "{\"on\":true, \"sat\":254, \"bri\":150,\"hue\":1000}");		// an
		this.doRequest(2, "{\"on\":true, \"sat\":254, \"bri\":150,\"hue\":1000}");
		this.doRequest(3, "{\"on\":true, \"sat\":254, \"bri\":150,\"hue\":1000}");
		
		while (true) {
			this.doRequest(1, "{\"on\":false}");		// aus
			this.doRequest(2, "{\"on\":false}");
			this.doRequest(3, "{\"on\":false}");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.doRequest(1, "{\"on\":true}");		// an
			this.doRequest(2, "{\"on\":true}");
			this.doRequest(3, "{\"on\":true}");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
