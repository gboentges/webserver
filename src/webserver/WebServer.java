/*
 * CS 5523
 * Multi-threaded Web Server
 * Gabriela Boentges
 * ctj784
 * 12/13/2019
 */
package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

/*
 * This program will be given a port number to be listening into and wait for a client requests.
 * Once it receives a request for connection, it will create a dedicated thread for that client
 * to manage request through there.
 * It will continue to listen until program gets terminated with an interrupt command. 
 */
public class WebServer {

	private static ServerSocket serverSocket;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// Program expects a port number given as a parameter
		if(args.length != 1) {
			System.out.println("Usage: java WebServer <port>");
			System.exit(-1);
		}
		
		int PORT = Integer.parseInt(args[0]);

		try {
			serverSocket = new ServerSocket(PORT);
			
			// Initiate secondary storage for file requests
			SecondaryStorage website = new SecondaryStorage();
			
			System.out.println("Web server starting up on port "+PORT);
			System.out.println("(press ctrl-c to exit)");
			
			// We listen to the port until user halts server execution
			while(true) {
				JavaHTTPHandle myServer = new JavaHTTPHandle(serverSocket.accept(), website);
				
				System.out.println("Connection opened. (" + new Date() + ")");
				
				// We create a dedicated thread to manage the client connection
				Thread client = new Thread(myServer);
				client.start();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Server Connection error: " + e.getMessage());
		}
	}
}
