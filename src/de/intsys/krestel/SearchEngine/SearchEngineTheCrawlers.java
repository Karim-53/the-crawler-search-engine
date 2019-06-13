package de.intsys.krestel.SearchEngine;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.istack.internal.Nullable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.text.Normalizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.*;

public class SearchEngineTheCrawlers extends SearchEngine {


	static String stringMitChar = "";
	static Set<String> tokens = new HashSet<String>();
	
	public SearchEngineTheCrawlers() {
		super();// This should stay as is! Don't add anything here!
	}

	@Override
	void index(String dir) {
		// FIXME Auto-generated method stub
	}

	@Override
	boolean loadIndex(String directory) {
		// FIXME Auto-generated method stub
		return false;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		// FIXME Auto-generated method stub
		return null;
	}

	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int ndcgAt) {
		// FIXME Auto-generated method stub
		return null;
	}

	/**
	 * Implement a Java method using the provided template that crawls the newspaper articles for a given date. The method should return a csv file
	 */
	@Override
	int crawlNewspaper(String newspaper, Date day) {
		return crawlNewspaper(newspaper, day, day);
	}

	int crawlNewspaper(String newspaper, Date start_day, Date end_end) {
		if (newspaper.compareTo("The Guardian")!=0){//If both the strings are equal then this method returns 0
			System.out.println("this works only for The Guardian");
			return 0;
		}
		System.out.println("From "+ start_day + " to "+ end_end);
		
		String rootUrl = Constants.GUARDIAN_QUERY_WORLD;

		if (start_day == null) {//look for all articles
			//DONE start from the oldest so we can resume concat this opt to the url
			rootUrl +="&order-by=oldest";
		}else {//crawl for a specific day
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String start_date = sdf.format(start_day);
			String end_date = sdf.format(end_end);
			rootUrl += String.format(
					"&from-date=%s&to-date=%s",
					start_date, end_date);
		}
		try {

			int ArticleCrawled = 0;
			FileWriter csvWriter1 = CSVInit(new SimpleDateFormat("yyyy").format(start_day) );
			String targetUrl;
			for (int pageNb=1; pageNb < Constants.MAX_PAGE_CRAWLED; pageNb++) {

				//FIXME fix the prob related to this link
				//http://content.guardianapis.com/search?api-key=2d6d0790-aee6-41d4-94e6-035f016fb2e1&show-tags=contributor,keyword&show-fields=bodyText&page-size=200&order-by=oldest&page=191
				//{"response":{"status":"error","message":"Content API does not support paging this far. Please change page or page-size."}}
				targetUrl = String.format(rootUrl + "&page=%d", pageNb);
				System.out.println(targetUrl);
				if ( pageNb==190) {
					log(targetUrl,"uncomplete.log");
				}
				//try {Thread.sleep(2000);} catch (Exception e) {}

				String content;
				while (true) {
					try {
						WebFile webFile = new WebFile(targetUrl);
						content = (String) webFile.getContent();
						break;
					}catch (java.net.UnknownServiceException | java.net.SocketTimeoutException e) { System.out.println("Cx error, query again...");}
					catch (java.io.IOException e) { System.out.println("IOException with WebFile, query again..."); }
				}
				content = content.replaceAll("\u0000", "");
				//System.out.println(content);

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				JsonNode jsonNode = objectMapper.readTree(content);

				JsonNode tmp = jsonNode.get("message");
				ArrayNode resultsNode = null;
				try {
					resultsNode = (ArrayNode) tmp;
				} catch (java.lang.ClassCastException e) {
					//5000 queries exeeded (maybe)
					//{"message":"API rate limit exceeded"}
					System.out.println(tmp);
					log(tmp.toString(), Constants.ROOT_DIR+"errors.log");
					try {Thread.sleep(5*60000);} catch (Exception ee) {}
				}
				if (resultsNode != null) {
					System.out.println("error with this targetUrl:");
					System.out.println(targetUrl);

					System.out.println("resultsNode:");
					System.out.println(resultsNode);

					throw new RuntimeException("Error while crawling");
				}
				resultsNode = (ArrayNode) jsonNode.get("response").get("results");
				if (resultsNode==null) {
					System.out.println(content);
					System.out.println(jsonNode);
				}
				for (int i = 0; i < resultsNode.size() ; i++) {
					//System.out.println("Article nb:");
					System.out.print(" "+i+" ");
					ArticleCrawled += ExtractInfoFromArticle(csvWriter1, resultsNode.get(i)); // .get("webUrl")
				}
				System.out.println(".");

				csvWriter1.flush();
				Article.savetotNbArticles();


				System.out.println("################");
				System.out.println("Last targetUrl");
				System.out.println(targetUrl);

				if (resultsNode.size() != 200 ) { break; }

			}
			csvWriter1.close();
			log(tokens.toString(), "tokens.txt");
			//System.out.println("############################################");
			//System.out.println(stringMitChar);
			//BufferedWriter writer = new BufferedWriter(new FileWriter("mitChar2.txt"));
			//writer.write(stringMitChar);
			//writer.close();

			return ArticleCrawled;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			//FIXME: CSVInit Exception
			System.out.println(e);
			e.printStackTrace();
			return -2;
		}
	}
		static void CrawlTheWeb() {
			Calendar calendar_start = Calendar.getInstance();
			Calendar calendar_end = Calendar.getInstance();
			for(int year=2005;year<=2018;year++) {
				for(int month=1;month<=11;month++) {
					calendar_start.set(year, month, 01);
					calendar_end.set((month+1==13 ? year+1 : year), (month+1==13 ? 01 : month+1), 01);
					System.out.println(calendar_start.getTime() + " to " + calendar_end.getTime() );

					int total = new SearchEngineTheCrawlers().crawlNewspaper("The Guardian", calendar_start.getTime(), calendar_end.getTime() );
					//int total = new SearchEngineTheCrawlers().crawlNewspaper("The Guardian", null);

					System.out.println("Total articles: " + total);
				}
			}
		}
		
		
	int ExtractInfoFromArticle(FileWriter csvWriter1, JsonNode JsonArticle) {


		//FIXME: check if .get("type") == "article"
		//FIXME: exeption key->val inexistant
		//article id
		JsonNode nodeId = JsonArticle.get("id");
		if (nodeId==null) {return 0;}
		String article_uid = nodeId.toString();
		//article url
		String article_url = JsonArticle.get("webUrl").toString();
		//System.out.println(article_url);
		//otherwise use this: System.out.println(Constants.GUARDIAN_WEB_ROOT + JsonArticle.get("id"));

		//article_authors
		JsonNode tags = JsonArticle.get("tags");
		List<String> authors = new ArrayList<>();
		List<String> categories = new ArrayList<>();
		for (JsonNode tag : tags)
		{
			if ("\"contributor\"".compareTo(tag.get("type").toString())==0) {
				authors.add(tag.get("webTitle").toString());
			}
			if ("\"keyword\"".compareTo(tag.get("type").toString())==0) {
				categories.add(tag.get("webTitle").toString());
			}
			//FIXME: log a warning cause i don't think we are getting an other type
		}
		//String article_authors = authors.toString();
		//System.out.println(article_authors);
		//FIXME: UnitTest : no author "world/2019/may/03/german-police-close-down-dark-web-marketplace"

		//article text
		String article_text = JsonArticle.get("fields").get("bodyText").toString();
		//System.out.println("article text:");
		//System.out.println(article_text);
		if (article_text.length()<Constants.MIN_ARTICLE_TXT_LENGHT){
			//FIXME: UnitTest no text content "crosswords/weekend/435"
			System.out.println("We scipped this article because it have a very short $article_text");
			return 0;
		}
		//article_text = SearchEngineTheCrawlers.TokenizeBody(article_text);// we will do this when we transform offline data to index
		/*
		//match any ponctuation that we left
		Pattern pattern = Pattern.compile("(?i)( [^ ]*[^ \\w]+[^ ]*)");
		Matcher matcher = pattern.matcher(article_text);
		if (matcher.find())
		{
			//System.out.println("##########################################################");
			//for (int i=1; i <= matcher.groupCount() ;i++)
				//System.out.println(matcher.group(i));
				//stringMitChar += "\n"+ matcher.group(i);
		
		}
		*/

		//article headline
		String article_headline = JsonArticle.get("webTitle").toString();
		//System.out.println("headline:");
		//System.out.println(article_headline);

		//publication timestamp
		String publication_timestamp = JsonArticle.get("webPublicationDate").toString();
		//System.out.println(publication_timestamp);
		//Z suffix means UTC, java.util.SimpleDateFormat doesn’t parse it correctly, you need to replace the suffix Z with ‘+0000’.
		// ++ https://stackoverflow.com/questions/44705738/format-date-and-time-in-string-format-from-an-api-response

		//article categories
		//String article_categories = categories.toString();
		//System.out.println(article_categories);
		
		Article article = new Article( null, article_uid,article_url,authors,article_text,article_headline,publication_timestamp,categories);
		//article_text = TokenizeMinimumChange(article_text);
		
		try {
			toCSV(csvWriter1, article);
		} catch (Exception e) {
			//FIXME: CSVInit exeption
		}

		article.stemNTokenize();
		tokens.addAll(article.getUniqueTokens());
		log(""+tokens.size(), "tokenSize");
		log(article.toString(), "LightDB.csv");
		
		
		
		return 1;
	}

	
	FileWriter CSVInit(String date) throws IOException {
		FileWriter csvWriter1= new FileWriter(Constants.ROOT_DIR+"\\full\\"+date+".csv", Constants.APPEND_FILE);

		/* Kim: i had some issue with this: dome times i find it written multiple time in a file
		 * 
		 * csvWriter1.append("article id, article uid, ");//FIXME only if file do not exists
		csvWriter1.append("article url, ");
		csvWriter1.append("[article authors], ");
		csvWriter1.append("article text, ");
		csvWriter1.append("article headline, ");
		csvWriter1.append("publication timestamp, ");
		csvWriter1.append("[article categories]\n");*/
		return csvWriter1;
	}

	void toCSV(FileWriter csvWriter1,Article article) throws IOException {
		csvWriter1.append(article.toString());
		csvWriter1.append("\n");
	}

	static void workOffline() {
		FileWriter fTokenSize;
		FileWriter fNonUniqueTokenSize;
		FileWriter fLightDB;
		BufferedWriter outTokenSize;
		BufferedWriter outLightDB;
		BufferedWriter outNonUniqueTokenSize;
		try {
			fTokenSize = new FileWriter("tokenSize", Constants.OVERWITE_FILE);
			fLightDB = new FileWriter("LightDB.csv", Constants.OVERWITE_FILE);
			fNonUniqueTokenSize = new FileWriter("NonUniqueTokenSize.csv", Constants.OVERWITE_FILE);
			outTokenSize = new BufferedWriter(fTokenSize);
			outLightDB = new BufferedWriter(fLightDB);
			outNonUniqueTokenSize = new BufferedWriter(fNonUniqueTokenSize);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		BufferedReader br = null;
		String line = "";
		//for(int year = 1999 ; year<=1999;year++) {
		long totTokens = 0L;
		long totAfter = 0L;
		long totBefore = 0L;
			try {
				br = new BufferedReader(new FileReader("offline.csv"));
				br.readLine();//skip line 1
				int i=0;
				while ((line = br.readLine()) != null) {
					i++; 
					if (i%500==1) {System.out.print(".");}
					String[] part = line.split(Constants.CSV_SEPARATOR);
					if ("article id"==part[0]) {continue;}
					int nb = 0;
					try {
						nb = Integer.valueOf(part[0]);
					}catch (Exception e) {
						System.out.println(part[0]);
						System.out.println(e);
					}
					Article article = new Article( new Integer( nb ), part[1], part[2], Arrays.asList(part[3].split(Constants.LIST_SEPARATOR)),part[4],
							part[5],part[6],Arrays.asList(part[7].split(Constants.LIST_SEPARATOR)));
					totBefore +=  article.text.length() +article.headline.length();
					article.stemNTokenize();
					totAfter +=  article.text.length() +article.headline.length();
					
					tokens.addAll(article.getUniqueTokens());
					log(""+tokens.size(), outTokenSize);
					totTokens += article.getNonUniqueTokens().length;
					log(""+totTokens, outNonUniqueTokenSize);
					log(article.toString(), outLightDB);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println(totBefore);
				System.out.println(totAfter);
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		//}
		try {
			outNonUniqueTokenSize.close();
			fNonUniqueTokenSize.close();
			outTokenSize.close();
			fTokenSize.close();
			outLightDB.close();
			fLightDB.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(tokens.toString(), "tokens.txt");

	}
	synchronized static void log(String line, String FilePath) {

		try {
			FileWriter fw = new FileWriter(FilePath, true);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(line + "\n");
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	synchronized static void log(String line, BufferedWriter out) {
		try {
			out.write(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
