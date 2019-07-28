package de.intsys.krestel.SearchEngine;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.intsys.krestel.SearchEngine.search.BM25;
import javafx.util.Pair;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class SearchEngineTheCrawlers extends SearchEngine {


	static String stringMitChar = "";
	static Set<String> tokens = new HashSet<String>();

	//Map<String, Pair<Integer, Integer>> dictionary = new HashMap<String, Pair<Integer, Integer>>(); // kim: maybe this should not be static but who cares -,-
	IdxDico idxDico;


	public SearchEngineTheCrawlers() {
		super();// This should stay as is! Don't add anything here!
	}

	public static void correctDB() {/*
		Article.lastNonUsedArticleID = 1;
		Article.totArticlesLength = 0;
		Article.nbProcessedArticles = 0;
		try {
			PrintWriter pw = new PrintWriter("corrected_NewYorkTimes.csv");
			String fileName = "full\\NewYorkTimes.csv";
			System.out.println("Reading from " + fileName);
			File f = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(f));

			String line = br.readLine();
			while (line != null) {
				// write to the output file
				line=line.replace("\\|","")
						.replace("\\&",""); //yep we need this :/
				String[] part = line.split(Constants.CSV_REGEX_SEPARATOR);
				// in "article id"|"article url"|"[article authors]"|"article headline"|"article subtitle"|"article description"|"publication timestamp"|"[article categories]"|"[image captions]"
				// out "article_id"|"article_url"|"article_authors"|"article_text"|"article_headline"|"publication_timestamp"|"article_categories"
				if (part[0]=="article_id") continue;

				//if (part.length!=7) { throw new RuntimeException(line+"\n"+ "have more | then expected "+ part.length); }
				Article a = new Article(null, "", part[1], Arrays.asList(part[2].split(Constants.LIST_SEPARATOR)), part[3],
						part[4], part[5], Arrays.asList(part[6].split(Constants.LIST_SEPARATOR)));
				//String debugStr = a.toString();
				pw.println(a.toString());
				line = br.readLine();
			}
			pw.flush();

			System.out.println("correcting Completed");
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	@Override
	void index(String dir) { //FIXME use String dir here
        HashMap<String, Map<Integer, Integer>> preIndex = SearchEngineTheCrawlers.workOffline();//Step 1

        InvertedIndexer.buildCompressedIndex(preIndex, "LightDB.csv", Constants.ROOT_DIR+"compressedIndex");
		InvertedIndexer.createDictOfflinecsv("offline.csv"); ////dictionary for offline.csv. should be run with searchEngineTheCrawlers.index method
		//InvertedIndexer.createDictOfflinecsv1("offline.csv"); faster but still have doubts

        //long startTime1 = System.currentTimeMillis();
        //HuffmanEncoding.decode("compressedIndex.dico.key", "Decompressed.Dico.key");
        //System.out.println("elapsedTime::  decompress dico.key and write it: "+ (System.currentTimeMillis() - startTime1) );
        //System.out.println(InvertedIndexer.articleIDList);
	}

	@Override
	boolean loadIndex(String directory) {// we do not load postings, we load the (token to pos) dico and the (id to article pos) dico
		//dictionary = InvertedIndexer.buildDict("NoncompressedIndex.txt");//step 3 for building dictionary. //TODO This IS WRONG it builds not loads, write dico then read it X(


		/*
		String NonCompressed_key = Article.readFileAsString("compressedIndex.dico.NonCompressed_key");

		try (InputStream inIndex_val = new BufferedInputStream(new FileInputStream(Constants.ROOT_DIR+"compressedIndex.dico.val"), 1024)){
			VByte.decode(inIndex_val);

		} catch (IOException e) {
		e.printStackTrace();
		}
		*/

		idxDico = IdxDico.LoadIdxDicoFromfile();
		this.LoadAllArticles();

		/*
		for (Map.Entry<String, Pair<Integer, Integer>> entry : idxDico.tokenToPostingPos.entrySet() ) {
			//System.out.println(entry.getKey() + "/" + entry.getValue());
			InvertedIndexer.getPostingList( entry.getKey(), idxDico);
		}
		*/
		return true;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		if (query.indexOf("|")>=0){
			String[] part = query.split(Constants.CSV_REGEX_SEPARATOR);
			query = part[0];
		}
		query = query.trim();
		if (!Constants.SilentOutput){System.out.println("query: "+query);}
		long startTime1 = System.currentTimeMillis();
		query = query.replaceAll("\\bUS\\b","USA").toLowerCase();
		boolean IsPhraseQuery = false;
		String exactQuery = query;

		if (query.matches("\".*?\"")){
			//System.out.println("Phrase Queries");
			IsPhraseQuery = true;
			exactQuery = query.substring(1, query.length()-1);
			query=exactQuery;
		}

		query = Article.tokenizeMinimumChange(query);
		String[] queryArray = query.split(" ");
		for (int i = 0; i < queryArray.length; i++) {// the query was npt working intitially because the stopwords where present and in AND it checks for posting list for "stopwords" . Since its
			if(Article.hs2.contains(queryArray[i].toLowerCase())) {//empty. it returned AND [empty set] which gave empty set.hs2 is stopwrords without operator hashmap
				queryArray[i]="";}
			else {
				queryArray[i]=queryArray[i];}
		}
		query= String.join(" ",queryArray);
		query=query.trim().replaceAll(" +", " ");
		if(IsPhraseQuery){
		    query=query.replaceAll("\\b(and|or|not)\\b","").replaceAll(" +"," ").replaceAll("\" ","\"");// this step is done so as to
		    query=query.trim();//work for exact queries containing operators
            query = query.replaceAll(" ","_AND_").replaceAll("_", " ") ;
		}
		//Case 2 : Bool Operator
		Pair< List<LightArticle> , Set<String> > result;
		// if Pattern has some AND OR =>  do a bool OP
		if (Pattern.compile(".*\\b(AND|OR|NOT|INOT)\\b.*", Pattern.CASE_INSENSITIVE).matcher(query).matches()) {
			result = BooleanRetrieval.searchBooleanQuery(query.toUpperCase(), idxDico  );
		}else{
			// Case 3: ranked search BM25
			String tempQuery=query;
			query = Article.TokenizeTitle( query.replaceAll(" ","_AND_").replaceAll("_", " ") );
			// do a BooleanRetrieval.searchBooleanQuery with lot of OR
			result = BooleanRetrieval.searchBooleanQuery(query.toUpperCase(), idxDico  );
			if(result.getKey().size()<=topK){
				query =Article.TokenizeTitle( tempQuery.replaceAll(" ","_OR_").replaceAll("_", " ") );
				result = BooleanRetrieval.searchBooleanQuery(query.toUpperCase(), idxDico  );
			}
		}
		List<LightArticle> searchResult = result.getKey();
		Set<String> setUniqueTokens = result.getValue();
		//rank
		HashMap<String,List<Integer>> uniqueTokenPostingList= InvertedIndexer.getArticleIdsInPostingListForBM25(setUniqueTokens,idxDico);
		for (LightArticle a:searchResult) {
			BM25.compute(idxDico, uniqueTokenPostingList, a);
		}
		searchResult.sort(LightArticle.scoreComparatorDESC);

		int totNbArticles = searchResult.size();;
		List<Article> articles; // = Article.getHeavyArticlesFromID( ArticleIDs, idxDico);
		if (IsPhraseQuery){
			articles = Article.PhraseQuery(searchResult, exactQuery,topK,idxDico);
		}else{
			List<Integer> miniLista = new ArrayList<>(topK);
			int k=0;
			for(LightArticle a :searchResult){
				miniLista.add(a.articleID);
				if ( (++k) >= Math.min(searchResult.size(),topK) ) break;
			}
			articles = Article.getHeavyArticlesFromID( miniLista, idxDico);
		}
		if (articles.size()==0){
			articles = ForceFind(exactQuery,topK);
			totNbArticles = articles.size();
		}
		//print
		return Article.PrettyPrintSearchResult(query,articles,setUniqueTokens, topK, startTime1, totNbArticles);
	}

	private List<Article> ForceFind(String exactquery, int topK) {
		List<LightArticle> searchResult = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		int docID = 1;
		if (allArticles!=null) {
			for (String line : allArticles) {
				if (line.indexOf(exactquery) >= 0) {
					searchResult.add(new LightArticle(docID));
				}
				if (System.currentTimeMillis() > startTime + 1900) {
					if (!Constants.SilentOutput) {
						System.out.println("time out :(");
					}
					break;
				}
				docID++;
			}
		}else{
			try {
				File f = new File(Constants.OFFLINE_FILE);// create instance of file from Name of
				BufferedReader br = new BufferedReader(new FileReader(f));// create object of BufferedReader

				String line = br.readLine();// Read from current file
				while (line != null) {
					if (line.indexOf(exactquery) >= 0) {
						searchResult.add(new LightArticle(docID));
					}
					if (System.currentTimeMillis() > startTime + 1900) {
						if (!Constants.SilentOutput) {
							System.out.println("time out :(");
						}
						break;
					}
					docID++;

					line = br.readLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		searchResult.sort(LightArticle.scoreComparatorDESC);
		int k=0;
		List<Integer> miniLista = new ArrayList<>();
		for(LightArticle a :searchResult){
			miniLista.add(a.articleID);
			if ( (++k) >= Math.min(searchResult.size(),topK) ) break;
		}
		return Article.getHeavyArticlesFromID( miniLista, idxDico);
	}

	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int ndcgAt) {
		// FIXME Auto-generated method stub
		return null;
	}

	/**
	 * Implement a Java method using the provided template that crawls the newspaper articles for a given date. The method should return a csv file
	 */
	void crawl(){
		this.CrawlTheWeb();
	    /*
		Calendar start = Calendar.getInstance();
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

    }
	@Override
	int crawlNewspaper(String newspaper, Date day) {
		return crawlNewspaper(newspaper, day, day);
	}

	int crawlNewspaper(String newspaper, Date start_day, Date end_day) {
		if (newspaper.compareTo("The Guardian")!=0){//If both the strings are equal then this method returns 0
			System.out.println("this works only for The Guardian");
			return 0;
		}
		System.out.println("From "+ start_day + " to "+ end_day);
		
		String rootUrl = Constants.GUARDIAN_QUERY_WORLD;

		if (start_day == null) {//look for all articles
			//DONE start from the oldest so we can resume concat this opt to the url
			rootUrl +="&order-by=oldest";
		}else {//crawl for a specific day
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String start_date = sdf.format(start_day);
			String end_date = sdf.format(end_day);
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
				Article.saveStaticVar("%d",Article.lastNonUsedArticleID,"lastNonUsedArticleID"  );


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
		for(int year=2000;year>=2000;year--) {
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

		/*
		article.stemNTokenize();
		tokens.addAll(article.getUniqueTokens());
		log(""+tokens.size(), "tokenSize");
		log(article.toString(), "LightDB.csv");
		*/
		
		
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
	static void MergeAllFiles(String directory, String outputFile){//Constants.ROOT_DIR+"offline.csv"
		System.out.println("MergeAllFiles");
		Article.lastNonUsedArticleID = 1;
		Article.totArticlesLength = 0;
		Article.nbProcessedArticles = 0;
		try {
			File dir = new File(directory);// create instance of directory
			PrintWriter pw = new PrintWriter(outputFile);// create obejct of PrintWriter for output file
			String[] fileNames = dir.list();// Get list of all the files in form of String Array
			// loop for reading the contents of all the files
			for (String fileName : fileNames) {
				System.out.println("Reading from " + fileName);

				// create instance of file from Name of
				// the file stored in string Array
				File f = new File(dir, fileName);

				// create object of BufferedReader
				BufferedReader br = new BufferedReader(new FileReader(f));

				//pw.println("Contents of file " + fileName);

				// Read from current file
				String line = br.readLine();
				while (line != null) {
					// write to the output file
					line = line.replace("\\|", "")
							.replace("\\&", ""); //yep we need this :/
					String[] part = line.split(Constants.CSV_REGEX_SEPARATOR);
					if (part.length != 7) {
						throw new RuntimeException(line + "\n" + "have more | then expected " + part.length);
					}
					if (part[0].compareTo("\"article_id\"") != 0) {
						Article a = new Article(null, "", part[1], Arrays.asList(part[2].split(Constants.LIST_SEPARATOR)), part[3],
								part[4], part[5], Arrays.asList(part[6].split(Constants.LIST_SEPARATOR)));
						String debugStr = a.toString();
						pw.println(a.toString());
					}
					line = br.readLine();
				}
				pw.flush();
			}
			System.out.println("Reading from all files" +
					" in directory " + dir.getName() + " Completed");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public static List<String> allArticles = null;
	public static List<Integer> allArticlesTokenNb = null;
	public void LoadAllArticles(){
		//System.out.println("LoadAllArticles");
		boolean loadAllArticles =false;
		if (loadAllArticles) allArticles = new ArrayList<>(168000);
		allArticlesTokenNb = new ArrayList<>(168000);
		if (loadAllArticles) allArticles.add("no article have idx = 0");
		try {
			File f = new File(Constants.OFFLINE_FILE);// create instance of file from Name of
			BufferedReader br = new BufferedReader(new FileReader(f));// create object of BufferedReader

			String line = br.readLine();// Read from current file
			while (line != null) {
				if (loadAllArticles) allArticles.add(line);
				String onlySpaceLine = line.replaceAll("[^ ]", "");
				try{
					allArticlesTokenNb.add(onlySpaceLine.length());}
				catch (Exception e) {
					System.out.println(line);
					System.out.println(onlySpaceLine);
					e.printStackTrace();
				}
				line = br.readLine();

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
    /**
     * transform raw articles to LightDB.csv
	 * @return
	 */
	static HashMap<String, Map<Integer, Integer>> workOffline() {
		String offlineIn1File = "offline.csv";//Constants.ROOT_DIR+
		String LightIn1File = "LightDB.csv";
		//SearchEngineTheCrawlers.MergeAllFiles(Constants.ROOT_DIR+"full\\", offlineIn1File);
		return workOffline(offlineIn1File, LightIn1File);

	}
	static HashMap<String, Map<Integer, Integer>> workOffline(String fullDBfile, String lightDBfile) {
		System.out.println("workOffline:\n"+fullDBfile+"\n"+lightDBfile);
		FileWriter fTokenSize;
		FileWriter fNonUniqueTokenSize;
		FileWriter fLightDB;
		BufferedWriter outTokenSize;
		BufferedWriter outLightDB;
		BufferedWriter outNonUniqueTokenSize;
		try {
			fLightDB = new FileWriter(lightDBfile, Constants.OVERWITE_FILE);//"LightDB.csv"
			//fTokenSize = new FileWriter("tokenSize", Constants.OVERWITE_FILE);
			//fNonUniqueTokenSize = new FileWriter("NonUniqueTokenSize.csv", Constants.OVERWITE_FILE);
			//outTokenSize = new BufferedWriter(fTokenSize);
			outLightDB = new BufferedWriter(fLightDB);
			//outNonUniqueTokenSize = new BufferedWriter(fNonUniqueTokenSize);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		BufferedReader br = null;
		String line = "";
		//for(int year = 1999 ; year<=1999;year++) {
		long totTokens = 0L;
		long totAfter = 0L;
		long totBefore = 0L;

		Map<Integer, Pair<Integer, Integer>> articleIdToHeavyArticlePos = new HashMap<>(); // for idxDico
		int articleStartPos = 0;

		Map<String, Integer> tokensOccurrence = new TreeMap<>();

		HashMap<String, Map<Integer, Integer>> preIndex;
		try {
				FileReader fr = new FileReader(fullDBfile);//"offline.csv"
				br = new BufferedReader(fr);
				//String Encoding = fr.getEncoding();
				//br.readLine();//skip line 1 // faiz: why skip first line?? In offline.csv, there is content in first line // kim , you are right, it was headerline, now it is deleted
				int i=0;
				//Pair<String, int> pair = brReadLine(br);
				while ((line = br.readLine()) != null) {
					if ((i++) %1000==1) {System.out.print(".");}


					String[] part = line.split(Constants.CSV_REGEX_SEPARATOR);
					if ("article id"==part[0]) {continue;}
					int nb = 0;
					try {
						nb = Integer.valueOf(part[0]);
					}catch (Exception e) {
						System.out.println(part[0]);
						System.out.println(e);
					}

					int lineLen = line.getBytes().length;
					articleIdToHeavyArticlePos.put( nb , new Pair<>(articleStartPos ,lineLen));
					articleStartPos += lineLen+ 1; // \n char




					//Article article = new Article( new Integer( nb ), part[1], part[2], Arrays.asList(part[3].split(Constants.LIST_SEPARATOR)),part[4],
					//		part[5],part[6],Arrays.asList(part[7].split(Constants.LIST_SEPARATOR)));
					if (part.length!=7){
						//System.out.println("diff "+part.length+"\n"+line);
						part = (line+" "+Constants.CSV_SEPARATOR).split(Constants.CSV_REGEX_SEPARATOR);
						/*for(int j=0;j < part.length;j++ ){
							System.out.println(part[j]);
							System.out.println("+");
						}*/
					}
					Article article = new Article( new Integer( nb ), "", part[1], Arrays.asList(part[2].split(Constants.LIST_SEPARATOR)),part[3],
							part[4],part[5],Arrays.asList(part[6].split(Constants.LIST_SEPARATOR)));
					totBefore +=  article.text.length() +article.headline.length();
					article.stemNTokenize();
					totAfter +=  article.text.length() +article.headline.length();

					for(String token: (article.headline+" "+article.text).split(" ") ){

						if (!tokensOccurrence.containsKey(token)) {
							tokensOccurrence.put(token, 1);
						} else {
							tokensOccurrence.put(token, tokensOccurrence.get(token) + 1);
						}

					}

					tokens.addAll(article.getUniqueTokens());
					//log(""+tokens.size(), outTokenSize);
					totTokens += article.getNonUniqueTokens().length;
					//log(""+totTokens, outNonUniqueTokenSize);
					log(article.toString(), outLightDB);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				int[] distrib = new int[101];
				for(int i=0;i<101;i++) {distrib[i]=0;}
				Iterator it = tokensOccurrence.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry)it.next();
					distrib[ Math.min(100, (int) pair.getValue() ) ] ++;
				}
				System.out.println("distribution");
			for(int i=0;i<101;i++) {
				System.out.print(", "+distrib[i]);}
				int sum = 0;

				int i=100;
				for(;i>0&& sum+distrib[i]<=100000 ;i--){
					sum+=distrib[i];
				}
				System.out.println("\nacceptable occ: " + (i-1) );
				System.out.println("sum "+sum);


				preIndex = new HashMap<>(sum+10,1);
				it = tokensOccurrence.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry)it.next();
					if ( ((int) pair.getValue()) > i ){
						preIndex.put((String)pair.getKey(), new HashMap<>());
					}
				}
				tokensOccurrence=null;





				IdxDico idxDico = new IdxDico();
				idxDico.articleId_To_HeavyArticlePos = articleIdToHeavyArticlePos;
				idxDico.writeThisToFile();

				//System.out.println(totBefore);
				//System.out.println(totAfter);
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		try {
			//outNonUniqueTokenSize.close();
			//fNonUniqueTokenSize.close();
			//outTokenSize.close();
			//fTokenSize.close();
			outLightDB.close();
			fLightDB.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(tokens.toString(), "tokens.txt"); // 330 000 for all our articles
		return preIndex;
	}

	private static Pair<String,Integer> brReadLine(BufferedReader br) {
		//while(br.read()) this return int, maybe from it you can get the real size of the char maybe...
		return null;
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
