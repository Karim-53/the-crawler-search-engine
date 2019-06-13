package de.intsys.krestel.SearchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sun.istack.internal.Nullable;

import opennlp.tools.stemmer.PorterStemmer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;

public class Article {
	static int totNbArticles;
	static 
    {//https://ourcodeworld.com/articles/read/839/how-to-read-parse-from-and-write-to-ini-files-easily-in-java
		totNbArticles = Integer.valueOf(readFileAsString("theCrawler_totNbArticles"));
    }

	static PorterStemmer stem1 = new PorterStemmer();
	public int nb;
	public String uid;
	public String url;
	public List<String> authors;
	public String text;
	public String headline;
	public String publication_timestamp;
	public List<String> categories;
	public static HashSet<String> hs = new HashSet<String>();//stopwords list
	//Article article = new Article(article_nb,article_uid,article_url,article_authors,article_text,
	// article_headline,publication_timestamp,article_categories);
	public Article() {
		this(null);
	}	
	public Article(@Nullable Integer article_nb) {
		authors = new ArrayList<String>();
		categories = new ArrayList<String>();
		if (article_nb==null) {
		nb = totNbArticles;
		totNbArticles++;
		}else {
			nb = article_nb;
		}
	}
	public Article(@Nullable Integer article_nb, String article_id,String article_url, List<String> article_authors,String article_text,
	  String article_headline,String article_publication_timestamp,
	  List<String> article_categories) {
		this(article_nb);
		uid=tokenizeMinimumChange(article_id);
		url=encodeComma(article_url);
		authors=article_authors.stream()
                .map(s -> tokenizeMinimumChange(s))
                .collect(Collectors.toList());
		text=tokenizeMinimumChange(article_text);
		headline=tokenizeMinimumChange(article_headline);
		publication_timestamp=tokenizeMinimumChange(article_publication_timestamp);
		categories = article_categories.stream()
                .map(s -> tokenizeMinimumChange(s))
                .collect(Collectors.toList());		
	}
	
	private String encodeComma(String article_url) {
		return article_url.replaceAll(",", "%2C");
	}
	public void stemNTokenize() {
		text = PorterStem(TokenizeBody(text));
		headline = PorterStem(TokenizeTitle(headline));
		//FIXME wwwapplecom
	}
	public Set<String> getUniqueTokens() {
		String str = text + " "+ headline;
		String[] parts = str.split(" ");
		return new HashSet<String>(Arrays.asList(parts));
	}
	public String[] getNonUniqueTokens() {
		String str = text + " "+ headline;
		String[] parts = str.split(" ");
		return parts;
	}
	public static String PorterStem(String txt) { 
		String[] tokens = txt.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			if(hs.contains(tokens[i])) {
			tokens[i]="";}
			else {
			tokens[i]=stem1.stem(tokens[i]);}
			}
		txt=String.join(" ",tokens);
		
		return txt.trim().replaceAll(" +", " ");
		
	}
	/**
	 * 
	 * @param txt Input Txt
	 * @return Tokenized txt for crawling and storing
	 */
	public static String tokenizeMinimumChange(String txt) {
		txt = txt.replaceAll("\u0000", "")
				.toLowerCase()
				.replaceAll("\\s", " ")
				.replaceAll("[\\v]", " ")	//vertical spaces (newline)
				.trim().replaceAll(" +", " ");
		
		txt = txt.replaceAll("(\\d)(,)(\\d)", "$1$3"); //separateur de millier
		txt = txt.replaceAll("(\\w)([,/])(\\w)", "$1 $3")
				.replaceAll("(\\.){2,}"," ");
		txt = txt.replaceAll(",", " "); //no comma at all: we need this for storing
		
		return txt;
	}
	public static String TokenizeBody(String txt) {
		txt = txt.replaceAll("(\\d)", "")
				.replaceAll("(\\.)", "");
		return TokenizeTitle(txt);
	}
	public static String TokenizeTitle(String txt){
		txt = Article.tokenizeMinimumChange(txt);
		txt = Normalizer.normalize(txt, Normalizer.Form.NFD)
				.replaceAll("æ", "oe")//FIXME: improve this
				.replaceAll("[\\(\\)_\\?\"%§!{}\\[\\]=+~#<>]", " ")
				.replaceAll("&amp;?", " ")
				.replaceAll("ß", "ss");
		txt = " "+txt+" ";//yes i need this here
		txt = txt.replaceAll("[’']s ", " ");// Ex: it’s // the user may use the second quote
		txt = txt.replaceAll("[’']t ", "t ");
		txt = txt.replaceAll("[’']", "");

		txt = txt.replaceAll("[^\\p{ASCII}]", "");//FIXME: leg these chars before dropping them

		
		txt = txt.replaceAll("([a-z])(\\.)([a-z])", "$1$3");
		txt = txt.replaceAll("([a-z])(\\.)([a-z])", "$1$3");//yes it have to be written 2 time :( but not 3 XD

		//can be done later we might need this to scape &amp StringEscapeUtils.unescapeHtml4()
		//should we keep H&M ? may cause prob during executing a query


		txt = txt.replaceAll("( [^ \\w]+ )"," ");

		//At the end
		txt = txt.replaceAll("(\\w)([^\\w]) ", "$1 ");//any punctuation at the end of the phrase will be dropped
		txt = txt.replaceAll(" ([^\\w])(\\w)", " $2");//FIXME: i think this is too general
		// this is creating too many tokens :( txt = txt.replaceAll(" (\\w+)-(\\w+)", " $1 $2 $1$2"); //we can add both aaa-bbb version aaa bbb and then aaabbb //FIXME: should we limit this to alpha char ?
		txt = txt.replaceAll(" (\\w+)-(\\w+)", " $1 $2"); //we can add both aaa-bbb version aaa bbb and then aaabbb //FIXME: should we limit this to alpha char ?
		txt = txt.replaceAll("(\\w)-(\\w)", "$1 $2")
				.replaceAll("(\\w)-(\\w)", "$1 $2"); //need it twice //FIXME: only solution for word with multiple hyfen
		txt = txt.replaceAll("([^\\w\\.])+", " ")
				.replaceAll("([^\\d])(\\.)([^\\d])", "$1 $3")//FIXME MAybe we should round numbers XD
				.trim().replaceAll(" +", " ");

		return  txt.trim();
	}
	
	public static void StopWords()
	{
	    int len= Constants.stopwords.length;
	    for(int i=0;i<len;i++)
	    {
	        hs.add(Constants.stopwords[i]);
	    }
	   
	}
	

	public String toString() {
		StringBuilder s = new StringBuilder(); 
		s.append(nb);
		s.append(", ");
		s.append(uid);
		s.append(", ");
		s.append(url);
		s.append(", ");
		s.append(String.join(Constants.LIST_SEPARATOR,authors));
		s.append(", ");
		s.append(text);
		s.append(", ");
		s.append(headline);
		s.append(", ");
		s.append(publication_timestamp);
		s.append(", ");
		s.append(String.join(Constants.LIST_SEPARATOR,categories));
		//s.append("\n");
		return s.toString();
	}
	public static String readFileAsString(String fileName) 
	{ 
		try {
			String data = ""; 
			data = new String(Files.readAllBytes(Paths.get(fileName))); 
			return data;
		}catch (Exception e){
			return "1";
		}
	}
	public static void savetotNbArticles() {
		try {
			FileWriter fileWriter = new FileWriter("theCrawler_totNbArticles", false); //overwrites file
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    //printWriter.print("Some String");
		    printWriter.printf("%d",totNbArticles);
		    printWriter.close();
		} catch (Exception e) {
			System.err.format("IOException: %s%n", e);
		}
		
	}
}
//FIXME add a method that goes through the crawled articles and chek if there is any duplicates