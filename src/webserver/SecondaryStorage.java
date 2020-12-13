/*
 * CS 5523
 * Multi-threaded Web Server
 * Gabriela Boentges
 * ctj784
 * 12/13/2019
 */
package webserver;

import java.io.File;

/*
 * It will make a list of all files to be found in the server
 * and will be in charge of checking if files requested by a client
 * are found in the server or not, as well as sending back
 * file handles of files requested
 */
public class SecondaryStorage {
	static final File WEB_ROOT = new File("src/webserver");
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "501.html";
	static final String BAD_REQUEST = "400.html";
	private static String[] websites;
	static int Counter;
	static int Counter2;
	
	public SecondaryStorage() {
		Counter = 0;
		Counter2 = 0;
		populateWebsites();
	}
	
	// Populates list of files in the server
	private void populateWebsites() {
		if(WEB_ROOT.exists() && WEB_ROOT.isDirectory()) {
			File arr[] = WEB_ROOT.listFiles();
			numberOfFiles(arr,0,0);
			websites = new String[Counter];
			RecursivePrint(arr,0,0);
		}
	}
	
	// Looks to see if file requested is in the list
	public Boolean findSite(String fileName) {
		for(int i = 0; i < websites.length && !websites[i].isEmpty(); i++) {
			if(fileName.toLowerCase().equals(websites[i].toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	// Returns file handle of file requested by client
	public File getSite(String fileName) {
		File file = new File(WEB_ROOT, fileName);
		return file;
	}
	
	// Sends file handle of File Not Found HTML file
	public File fileNotFound() {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		return file;
	}
	
	// Sends file handle of Method Not Supported HTML file
	public File methodNotSupported() {
		File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
		return file;
	}
	
	// Sends file handle of Bad Request HTML file
	public File badRequest() {
		File file = new File(WEB_ROOT, BAD_REQUEST);
		return file;
	}
	
	// Checks files within directories in the server
	static void numberOfFiles(File[] arr, int index, int level) {
		if(index == arr.length)
			return;
		
		if(arr[index].isFile()) {
			Counter++;
		} else if (arr[index].isDirectory()) {
			numberOfFiles(arr[index].listFiles(),0,level+1);
		}
		
		numberOfFiles(arr,++index,level);
	}
	
	static void RecursivePrint(File[] arr, int index, int level) {
		if(index == arr.length)
			return;
		
		if(arr[index].isFile()) {
			String pathname = arr[index].getPath();
			pathname = pathname.substring(14);
			pathname = pathname.replace("\\", "/");
			websites[Counter2] = pathname;
			Counter2++;
		} else if (arr[index].isDirectory()) {
			RecursivePrint(arr[index].listFiles(),0,level+1);
		}
		
		RecursivePrint(arr,++index,level);
	}
}
