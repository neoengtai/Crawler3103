package app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import crawler.RankableURI;

public class Main {
	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		ConcurrentHashMap<String, List<Object>> visited;
		ConcurrentHashMap<String, Integer> keywords;
		PriorityBlockingQueue<RankableURI> toBeVisited;

		System.out.println("Loading files...");
		visited = loadObjects("visited");
		keywords = loadObjects("keywords");
		toBeVisited = loadObjects("toBeVisited");

		if (visited == null || keywords == null || toBeVisited == null) {
			System.out.println("Error loading files");
			System.exit(0);
		}

		int option = 0;

		while (option != 5) {
			System.out.print("1. All pages\n" + "2. Top keywords\n" + "3. Pages to be crawled\n"
					+ "4. Search by keywords\n" + "5. Quit\n" + "Enter option: ");

			option = sc.nextInt();
			sc.nextLine(); // Clear the buffer

			switch (option) {
			case 1:
				viewAllCrawled(visited);
				break;
			case 2:
				viewAllKeywords(keywords);
				break;
			case 3:
				viewAllPending(toBeVisited);
				break;
			case 4:
				break;
			case 5:
				System.out.println("Bye");
				break;
			default:
				System.out.println("Invalid!");
				break;
			}
		}
		sc.close();
	}

	public static void viewAllCrawled(ConcurrentHashMap<String, List<Object>> visited) {
		String option;
		Set<String> keys = visited.keySet();
		Iterator<String> i = keys.iterator();

		do {
			for (int j = 0; j < 10 && i.hasNext(); j++) {
				String k = i.next();
				List<Object> v = visited.get(k);

				if (v.size() != 0) {
					long responseTime = (long) v.get(0);
					ArrayList<String> keywords = (ArrayList<String>) v.get(1);
					System.out.format("URL: %s\nResponse time: %d ms\n", k, responseTime);
					System.out.println("Keywords: " + keywords);
				} else {
					System.out.println("URL: " + k + "\n" + "Response time: Nil\n" + "Keywords: Nil");
				}
			}

			if (i.hasNext()) {
				System.out.print("More--");
				option = sc.nextLine();
			} else {
				option = "q";
			}
		} while (!option.equalsIgnoreCase("q"));
	}

	public static void viewAllKeywords(ConcurrentHashMap<String, Integer> keywords) {
		String option;
		Set<String> keys = keywords.keySet();
		Iterator<String> i = keys.iterator();

		do {
			for (int j = 0; j < 10 && i.hasNext(); j++) {
				String k = i.next();
				System.out.println(k + ": " + keywords.get(k) + " hits");
			}

			if (i.hasNext()) {
				System.out.print("More--");
				option = sc.nextLine();
			} else {
				option = "q";
			}
		} while (!option.equalsIgnoreCase("q"));
	}
	
	public static void viewAllPending(PriorityBlockingQueue<RankableURI> toBeVisited){
		String option;
		Iterator<RankableURI> i = toBeVisited.iterator();

		System.out.println("Number of links: "+toBeVisited.size());
		do {
			for (int j = 0; j < 10 && i.hasNext(); j++) {
				RankableURI uri = i.next();
				System.out.println(uri.getRank()+": "+uri.getURI());
			}

			if (i.hasNext()) {
				System.out.print("More--");
				option = sc.nextLine();
			} else {
				option = "q";
			}
		} while (!option.equalsIgnoreCase("q"));
	}

	public static <T> T loadObjects(String filename) {
		T ret = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			ret = (T) ois.readObject();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} catch (ClassNotFoundException e) {

		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}
