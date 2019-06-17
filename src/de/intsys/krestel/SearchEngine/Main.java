package de.intsys.krestel.SearchEngine;

import java.util.HashMap;
import java.util.Map;



public class Main {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		long estimatedTime = System.currentTimeMillis();
		Map<String, Long> dictionary = new HashMap<String, Long>();
		String searchResult="";
		String query="";
		Article.StopWords();

		System.out.println("Application started...");
		/*small test
		 * Article article = new Article( 0, "","",Arrays.asList(";".split(Constants.LIST_SEPARATOR)),"aaaaa aaaaa bbbbbb","","",Arrays.asList(";".split(Constants.LIST_SEPARATOR)));
		
		article.stemNTokenize();
		Set<String> tok = article.getUniqueTokens();
		System.out.println(tok.size());
		System.out.println(article.toString());
		System.out.println(article.getNonUniqueTokens().length);
		*/





		//SearchEngineTheCrawlers.workOffline();
		
		/*Calendar start = Calendar.getInstance();
		start.set(2019, 04, 18);
		
		Calendar end = Calendar.getInstance();
		end.set(2019, 05, 01);

		int total = new SearchEngineTheCrawlers().crawlNewspaper("The Guardian", start.getTime(),  end.getTime());*/

		/*
		Calendar calendar = Calendar.getInstance();
		calendar.set(2019, 04, 03);
		System.out.println(calendar.getTime());
		int total = new SearchEngineTheCrawlers().crawlNewspaper("The Guardian", calendar.getTime());
		*/
		//int total = new SearchEngineTheCrawlers().crawlNewspaper("The Guardian", null);
		//System.out.println("Total articles: " + total);

		//SearchEngineTheCrawlers.workOffline();


		//InvertedIndexer.buildIndex("LightDB.csv");

		//InvertedIndexer.buildIndex("LightDB.csv", "NoncompressedIndex.txt");


		InvertedIndexer.buildCompressedIndex("LightDB.csv", "compressedIndex");

		long startTime1 = System.currentTimeMillis();
		//HuffmanEncoding.decode("compressedIndex.dico.key", "Decompressed.Dico.key");
		System.out.println("elapsedTime::  decompress dico.key and write it: "+ (System.currentTimeMillis() - startTime1) );


		/*
		dictionary = InvertedIndexer.buildDict("index.txt");
		//System.out.println(dictionary);
		Scanner input = new Scanner(System.in);
		while_loop:
		while(true) {
			System.out.println("Input your query : ");
			query=input.next();
			startTime = System.currentTimeMillis();
			query=Article.PorterStem(Article.TokenizeTitle(query));
			if(query.contentEquals("stop123")) {
				break while_loop;}
			searchResult=InvertedIndexer.searchQuery(query.toLowerCase(), dictionary);
			estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("Search Results (" + estimatedTime + "ms) : " + searchResult);

		}

		input.close();
		*/
		System.out.println("Application stopped...");
	}

}