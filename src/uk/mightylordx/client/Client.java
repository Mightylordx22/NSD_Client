package uk.mightylordx.client;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.mightylordx.utils.enums.ClientCommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private final Socket socket;
    private String name = null;
    private String currentChannel = null;

    public Client() throws IOException {
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        System.out.println("[CLIENT] Connected to Server");
    }


    public void readData() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter output = new PrintWriter(this.socket.getOutputStream(), true);
            while (true) {
                System.out.print(">>> ");
                String clientInput = keyboardInput.readLine();
                boolean validCommand = false;
                String[] commands = clientInput.replaceAll("\\s+", " ").trim().split(" ", 2);
                JSONObject object = new JSONObject();
                if (commands.length > 1) {
                    if (commands[0].equalsIgnoreCase(ClientCommands.OPEN_COMMAND.toString())) {
                        if (this.name != null) {
                            System.out.println("[CLIENT] You are already have an open channel please logout");
                            continue;
                        }
                        if (commands[1].replaceAll("\\s+", " ").trim().split(" ").length > 1) {
                            System.out.println("[CLIENT] Channel names can not include whitespace");
                            continue;
                        } else {
                            object.put("_class", "OpenRequest");
                            this.name = commands[1].toLowerCase();
                            object.put("identity", this.name);
                            this.currentChannel = this.name;
                            validCommand = true;
                        }
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.PUBLISH_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        } else {
                            JSONObject sendMessage = new JSONObject();
                            sendMessage.put("from", this.name);
                            sendMessage.put("_class", "Message");
                            sendMessage.put("body", commands[1]);
                            sendMessage.put("when", 0);
                            object.put("_class", "PublishRequest");
                            object.put("identity", this.currentChannel);
                            object.put("message", sendMessage);
                            validCommand = true;
                        }
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.SUBSCRIBE_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                        if (commands[1].replaceAll("\\s+", " ").trim().split(" ").length > 1) {
                            System.out.println("[CLIENT] Channel names can not include whitespace");
                            continue;
                        } else {
                            object.put("_class", "SubscribeRequest");
                            object.put("identity", this.name);
                            object.put("channel", commands[1]);
                            validCommand = true;
                        }
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.UNSUBSCRIBE_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                        if (commands[1].replaceAll("\\s+", " ").trim().split(" ").length > 1) {
                            System.out.println("[CLIENT] Channel names can not include whitespace");
                            continue;
                        } else {
                            object.put("_class", "UnsubscribeRequest");
                            object.put("identity", this.name);
                            object.put("channel", commands[1]);
                            validCommand = true;
                        }
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.GET_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                        if (commands[1].replaceAll("\\s+", " ").trim().split(" ").length > 1) {
                            System.out.println("[CLIENT] Too many arguments see [README.txt]");
                            continue;
                        } else {
                            object.put("_class", "GetRequest");
                            object.put("identity", this.currentChannel);
                            object.put("after", commands[1]);
                            validCommand = true;
                        }
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.LOGOUT_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                    } else {
                        System.out.println("[CLIENT] Sorry that is not valid see [README.txt] for more!");
                    }
                } else {
                    if (commands[0].equalsIgnoreCase(ClientCommands.GET_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                        object.put("_class", "GetRequest");
                        object.put("identity", this.currentChannel);
                        object.put("after", 0);
                        validCommand = true;
                    } else if (commands[0].equalsIgnoreCase(ClientCommands.LOGOUT_COMMAND.toString())) {
                        if (this.name == null) {
                            System.out.println("[CLIENT] Please open a channel first");
                            continue;
                        }
                        object.put("_class", "LogoutRequest");
                        object.put("identity", this.currentChannel);
                        validCommand = true;
                    } else {
                        System.out.println("[CLIENT] That is not a valid command see [README.txt] for more!");
                    }
                }
                if (validCommand) {
                    output.println(object);
                    String temp = input.readLine();
                    JSONObject serverResp = new JSONObject(temp);
                    String serverCommand = (String) serverResp.get("_class");
                    if (serverCommand.equalsIgnoreCase("SuccessResponse")) {
                        System.out.println("[CLIENT] Success!");
                        if (commands[0].equalsIgnoreCase(ClientCommands.SUBSCRIBE_COMMAND.toString()))
                            this.currentChannel = commands[1];
                        else if (commands[0].equalsIgnoreCase(ClientCommands.UNSUBSCRIBE_COMMAND.toString()))
                            this.currentChannel = this.name;
                    } else if (serverCommand.equalsIgnoreCase("ErrorResponse")) {
                        System.out.println("[CLIENT] " + serverResp.get("error"));
                        if (serverResp.get("error").toString().contains("THE IDENTITY")) {
                            this.name = null;
                        }
                    } else if (serverCommand.equalsIgnoreCase("MessageListResponse")) {
                        JSONArray data = serverResp.getJSONArray("messages");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject = data.getJSONObject(i);
                            System.out.printf("From: %s\nSent: %s\n%s\n%n", jsonObject.get("from"), jsonObject.get("when"), jsonObject.get("body"));
                        }

                    } else if (serverCommand.equalsIgnoreCase("LogoutSuccessResponse")) {
                        System.out.println("[CLIENT] Success!");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[CLIENT] Something went wrong: " + e.getMessage());
        } finally {
            System.out.println("[CLIENT] Closing Connection");
        }

    }
}
