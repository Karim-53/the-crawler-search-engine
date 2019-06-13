package de.intsys.krestel.SearchEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

// Don't change this file!
public abstract class SearchEngine {

	// Replace 'Y' with your team name.
	String baseDirectory = "./TheCrawlers/media/";
	String directory;
	String logFile;

	public SearchEngine() {

		// Directory to store index and result logs
		this.directory = this.baseDirectory + this.getClass().getSimpleName().toString() + "/runtime";
		new File(this.directory).mkdirs();
		this.logFile = this.directory + "/" + System.currentTimeMillis() + ".log";
	}

	synchronized void log(String line) {

		try {
			FileWriter fw = new FileWriter(this.logFile, true);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(line + "\n");
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	abstract boolean loadIndex(String directory);

	abstract void index(String directory);

	abstract ArrayList<String> search(String query, int topK, int prf);

	abstract Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> myRanking, int at);

	/**
	 * Crawls all articles of the online newspaper that has been published on a specific day,
	 * stores them to {@code baseDirectory} and returns the number of stored articles.
	 * 
	 * @param newspaper		Identifying name for newspaper (e.g. "Times of India", "The Guardian", etc.)
	 * @param day			Specify day on which articles has been published.
	 * @return 				Number of crawled articles.
	 */
	abstract int crawlNewspaper(String newspaper, Date day);
}
