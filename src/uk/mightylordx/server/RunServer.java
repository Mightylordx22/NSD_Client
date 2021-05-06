package uk.mightylordx.server;

import uk.mightylordx.utils.sql.SQLExecutor;

import java.io.IOException;

public class RunServer {

    public static void main(String[] args) throws IOException {
        SQLExecutor.createDatabase();
        Server server = new Server();
        server.getSocket();

    }

}
