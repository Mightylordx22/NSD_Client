package uk.mightylordx.client.gui;


import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.mightylordx.utils.Utils;
import uk.mightylordx.utils.enums.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MessageListener extends Thread {


    private final GUIClient client;
    private final ListView<String> listView;
    private final boolean exit = false;
    int oldMessagesLength;
    JSONArray oldMessages;
    private boolean scrolled;


    public MessageListener(GUIClient client, ListView<String> listView) {
        this.client = client;
        this.listView = listView;
        this.scrolled = false;
    }

    @Override
    public void run() {
        try {
            while (!exit) {
                Platform.runLater(() -> {
                    JSONArray temp = null;
                    try {
                        temp = client.getMessages();
                        if (temp == null) {
                            int index = listView.getItems().size();
                            listView.scrollTo(index);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (temp != null) {
                        if (temp.length() > oldMessagesLength) {
                            listView.getItems().clear();
                            HashMap<Integer, String> imagePlaces = new HashMap<>();
                            int count = 0;
                            for (Object object : temp) {
                                JSONObject t = (JSONObject) object;
                                String from = t.getString("from");
                                String message = t.getString("body");
                                listView.getItems().add(from + ": " + message);

                                try {
                                    String text = t.getString("pic");
                                    imagePlaces.put(count, text);
                                } catch (Exception ignored) {
                                }
                                count++;
                            }
                            listView.setCellFactory(p -> new ListCell<>() {
                                public final ImageView imageView = new ImageView();

                                @Override
                                public void updateItem(String name, boolean empty) {
                                    super.updateItem(name, empty);
                                    if (empty) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        setText(name);
                                        var t = imagePlaces.get(super.getIndex());
                                        if (t != null) {
                                            try {
                                                imageView.setImage(Utils.decodeImage(t));
                                                imageView.setFitWidth(Config.PICTURE_WIDTH.get());
                                                imageView.setPreserveRatio(true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            setGraphic(imageView);
                                        }
                                    }
                                }
                            });
                            List<String> items = listView.getItems();
                            int index = items.size();
                            oldMessagesLength = temp.length();
                            oldMessages = temp;
                            if (!scrolled) {
                                listView.scrollTo(index);
                                scrolled = !scrolled;
                            }
                        }
                    }
                });
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong " + e.getMessage());
        }
    }
}
