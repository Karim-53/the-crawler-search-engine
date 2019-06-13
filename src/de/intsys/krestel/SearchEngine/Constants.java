package de.intsys.krestel.SearchEngine;

public class Constants {
	private static final String GUARDIAN_API_KEY = "2d6d0790-aee6-41d4-94e6-035f016fb2e1";
	public static final String GUARDIAN_WEB_ROOT = "https://www.theguardian.com/";
	private static final String GUARDIAN_API_ROOT = "http://content.guardianapis.com/search";
	private static final String GUARDIAN_API_world = "http://content.guardianapis.com/world";

	public static final int MIN_ARTICLE_TXT_LENGHT = 100; //FIXME: Correct typo
	private static final int PAGE_PER_QUERY = 200;//200 possible
	public static final int MAX_PAGE_CRAWLED = 191; //191 max
	public static final String CSV_SEPARATOR = ",";
	public static final String LIST_SEPARATOR = ";";
	public static final boolean OVERWITE_FILE = false;
	public static final boolean APPEND_FILE = true;
	public static final String ROOT_DIR = "TheCrawlers\\media\\SearchEngineTheCrawlers\\";
	
	
	private static final String GARDIAN_QUERY_OPT = String.format(
			"?api-key=%s&show-tags=contributor,keyword&show-fields=bodyText" +
					"&page-size=%d", Constants.GUARDIAN_API_KEY, Constants.PAGE_PER_QUERY);
	public static final String GUARDIAN_QUERY = GUARDIAN_API_ROOT + GARDIAN_QUERY_OPT;
	public static final String GUARDIAN_QUERY_WORLD = GUARDIAN_API_world + GARDIAN_QUERY_OPT;// GUARDIAN_QUERY +"&tag=politics/politics"; 200 articles/week chwaya


	
	public static String[] stopwords = {"a", "able", "about",
	        "across", "after", "all", "almost", "also", "am", "among", "an",
	        "and", "any", "are", "as", "at", "b", "be", "because", "been",
	        "but", "by", "c", "can", "cannot", "could", "d", "dear", "did",
	        "do", "does", "e", "either", "else", "ever", "every", "f", "for",
	        "from", "g", "get", "got", "h", "had", "has", "have", "he", "her",
	        "hers", "him", "his", "how", "however", "i", "if", "in", "into",
	        "is", "it", "its", "j", "just", "k", "l", "least", "let", "like",
	        "likely", "m", "may", "me", "might", "most", "must", "my",
	        "neither", "n", "no", "nor", "not", "o", "of", "off", "often",
	        "on", "only", "or", "other", "our", "own", "p", "q", "r", "rather",
	        "s", "said", "say", "says", "she", "should", "since", "so", "some",
	        "t", "than", "that", "the", "their", "them", "then", "there",
	        "these", "they", "this", "tis", "to", "too", "twas", "u", "us",
	        "v", "w", "wants", "was", "we", "were", "what", "when", "where",
	        "which", "while", "who", "whom", "why", "will", "with", "would",
	        "x", "y", "yet", "you", "your", "z"};

}
