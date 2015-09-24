import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Chat Room
 * @author Yifei
 * This is the class of ChatClient
 */

public class ChatClient {
	/**
	 * main method
	 */	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				MyFrame myFrame = new MyFrame();
				myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				myFrame.setVisible(true);
			}
		});
	}
}


/**
 * MyFrame class
 */
class MyFrame extends JFrame {
	/**
	 * Attributes
	 */
	private JTextArea textArea = new JTextArea();
	private String ipAddress = "10.0.0.3";
	private int portNumber = 13042;
	private String userName = "UserName";
	private Client client;
	private Thread clientThread = null;
	private JTextField ip;
	private JTextField port;
	private JTextField name;
	/**
	 * constructor
	 */
	public MyFrame() {
		setTitle("Chat Client");
		setSize(600,600);
		setResizable(false);
		
		JPanel panel = new JPanel();
		ip = new JTextField(ipAddress,15);
		port = new JTextField(Integer.toString(portNumber),5);
		name = new JTextField(userName);
		final JButton connection = new JButton("connect");

		connection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (client == null) {
                        client = new Client(ip.getText(), Integer.parseInt(port.getText()));
                        connection.setText("disconnect");
                        if (clientThread == null) 
                        	clientThread = new Thread(client);
                        clientThread.start();
                    }
                   	else if(client != null) {
                    	client.close();
                    	client = null;
                    	clientThread = null;
                    	connection.setText("connect");
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
		panel.add(ip);
		panel.add(port);
		panel.add(name);
		panel.add(connection);
		add(panel,BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setEditable(false);
		add(scrollPane);
		
		final JTextField chatBox = new JTextField(40);
		chatBox.addActionListener(new ActionListener() {
			private String message = null;
			public void actionPerformed(ActionEvent event) {
				message = chatBox.getText();
				client.send(message);
				chatBox.setText("");
			}
		});
		add(chatBox, BorderLayout.SOUTH);
	}
	
	/**
	 * Client class
	 */
	class Client implements Runnable {
		/**
		 * attributes
		 */
		private Socket server;
		private Scanner fromServer;
		private PrintWriter toServer;
		private StringBuffer receiveMsg;
		/**
		 * constructor
		 */
		public Client(String ipAddress, int portNumber) throws IOException {
			server = new Socket(ipAddress, portNumber);
			fromServer = new Scanner(server.getInputStream());
			toServer = new PrintWriter(server.getOutputStream());
			this.send(name.getText());
		}
		/**
		 * send message
		 */
		public void send(String message) {
			toServer.println(message);
			toServer.flush();
		}
		/**
		 * close printwriter and socket
		 */
		public void close() {
            try {
                toServer.close();
                server.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
		/**
		 * implement run method in runnable
		 */
		public void run() {
            while(true) {
            	if(fromServer.hasNextLine()) {
                    receiveMsg = new StringBuffer();
                    receiveMsg.append(fromServer.nextLine());
                    receiveMsg.append("\n");
                    textArea.append(receiveMsg.toString());
                }
            }
        }
    }

}