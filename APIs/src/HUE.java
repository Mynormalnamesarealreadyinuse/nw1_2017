import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class HUE {
    public static final String bridge = "10.28.9.120";
    public static final String authorizedUser = "197ea42c25303cef1a68c4042ed56887";
    ArrayList<String> lampStates = new ArrayList<String>();
	
//	public static void main(String[] args){
//		HUE client = new HUE();
//		client.doRequest();
//	}
	
	public void initialize() {
		lampStates.add(0, "white");
		lampStates.add(1, "white");
		lampStates.add(2, "white");
		this.doRequest();
	}
	
	
	public void doRequest(){
		try(Socket s = new Socket(bridge, 80);
				BufferedWriter toBridge = 
						new BufferedWriter(
								new OutputStreamWriter(
										s.getOutputStream()));
				BufferedReader fromBridge =
						new BufferedReader(
								new InputStreamReader(
										s.getInputStream()))){
			toBridge.write("GET /api/" + authorizedUser + "/lights/1/state" + " HTTP/1.0\r\n");
			toBridge.write("Host: " + bridge + "\r\n");
			toBridge.write("\r\n");
			toBridge.flush();
			
			for(String line = fromBridge.readLine(); line != null /*&& line.length()>0*/; line = fromBridge.readLine()) {
				System.out.println(line);
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public void setLights(int i, boolean timeExceeded, long remainingTime) {
		if (remainingTime > 2 && !timeExceeded) {
			if (lampStates.get(i).equals("white")) {
				return;
			}
			else {
				lampStates.set(i, "white");
				// mache request
			}
		}
		else if (remainingTime <= 2 && remainingTime > 1) {
			if (lampStates.get(i).equals("orange")) {
				return;
			}
			else {
				lampStates.set(i, "orange");
				// mache request
			}
		}
		else if (remainingTime <= 1 && remainingTime >= 0) {
			if (lampStates.get(i).equals("red")) {
				return;
			}
			else {
				lampStates.set(i, "red");
				// mache request
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
				// mache request
			}
		}
	}
	
	
}
