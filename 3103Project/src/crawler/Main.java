package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	private static int minRelevanceScore = 1;
	private static Map<String, List<Object>> visited = new HashMap<String, List<Object>>();
	private static Map<String, Integer> keywords = new HashMap<String, Integer>();
	private static ArrayList<String> toBeVisited = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		keywords.put("findings", 0);
		keywords.put("developing", 0);
		toBeVisited.add("http://www.nus.edu.sg");
		int count = 0;
		
		while (count < 5){
			String uri = toBeVisited.get(0);
			toBeVisited.remove(0);
			
			if (!visited.containsKey(uri)){
				processPage(uri);
				count ++;
			}
						
			if (toBeVisited.isEmpty()){
				break;
			}
		}
		
		// Add a breakpoint here and see what's in visited, keywords, and toBeVisited
		System.out.println("Done");
		
		/*
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
		*/
		/* CODE TO WRITE DATA TO FILE
		Map<String, Integer> map = new HashMap();
		map.put("1",new Integer(2));
        map.put("2",new Integer(4));
        map.put("3",new Integer(6));
        
        System.out.println("Original map: ");
        System.out.println(map);
        
        FileOutputStream fos = new FileOutputStream("map.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(map);
        oos.close();

        FileInputStream fis = new FileInputStream("map.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map<String, Integer> anotherMap;
		try {
			anotherMap = (Map) ois.readObject();
			System.out.println(anotherMap);
			ois.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
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

	public static String httpGet(String url) throws IOException {
		URI uri;
		String path;
		Socket sock = null;
		StringBuilder sb = new StringBuilder();

		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
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
			if (httpStatus.equals("HTTP/1.1 200 OK")) {
				// Content length is specified, just read for that amount
				if (contentLength != 0) {
					while (contentLength > 0) {
						sb.append(br.read());
						contentLength--;
					}
				} else {
					// Content length not specified, attempt to read until end
					// of stream or timeout occurs
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
		} catch (SocketTimeoutException e) {
			// Do nothing and return whatever data that has been received
		} finally {
			if (sock != null) {
				sock.close();
			}
		}
		return sb.toString();
	}

	public static void processPage(String URL) {
		String page = null;
		long responseTime = -1;
		int score = 0;
		Document doc;
		ArrayList<String> matchedKeywords = new ArrayList<String>();
		
		try {
			long start_t = System.currentTimeMillis();
			page = httpGet(URL);
			responseTime = System.currentTimeMillis() - start_t;
		} catch (IOException e) {
			visited.put(URL, null);
			System.out.println("Error: "+URL);
			return;
		}
		
		if (page != null){
			doc = Jsoup.parse(page);
		} else {
			visited.put(URL, null);
			System.out.println("Error: "+URL);
			return;
		}

		for (String word : doc.text().split(" ")){
			if (keywords.containsKey(word.toLowerCase())){
				score ++;
				matchedKeywords.add(word.toLowerCase());
				keywords.merge(word.toLowerCase(), 1, Integer::sum);
			}
		}
		
		ArrayList<Object> urlStatistics = new ArrayList<Object>();
		urlStatistics.add(responseTime);
		urlStatistics.add(matchedKeywords);
		visited.put(URL, urlStatistics);
		
		if (score >= minRelevanceScore){
			// Get all urls (hrefs) that exists in the document
			Elements links = doc.select("a[href]");
			for (Element link : links){
				toBeVisited.add(link.attr("abs:href"));
			}
		} else {
			// Since this page isn't relevant, don't append links to the main queue
		}
	}
}