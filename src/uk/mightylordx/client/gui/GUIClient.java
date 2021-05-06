package uk.mightylordx.client.gui;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GUIClient {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String name = null;
    private String currentChannel = null;

    public GUIClient() throws IOException {
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
    }


    public boolean login(JSONObject object) throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        output.println(object);
        String temp = input.readLine();
        JSONObject serverResp = new JSONObject(temp);
        String serverCommand = (String) serverResp.get("_class");
        if (serverCommand.equals("SuccessResponse")) {
            this.name = (String) object.get("identity");
            this.currentChannel = this.name;
            return true;
        }
        return false;
    }

    public void logout() throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        object.put("_class", "LogoutRequest");
        object.put("identity", this.getName());
        output.println(object);
    }

    public JSONArray getMessages() throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        object.put("_class", "GetRequest");
        object.put("identity", this.currentChannel);
        object.put("after", 0);
        output.println(object);
        String temp = input.readLine();
        try {
            JSONObject serverResp = new JSONObject(temp);
            String serverCommand = (String) serverResp.get("_class");
            if (serverCommand.equals("SuccessResponse")) {
            } else {
                return (JSONArray) serverResp.get("messages");
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public String getName() {
        return this.name;
    }


    public void subscribe(String name) throws Exception {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        object.put("_class", "SubscribeRequest");
        object.put("identity", this.name);
        object.put("channel", name.toLowerCase());
        output.println(object);
        String temp = input.readLine();
        JSONObject serverResp = new JSONObject(temp);
        String serverCommand = (String) serverResp.get("_class");
        if (!serverCommand.equals("ErrorResponse")) {
            this.currentChannel = name;
        }

    }

    public String getCurrentChannel() {
        return this.currentChannel;
    }

    public void sendMessage(String message, String image) throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        JSONObject sendMessage = new JSONObject();
        sendMessage.put("from", this.name);
        sendMessage.put("_class", "Message");
        sendMessage.put("body", message);
        if (image != null) {
            sendMessage.put("pic", image);
        }
        sendMessage.put("when", 0);
        object.put("_class", "PublishRequest");
        object.put("identity", this.currentChannel);
        object.put("message", sendMessage);
        output.println(object);
    }

    public void unsubscribe() throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        object.put("_class", "UnsubscribeRequest");
        object.put("identity", this.name);
        object.put("channel", this.name);
        output.println(object);
        String temp = input.readLine();
        JSONObject serverResp = new JSONObject(temp);
        String serverCommand = (String) serverResp.get("_class");
        if (!serverCommand.equals("ErrorResponse")) {
            this.currentChannel = this.name;
        }

    }

    public JSONArray search(String search) throws IOException {
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        JSONObject object = new JSONObject();
        object.put("_class", "SearchRequest");
        object.put("identity", this.currentChannel);
        object.put("words", search);
        output.println(object);
        String temp = input.readLine();
        JSONObject serverResp = new JSONObject(temp);
        return (JSONArray) serverResp.get("messages");
    }

}
