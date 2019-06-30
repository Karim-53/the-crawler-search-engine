package de.intsys.krestel.SearchEngine;

public class Main {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		long estimatedTime = System.currentTimeMillis();

		String query = "Trump AND Putin";

		System.out.println("Application started...");
		SearchEngineTheCrawlers searchEngineTheCrawlers = new SearchEngineTheCrawlers();

		// SearchEngineTheCrawlers.crawl(); // Step 0


		//SearchEngineTheCrawlers.workOffline();//Step 1


		//searchEngineTheCrawlers.index(null);//Step 2

		searchEngineTheCrawlers.loadIndex(null);//Step 3




		//Step 4 : query
		//searchEngineTheCrawlers.search(query,10,0);
		//searchEngineTheCrawlers.search("Trump Putin",10,0);

		//searchEngineTheCrawlers.search("Germany",10,0);
		//searchEngineTheCrawlers.search("Tropical fish",10,0);
		searchEngineTheCrawlers.search("Mexico refugees wall",10,0);


		/*


		Scanner input = new Scanner(System.in);
		while_loop:
		while (true) {
			System.out.println("Input your query : ");
			query = input.nextLine();
			System.out.println(query);
			if (query.contentEquals("stop123")) {
				break while_loop;
			}
			searchEngineTheCrawlers.search(query,10,0);
		}
*/
		System.out.println("Application stopped...");

	}

}