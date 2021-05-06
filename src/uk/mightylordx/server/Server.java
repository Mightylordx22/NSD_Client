package uk.mightylordx.server;


import uk.mightylordx.utils.enums.Throughput;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 12345;
    private final ServerSocket serverSocket;


    private final ArrayList<ClientHandler> clients;
    private final ExecutorService pool = Executors.newFixedThreadPool(Throughput.HIGH.getThroughputLimit());


    public Server() throws IOException {
        this.clients = new ArrayList<>();
        this.serverSocket = new ServerSocket(PORT);
    }

    public void getSocket() {
        try {
            while (true) {
                System.out.println("[SERVER] Waiting for client on port " + this.serverSocket.getLocalPort() + "...");
                Socket client = this.serverSocket.accept();
                System.out.println("[SERVER] Client connected on port: " + client.getLocalPort());
                System.out.println("[SERVER] Number of clients connected: ");

                ClientHandler clientThread = new ClientHandler(client);
                this.clients.add(clientThread);
                this.pool.execute(clientThread);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
