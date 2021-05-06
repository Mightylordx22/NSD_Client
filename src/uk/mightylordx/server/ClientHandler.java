package uk.mightylordx.server;

import org.json.JSONObject;
import uk.mightylordx.utils.enums.Config;
import uk.mightylordx.utils.enums.RequestCommands;
import uk.mightylordx.utils.returnmessages.ReturnMessages;
import uk.mightylordx.utils.sql.SQLExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler implements Runnable {


    private static final ArrayList<String> channels = new ArrayList<>();
    private static final HashMap<Socket, String> clientHashMap = new HashMap<>();
    private static int clientsConnected = 0;
    private final Socket client;
    private final BufferedReader input;
    private final PrintWriter output;

    public ClientHandler(Socket clientSocket) throws IOException {

        this.client = clientSocket;
        this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);

        clientsConnected++;
        System.out.println("[SERVER] Number of clients: " + clientsConnected);

    }


    @Override
    public void run() {
        try {
            while (true) {
                String clientCommand = input.readLine();
                JSONObject jsonObject = new JSONObject(clientCommand);
                String clientRequest = (String) jsonObject.get("_class");
                if (clientRequest.equalsIgnoreCase(RequestCommands.OPEN_COMMAND.toString())) {
                    String identity = (String) jsonObject.get("identity");
                    if (!identity.equals("")) {
                        if (!channels.contains(identity.toLowerCase())) {
                            channels.add(identity);
                            clientHashMap.put(this.client, identity);
                            SQLExecutor.createTable(identity);
                            this.output.println(ReturnMessages.success());
                        } else {
                            this.output.println(ReturnMessages.error("THE IDENTITY ALREADY EXISTS: " + identity));
                        }
                    } else {
                        this.output.println(ReturnMessages.error("NO CHANNEL OPENED"));
                    }
                } else if (clientRequest.equalsIgnoreCase(RequestCommands.PUBLISH_COMMAND.toString())) {
                    JSONObject tempJsonObject = jsonObject.getJSONObject("message");
                    if (tempJsonObject.get("body").toString().length() < Config.CHAR_LIMIT.get()) {
                        SQLExecutor.insertData(jsonObject);
                        this.output.println(ReturnMessages.success());
                    } else {
                        this.output.println(ReturnMessages.error("MESSAGE TOO BIG: " + tempJsonObject.get("body").toString().length() + " characters"));
                    }
                } else if (clientRequest.equalsIgnoreCase(RequestCommands.SUBSCRIBE_COMMAND.toString()) || clientRequest.equalsIgnoreCase(RequestCommands.UNSUBSCRIBE_COMMAND.toString())) {
                    String channelName = (String) jsonObject.get("channel");
                    if (channels.contains(channelName)) {
                        this.output.println(ReturnMessages.success());
                    } else {
                        this.output.println(ReturnMessages.error("CHANNEL DOESN'T EXIST: " + channelName));
                    }
                } else if (clientRequest.equalsIgnoreCase(RequestCommands.GET_COMMAND.toString())) {
                    ArrayList<JSONObject> data = SQLExecutor.getData(jsonObject);
                    this.output.println(ReturnMessages.getRequest(data));
                } else if (clientRequest.equalsIgnoreCase(RequestCommands.LOGOUT_COMMAND.toString())) {
                    this.output.println(ReturnMessages.logoutMessage());
                    break;
                } else if (clientRequest.equalsIgnoreCase(RequestCommands.SEARCH_COMMAND.toString())) {
                    ArrayList<JSONObject> data = SQLExecutor.searchData(jsonObject);
                    this.output.println(ReturnMessages.getRequest(data));
                } else {
                    this.output.println(ReturnMessages.error("INVALID REQUEST: [" + jsonObject + "]"));
                }

            }
        } catch (IOException e) {
            System.err.println("[SERVER] IO Exception in ClientHandler something went wrong with the client");
        } finally {
            System.out.println("[SERVER] Client Disconnecting!");
            clientsConnected--;
            System.out.println("[SERVER] Number of clients: " + clientsConnected);
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channels.remove(clientHashMap.get(this.client));
            clientHashMap.remove(this.client);
            output.close();
        }
    }
}
