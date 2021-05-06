# NSD Assignment Server

### Project Structure

NSD_assisngment_2/ ├── out/ ├── src/      
│ └── uk/ │ └── mightylordx/ │ ├── client/ │ │ ├── gui/ │ │ │ ├── GUI.java │ │ │ ├── GUIClient.java │ │ │ └──
MessageListener.java │ │ ├── Client.java │ │ └── RunClient.java │ ├── server/ │ │ ├── tester/ │ │ │ └── Tester.java │ │
├── ClientHandler.java │ │ ├── RunSever.java │ │ └── Server.java │ └── utils/ │ ├── enums/ │ │ ├── ClientCommands.java │
│ ├── RequestCommands.java │ │ ├── Config.java │ │ └── Throughput.java │ ├── returnmessages/ │ │ └── ReturnMessages.java
│ ├── sql/ │ │ └── SQLExecutor.java │ └── Utils.java │                     
│                    
├── database.db ├── json-20201115.jar ├── sqlite-jdbc-3.32.3.2.jar └── README.md

• The base folder NSD_assisngment_2 has one sub directory src. • The out folder is where all compiled classes go. • uk
is a package • mightylordx is a package • client is a package that holds my own single user input client • server is a
package that holds my server files and tester client • utils is a package • tester is a package that holds the auto
tester client • enums is a package that holds my enums • returnmessages is a package that holds my return messages • sql
is a package that holds my sql commands • database.db is used for data persistence • json-20201115.jar &&
sqlite-jdbc-3.32.3.2.jar are external dependencies

### Extension

• The extension I added was the [Searching] functionality which allows me to search for any message in a channel that a
user is subscribed to • It returns all messages that contain said words • If the messages contains pictures it will also
render the images

### Compilation [Command Line]

• Start by typing the following

```
javac -d out/production/NSD_assisngment_2 -cp json-20201115.jar;sqlite-jdbc-3.32.3.2.jar src/uk/mightylordx/server/*.java src/uk/mightylordx/utils/enums/*.java src/uk/mightylordx/utils/returnmessages/*.java src/uk/mightylordx/utils/sql/*.java src/uk/mightylordx/client/*.java src/uk/mightylordx/server/tester/*.java
```

• Then run the compiled server java by the following

```
java -cp "out\production\NSD_assisngment_2;json-20201115.jar;sqlite-jdbc-3.32.3.2.jar" uk.mightylordx.server.RunServer
```

• Compile the client by running the following

```
javac -d out/production/NSD_assisngment_2 --module-path C:/Users/Adam/Desktop/Coding/java/NSD_assisngment_2/fx/lib --add-modules=javafx.controls -classpath :out/production/NSD_assisngment_2;json-20201115.jar;sqlite-jdbc-3.32.3.2.jar;fx\lib\src.zip;fx\lib\javafx-swt.jar;fx\lib\javafx.web.jar;fx\lib\javafx.base.jar;fx\lib\javafx.fxml.jar;fx\lib\javafx.media.jar;fx\lib\javafx.swing.jar;fx\lib\javafx.controls.jar;fx\lib\javafx.graphics.jar src/uk/mightylordx/client/gui/*.java src/uk/mightylordx/utils/Utils.java src/uk/mightylordx/utils/enums/*.java
```

• To run a single client instance run the following

```
java --module-path C:/Users/Adam/Desktop/Coding/java/NSD_assisngment_2/fx/lib --add-modules=javafx.controls -classpath out\production\NSD_assisngment_2;json-20201115.jar;sqlite-jdbc-3.32.3.2.jar;fx\lib\src.zip;fx\lib\javafx-swt.jar;fx\lib\javafx.web.jar;fx\lib\javafx.base.jar;fx\lib\javafx.fxml.jar;fx\lib\javafx.media.jar;fx\lib\javafx.swing.jar;fx\lib\javafx.controls.jar;fx\lib\javafx.graphics.jar uk.mightylordx.client.gui.GUI
```