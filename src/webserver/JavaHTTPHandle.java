/*
 * CS 5523
 * Multi-threaded Web Server
 * Gabriela Boentges
 * ctj784
 * 12/13/2019
 */
package webserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/*
 * This thread will manage dedicated connection request from a web browser. It will read the
 * HTTP request and send back an HTTP response depending on the request made by client. It will
 * also log in history the file requested by client as well as any errors that it encounters
 * from the request.
 */
public class JavaHTTPHandle implements Runnable {
	private Socket connect;
	private SecondaryStorage website;
	
	// Obtains client's connection and access to secondary storage
	public JavaHTTPHandle(Socket c, SecondaryStorage a) {
		connect = c;
		website = a;
	}

	@Override
	public void run() {
		BufferedReader in;
		PrintWriter out;
		String fileRequested;
		BufferedOutputStream dataOut;
		
		try {
				// We read characters from the client via input stream on the socket
				in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				// We get first line of the request from the client
				String input = in.readLine();
				
				// We check that the request from client is not empty
				// If it is, it will simply close connection
				if (input == null) {
					in.close();
					connect.close();
					
					log(connect,input);
					return;
				}
				
				System.out.println(new Date() + " [" + connect.getInetAddress().getHostAddress() +
				":" + connect.getPort() + "] " + input);
				
				// We parse the request with a string tokenizer
				StringTokenizer parse = new StringTokenizer(input);
				// We get the HTTP method of the client
				String method = parse.nextToken().toUpperCase();
				// We get file requested
				fileRequested = parse.nextToken().toLowerCase();
				fileRequested = fileRequested.substring(1,fileRequested.length());

				// If no file is requested in given in URL, show home page
				if (fileRequested.equalsIgnoreCase("")) {
					fileRequested = "index.html";
				}
				
				// We get character output steam to client for HTTP headers
				out = new PrintWriter(connect.getOutputStream());
				// Get binary output stream to client for requested data
				dataOut = new BufferedOutputStream(connect.getOutputStream());
				
				// Checks if the request of client has no errors
				// If so, sends back HTTP response of Bad Request error
				if(!(input.endsWith("HTTP/1.1") || input.endsWith("HTTP/1.0"))) {
					File file = website.badRequest();
					int fileLength = (int) file.length();
					byte[] fileData = readFileData(file, fileLength);
					
					out.println(constructHTTPHeader(400,fileLength,"text/html"));
					out.flush(); 

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
					dataOut.close();
					out.close();
					
					log(connect,"400 Bad Request");
					return;
				}
				
				// Checks if HTTP method is GET or HEAD
				if(method.equals("GET") || method.equals("HEAD")){
					String content = getContentType(fileRequested);
					
					// Calls secondary storage to see if file requested
					// is found in server
					if(website.findSite(fileRequested)) {
						// File has been found, so it will now prepare
						// HTTP header with the data of the file
						File file = website.getSite(fileRequested);
						int fileLength = (int) file.length();
						byte[] fileData = readFileData(file, fileLength);
						
						out.println(constructHTTPHeader(200,fileLength,content));
						out.flush();

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
						dataOut.close();
						out.close();
					} else {
						// File was not found in the server so now it will
						// prepare HTTP header with File Not Found error
						File file = website.fileNotFound();
						int fileLength = (int) file.length();
						byte[] fileData = readFileData(file, fileLength);
						
						out.println(constructHTTPHeader(404,fileLength,"text/html"));
						out.flush(); 

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
						dataOut.close();
						out.close();
						
						log(connect,"404 File Not Found");
					}
				} else {
					// HTTP method is not GET or HEAD so now it will prepare
					// HTTP header with Method Not Supported error
					File file = website.methodNotSupported();
					int fileLength = (int) file.length();
					byte[] fileData = readFileData(file, fileLength);
					
					out.println(constructHTTPHeader(501,fileLength,"text/html"));
					out.flush(); 
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
					dataOut.close();
					out.close();
					
					log(connect,"505 Not Implemented");
				}
				
				// Close connection
				in.close();
				connect.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				System.err.println("Unsupported Enconding error: " + e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("IO Exception error: " + e.getMessage());
			}
	}
	
	// Will read in the data of file to be sent in HTTP response
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	// Gets the Content Type of the file to be send in HTTP response
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";
		else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg"))
			return "image/jpeg";
	    else if (fileRequested.endsWith(".gif"))
	        return "image/gif";
	    else if (fileRequested.endsWith(".class"))
	        return "application/octet-stream";
	    else if (fileRequested.endsWith(".css"))
	    	return "text/css";
	    else if (fileRequested.endsWith(".png"))
	    	return "image/png";
	    else if (fileRequested.endsWith(".txt") || fileRequested.endsWith(".java"))
	    	return "text/plain";
		else
			return "text/plain";
	}
	
	// Will construct HTTP header to be sent back to client
	private String constructHTTPHeader(int return_code, int file_size, String file_content) {
		String s = "HTTP/1.1 ";
		
		switch(return_code) {
		case 200:
			s = s + "200 OK";
			break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 404:
			s = s + "404 File Not Found";
			break;
		case 501:
			s = s + "501 Method Not Implemented";
			break;
		}
		
		s = s + "\r\n";
		s = s + "Server: Java HTTP Server\r\n";
		s = s + "Date: " + new Date() + "\r\n";
		s = s + "Content-type: " + file_content + "\r\n";
		s = s + "Content-length: " + file_size + "\r\n";
		
		return s;
	}
	
	// Logs errors putting time, host address, and reason of error
	private static void log(Socket connection, String msg) {
		System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress() +
				":" + connection.getPort() + "] " + msg);
	}
}
