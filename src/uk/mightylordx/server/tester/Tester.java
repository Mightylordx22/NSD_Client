package uk.mightylordx.server.tester;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.mightylordx.utils.enums.RequestCommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Tester {

    public static void main(String[] args) throws IOException {


        if (args[0].equalsIgnoreCase("R")) {

            MessageTester m1 = new MessageTester(10, 1000, 1);
            m1.start();

        } else if (args[0].equalsIgnoreCase("L")) {

            MessageTester m1 = new MessageTester(10, 1000, 1);
            MessageTester m2 = new MessageTester(10, 1000, 2);
            MessageTester m3 = new MessageTester(10, 1000, 3);
            m1.start();
            m2.start();
            m3.start();

        } else if (args[0].equalsIgnoreCase("H")) {

            MessageTester m1 = new MessageTester(1000, 0, 1);
            MessageTester m2 = new MessageTester(1000, 0, 2);
            MessageTester m3 = new MessageTester(1000, 0, 3);
            MessageTester m4 = new MessageTester(1000, 0, 4);
            MessageTester m5 = new MessageTester(1000, 0, 5);
            MessageTester m6 = new MessageTester(1000, 0, 6);
            MessageTester m7 = new MessageTester(1000, 0, 7);
            MessageTester m8 = new MessageTester(1000, 0, 8);
            MessageTester m9 = new MessageTester(1000, 0, 9);
            MessageTester m10 = new MessageTester(1000, 0, 10);
            m1.start();
            m2.start();
            m3.start();
            m4.start();
            m5.start();
            m6.start();
            m7.start();
            m8.start();
            m9.start();
            m10.start();

        } else {
            MessageTester m1 = new MessageTester(1, 1000, 1);
            m1.start();
        }


    }

}

class MessageTester extends Thread {

    private static final String SERVERIP = "127.0.0.1";
    private static final int PORT = 12345;
    private final int identifier;
    private final int numberOfMessage;
    private final int delay;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public MessageTester(int number, int period, int id) throws IOException {
        this.numberOfMessage = number;
        this.delay = period;
        this.identifier = id;
        this.socket = new Socket(SERVERIP, PORT);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader((this.socket.getInputStream())));

    }

    @Override
    public void run() {

        assert in != null;
        JSONObject obj = new JSONObject();
        obj.put("_class", RequestCommands.OPEN_COMMAND.toString());
        obj.put("identity", "tester" + this.identifier);
        this.out.println(obj);

        try {
            parseResponse(in);
            Thread.sleep(delay);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        obj = new JSONObject();
        obj.put("_class", RequestCommands.SUBSCRIBE_COMMAND.toString());
        obj.put("identity", "tester" + this.identifier);
        obj.put("channel", "tester" + this.identifier);
        this.out.println(obj);


        try {
            parseResponse(in);
            Thread.sleep(delay);
            for (int i = 1; i < numberOfMessage + 1; i++) {
                obj = new JSONObject();
                JSONObject sendMessage = new JSONObject();
                sendMessage.put("from", "tester" + this.identifier);
                sendMessage.put("_class", "Message");
                sendMessage.put("body", "This is message: " + i);
                sendMessage.put("when", 0);
                obj.put("_class", RequestCommands.PUBLISH_COMMAND.toString());
                obj.put("identity", "tester" + this.identifier);
                obj.put("message", sendMessage);
                this.out.println(obj);
                try {
                    parseResponse(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        obj = new JSONObject();
        obj.put("_class", RequestCommands.GET_COMMAND.toString());
        obj.put("identity", "tester" + this.identifier);
        obj.put("after", "0");
        this.out.println(obj);

        try {
            parseResponse(in);
            Thread.sleep(delay);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        obj = new JSONObject();
        obj.put("_class", RequestCommands.UNSUBSCRIBE_COMMAND.toString());
        obj.put("identity", "tester" + this.identifier);
        obj.put("channel", "tester" + this.identifier);
        this.out.println(obj);

        try {
            parseResponse(in);
            Thread.sleep(delay);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        obj = new JSONObject();
        obj.put("_class", RequestCommands.LOGOUT_COMMAND.toString());
        obj.put("identity", "tester" + this.identifier);
        this.out.println(obj);

        try {
            parseResponse(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void parseResponse(BufferedReader in) throws IOException {
        String temp = in.readLine();
        JSONObject serverResp = new JSONObject(temp);
        String serverCommand = (String) serverResp.get("_class");
        if (serverCommand.equalsIgnoreCase("SuccessResponse")) {
            System.out.println("[CLIENT] Success!");
        } else if (serverCommand.equalsIgnoreCase("ErrorResponse")) {
            System.out.println("[CLIENT] " + serverResp.get("error"));
        } else if (serverCommand.equalsIgnoreCase("MessageListResponse")) {
            JSONArray data = serverResp.getJSONArray("messages");
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                System.out.printf("From: %s\nSent: %s\n%s\n%n", jsonObject.get("from"), jsonObject.get("when"), jsonObject.get("body"));
            }
        } else if (serverCommand.equalsIgnoreCase("LogoutSuccessResponse")) {
            System.out.println("[CLIENT] Success!");
        }
    }

}