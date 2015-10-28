package crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	public static void main(String[] args) throws IOException {
		File dir = new File(".");
		String loc = dir.getCanonicalPath() + File.separator + "visited.txt";
		FileWriter fstream = new FileWriter(loc, true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.newLine();
		out.close();

		// Start recursive crawling
		processPage("http://www.nus.edu.sg");

		File file = new File(loc);

		if (file.delete()) {

		}
	}

	// Check if a url is contained in the records
	public static boolean checkExist(String s, File fin) throws IOException {

		FileInputStream fis = new FileInputStream(fin);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));

		String aLine = null;
		while ((aLine = in.readLine()) != null) {
			if (aLine.trim().contains(s)) {
				// System.out.println("contains " + s);
				in.close();
				fis.close();
				return true;
			}
		}
		in.close();
		fis.close();

		return false;
	}

	public static Document httpGet(String url) throws IOException {
		URI uri;
		String path;
		Socket sock = null;
		StringBuilder sb = new StringBuilder();
		
		try{
			uri = new URI(url);
		} catch (URISyntaxException e){
			return null;
		}
		
		// URL pre-processing
		if (!uri.getScheme().equalsIgnoreCase("http")) {
			return null;
		}
		if (uri.getPath() == null || uri.getPath().equals("")) {
			path = "/";
		} else {
			path = uri.getPath();
		}
		
		try {
			// Create the socket to http default port (80)
			sock = new Socket(uri.getHost(), 80);
			sock.setSoTimeout(5000); // 5 seconds timeout
			PrintWriter pw = new PrintWriter(sock.getOutputStream());

			// Send raw http commands to server
			pw.print("GET " + path + " HTTP/1.1\r\n");
			pw.print("Host: " + uri.getHost() + "\r\n");
			pw.print("Accept: text/html,application/xhtml+xml,application/xml;q=0.9\r\n");
			pw.print("User-Agent: Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36\r\n");
			pw.print("\r\n");
			pw.flush();

			// Get data from http server
			InputStreamReader ir = new InputStreamReader(sock.getInputStream());
			BufferedReader br = new BufferedReader(ir);
			String httpStatus, s;
			String responseHeaders[];
			int contentLength = 0;

			// Receive and parse http response headers
			httpStatus = br.readLine();
			while (!(s = br.readLine()).equals("")) {
				responseHeaders = s.split(": ", 2);

				switch (responseHeaders[0]) {
				case "Content-Length":
					contentLength = Integer.parseInt(responseHeaders[1]);
					break;
				default:
					break;
				}
			}
			
			// Receive the rest of response headers and contents
			if (httpStatus.equals("HTTP/1.1 200 OK")){
				// Content length is specified, just read for that amount
				if (contentLength != 0){
					while (contentLength > 0){
						sb.append(br.read());
						contentLength--;
					}
				} else {
				// Content length not specified, attempt to read until end of stream or timeout occurs
					while ((s = br.readLine()) != null) {
						sb.append(s);
					}
				}
			}
		} catch (UnknownHostException e) {
			// Host not found
			return null;
		} catch (ConnectException e) {
			// Error connecting to server
			return null;
		} catch (SocketException e) {
			// Underlying TCP error
			return null;
		} catch (SocketTimeoutException e){
			// Do nothing and let jsoup parse whatever data that has been received
		} finally {
			if (sock != null){
				sock.close();
			}
		}
		return Jsoup.parse(sb.toString());
	}

	public static void processPage(String URL) throws IOException {

		File dir = new File(".");
		String loc = dir.getCanonicalPath() + File.separator + "visited.txt";

		File file = new File(loc);

		// Check if url already visited
		boolean e = checkExist(URL, file);
		if (!e) {
			System.out.println("Visiting :  " + URL);
			// insert to visited records
			FileWriter fstream = new FileWriter(loc, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(URL);
			out.newLine();
			out.close();

			Document doc = null;
			try {
				doc = httpGet(URL);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}

			// Get all urls (hrefs) that exists in the document
			if (doc != null) {
				Elements questions = doc.select("a[href]");
				for (Element link : questions) {
					processPage(link.attr("abs:href"));
				}
			} else {
				// TODO write it to file instead
				System.out.println("Invalid: " + URL);
			}
		} else {
			// url already visited, do nothing
			return;
		}

	}
}