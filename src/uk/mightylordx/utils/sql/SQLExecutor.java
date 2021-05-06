package uk.mightylordx.utils.sql;


import org.json.JSONObject;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class SQLExecutor {

    private static final String tmpUrl = System.getProperty("user.dir").replace("\\", "/") + "/database.db";
    private static final String url = "jdbc:sqlite:" + tmpUrl;
    private static final SQLiteConfig config = new SQLiteConfig();


    private static Connection connect() {
        config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            config.apply(conn);
        } catch (SQLException e) {
            System.out.println("[SERVER] Error: " + e.getMessage());
        }
        return conn;
    }

    public static void createDatabase() {

        File f = new File(tmpUrl);
        if (f.exists() && !f.isDirectory()) {
            System.out.println("[SERVER] Database Already Exists Not Creating New One");
        } else {
            try (Connection conn = connect()) {
                if (conn != null) {
                    DatabaseMetaData databaseMetaData = conn.getMetaData();
                    System.out.println("[SERVER] The Database Has Been Created");
                }

            } catch (SQLException e) {
                System.out.println("[SERVER] Error: " + e.getMessage());
            }
        }
    }

    public static void createTable(String tableName) {

        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (\n" +
                "\tmessage_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\tfrom_user text,\n" +
                "\tbody text,\n" +
                "\tpic text\n" +
                ");", tableName);
        try (Connection conn = connect();
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
            System.out.println("[SERVER] Successfully created channel: " + tableName);
        } catch (SQLException e) {
            System.out.println("[SERVER] Error: " + e.getMessage());
        }
    }

    public static void insertData(JSONObject object) {
        String sqlStatement;
        String image = null;
        JSONObject tmpObject = object.getJSONObject("message");
        String channel = (String) object.get("identity");
        String from = (String) tmpObject.get("from");
        String body = (String) tmpObject.get("body");
        try {
            image = (String) tmpObject.get("pic");
        } catch (Exception ignored) {
        }
        sqlStatement = String.format("INSERT INTO %s (from_user,body, pic) VALUES (?,?,?);", channel);
        try (Connection conn = connect();
             PreparedStatement insertStatement = conn.prepareStatement(sqlStatement)) {
            insertStatement.setString(1, from);
            insertStatement.setString(2, body);
            insertStatement.setString(3, image);
            insertStatement.execute();
        } catch (SQLException e) {
            System.out.println("[SERVER] Error: " + e.getMessage());
        }
    }

    public static ArrayList<JSONObject> getData(JSONObject object) {
        ArrayList<JSONObject> finalJSONList = new ArrayList<>();
        String timestamp = object.get("after").toString();
        String sqlStatement = String.format("SELECT * FROM %s WHERE message_id >= ?;", object.get("identity"));
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {
            pstmt.setInt(1, Integer.parseInt(timestamp));
            ResultSet data = pstmt.executeQuery();
            while (data.next()) {
                JSONObject temp = new JSONObject();
                String fromUser = data.getString("from_user");
                String textBody = data.getString("body");
                String time = String.valueOf(data.getInt("message_id"));
                String image = data.getString("pic");
                temp.put("from", fromUser);
                temp.put("_class", "Message");
                temp.put("body", textBody);
                temp.put("pic", image);
                temp.put("when", time);
                finalJSONList.add(temp);
            }
            return finalJSONList;
        } catch (SQLException e) {
            System.out.println("[SERVER] Error: " + e.getMessage());
        }
        return null;
    }

    public static ArrayList<JSONObject> searchData(JSONObject object) {
        ArrayList<JSONObject> finalJSONList = new ArrayList<>();
        String words = object.getString("words");
        String sqlStatement = String.format("SELECT * FROM %s WHERE body LIKE ?;", object.get("identity"));
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {
            pstmt.setString(1, "%" + words + "%");
            ResultSet data = pstmt.executeQuery();
            while (data.next()) {
                JSONObject temp = new JSONObject();
                String fromUser = data.getString("from_user");
                String textBody = data.getString("body");
                String time = String.valueOf(data.getInt("message_id"));
                String image = data.getString("pic");
                temp.put("from", fromUser);
                temp.put("_class", "Message");
                temp.put("body", textBody);
                temp.put("pic", image);
                temp.put("when", time);
                finalJSONList.add(temp);
            }
            return finalJSONList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
