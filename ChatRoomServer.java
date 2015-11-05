import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.lang.InterruptedException;

/**
 * @author Yifei
 * This is the class of ChatRoomServer
 * which is the server receives and sends messages
 */

public class ChatRoomServer {
	
	/**
	 * Attributes
	 */
	private final int MSG_Q_SIZE = 10000;
	private static int portNumber = 13042;
	private ServerSocket serverSocket;
	private ConcurrentHashMap<Socket, PrintWriter> connections = 
			new ConcurrentHashMap<Socket, PrintWriter>();
	private BlockingQueue<String> msgQ = 
			new ArrayBlockingQueue<String>(MSG_Q_SIZE);
	
	/**
	 * constructor
	 * the main thread is used to handle new connections, 
	 * and MessageHandler thread is used to handle the message queue.
	 * 
	 * @param port  the port number this server will be listening
	 */
	public ChatRoomServer(int portNumber) throws IOException, 
				SocketException, InterruptedException {
		
		serverSocket = new ServerSocket(portNumber);
		String ipAddress = "0.0.0.0";
		
		try {
			InetAddress address = InetAddress.getLocalHost();
			ipAddress = address.getHostAddress();
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		System.out.format("Chat Room Server (%s) is ready and listening at port %d\n", ipAddress, portNumber);
		MessageHandler messageHandler = new MessageHandler();
		new Thread(messageHandler).start();
		listen();
	}

	/**
	 * main function
	 * @param args port number
	 */
	public static void main (String[] args) {
		try {
			if (args.length > 0) {
				portNumber = Integer.parseInt(args[0]);
			}
			new ChatRoomServer(portNumber);
		} catch(SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen to the specific port.
	 */
	public void listen() throws IOException, InterruptedException {
		while (true) {
			Socket incoming = serverSocket.accept();
			PrintWriter toClient = new PrintWriter(incoming.getOutputStream());
			toClient.println("You have joined Chat Room");
			toClient.flush();
			connections.put(incoming, toClient);
			ChatServerThread cst = new ChatServerThread(incoming);
			new Thread(cst).start();
			String welcome = String.format("%s joined Chat Room.", cst.getName());
			msgQ.put(welcome);
			System.out.println(welcome);
		}
	}

	/**
	 * Remove connection from the client list.
	 *
	 * @param socket  the socket needs to be removed
	 * @param clientName  the name of the corresponding client  
	 */
	public void removeConnection(Socket socket, String clientName) {
		try {
			PrintWriter toClient = new PrintWriter(socket.getOutputStream());
			toClient.println("You have left Chat Room.");
			toClient.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(clientName + " left Chat Room.");
		connections.remove(socket);
		
		try {
			msgQ.put(clientName + " left Chat Room.");
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MessageHandler Class.
	 * This class will check the message when necessary.
	 * If there is any message in the queue, it will send 
	 * the message one by one in FIFO manner.
	 * Sent message will be removed from the queue.
	 */
	class MessageHandler implements Runnable {
		/**
		 * Implement run function of the MessageHandler Runnable.
		 */
		public void run() {
			try {
				while (true) {
					//take method will retrieve and remove the head of this queue, waiting if necessary until an element becomes available.
					sendToEachClient(msgQ.take());
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Send message to each client that in the client list
		 *
		 * @param msg
		 */
		private void sendToEachClient(String msg) {
			for (Enumeration<PrintWriter> ele = connections.elements(); ele.hasMoreElements(); ) {
				PrintWriter toClient = ele.nextElement();
				toClient.println(msg);
				toClient.flush();
			}
		}
	}

	/**
	 * ChatServerThread Class
	 */
	class ChatServerThread implements Runnable {
		private String clientName;
		private Socket incomingSocket;
		private Scanner fromClient;

		/**
		 * ChatServerThread constructor
		 *
		 * @param incomingSocket
		 */
		public ChatServerThread(Socket incomingSocket) throws IOException {
			this.incomingSocket = incomingSocket;
			fromClient = new Scanner(incomingSocket.getInputStream());
			this.clientName = fromClient.nextLine();
		}
    /**
     * getName 
     */
		public String getName() {
			return this.clientName;
		}

		/**
		 * Implement run function in Runnable.
		 * Listen to the client, if there is any message, 
		 * it will put the message into the message queue.
		 */
		public void run() {
			try {
				while (true) {
					if (fromClient.hasNextLine()) {
						msgQ.put(this.clientName + ": " + fromClient.nextLine());
					} else {
						break;
					}
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			} finally {
				ChatRoomServer.this.removeConnection(incomingSocket, this.clientName);
			}
		}
	}
}
