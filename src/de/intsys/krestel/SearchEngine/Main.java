package de.intsys.krestel.SearchEngine;
import java.util.Scanner;
public class Main {

	public static void main(String[] args) {
		//String str = "aaa\\|bbb|ddd";
		//System.out.println(str.replace("\\|",""));
		//String[] x = "aaaa|BBBB|cd".split("[|]");
		//String[] y = "aaaa|BBBB|cd".split(`|`);
		// System.out.println(Constants.ROOT_DIR); this is now an empty string
		//long estimatedTime = System.currentTimeMillis();

		//System.out.println("==============Crawlers Search Engine================");
		SearchEngineTheCrawlers searchEngineTheCrawlers = new SearchEngineTheCrawlers();
		//SearchEngineTheCrawlers.correctDB();
		//searchEngineTheCrawlers.crawl(); // Step 1

		//searchEngineTheCrawlers.index(null);//Step 2

		//System.out.println(Constants.ROOT_DIR+"idxDico");

		searchEngineTheCrawlers.loadIndex(null);//Step 3

		//Step 4 : query
		String query = "\"make America great again\""; //"\"make America great again\""; //"Trump AND Putin";
		//searchEngineTheCrawlers.search(query,10,0);
		//searchEngineTheCrawlers.search("Trump Putin",10,0);

		//searchEngineTheCrawlers.search("Germany",10,0);
		//searchEngineTheCrawlers.search("Tropical fish",10,0);
		//searchEngineTheCrawlers.search("g20",10,0);
		//searchEngineTheCrawlers.search("12",10,0);
		/*searchEngineTheCrawlers.search("Merkel NOT Trump",10,0);
		searchEngineTheCrawlers.search("San Francisco",10,0);
		searchEngineTheCrawlers.search("apple tree",10,0);
		searchEngineTheCrawlers.search("G20 Xi Trump meeting",10,0);
		searchEngineTheCrawlers.search("\"to be or not to be\"",10,0);
		searchEngineTheCrawlers.search("Mexico USA wall border",10,0);
		searchEngineTheCrawlers.search("US-China trade war",10,0);
		searchEngineTheCrawlers.search("Japan whaling\n",10,0);
		searchEngineTheCrawlers.search("\"fridays for future\"",10,0);
		searchEngineTheCrawlers.search("\"Commission President\"",10,0);
		searchEngineTheCrawlers.search("12",10,0);
		searchEngineTheCrawlers.search("allah",10,0);
		searchEngineTheCrawlers.search("trump",10,0);
*/
		Scanner input = new Scanner(System.in);
		while_loop:
		while (true) {
			System.out.println("============================================Crawlers Search Engine========================================================");


			System.out.println("Input your query below: ");
			query = input.nextLine();
			//System.out.println(query);
			//startTime1 = System.currentTimeMillis();
			if (query.contentEquals("stop123")) {
				break while_loop;
			}
			searchEngineTheCrawlers.search(query,10,0);
		}
		//*/
		System.out.println("Application stopped...");


	}

}