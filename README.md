Project: Multi-User Chat Room

1. About the implementation.
This project was built with Java. 
Used Java Socket and Server Socket to implement the TCP connection.
Used Java Swing to implement a GUI for users.
Used Thread to handle multiple users and connections.

2. Files included.
There are three files in this folder. 
README.md: explaination of this application
ChatClient.java: the java source code file of the Client
ChatRoomServer.java: the java source code file of the Server

3. About the application.
This application allows mutiple users chat to the others 
and have conversation together through the Internet.

At first, this application requires a server running ChatRoomServer 
to listen on a particular port with an IP address. (CharRoomServer.java)

Then, the user could run ChatClient to connect to the server
and send messages to the server. 
The server will receive the messages and broadcast them to all clients.

4. How to run this application.
For the server, just compile the ChatRoomServer.java 
using command: javac ChatRoomServer.java
Then run the server using command: java ChatRoomServer [PortNumber]
The server will run and listen to the port with PortNumber.

For the user, compile the ChatClient.java
using command: javac ChatClient.java
Then use command: java ChatClient
to run the client program. 
Then you are free to talk to the world.

