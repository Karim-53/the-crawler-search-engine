package de.intsys.krestel.SearchEngine;

import com.sun.istack.internal.Nullable;
import javafx.util.Pair;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Article {
	//static:            ------------------------------------------------------
	public static int lastNonUsedArticleID;
	public static long totArticlesLength;
	public static int nbProcessedArticles;
	private static final Pattern endOfSentence = Pattern.compile("\\.\\s+");



	static public long averageLengthPerArticle(){return (long) (totArticlesLength / nbProcessedArticles) ;}


	public static HashSet<String> hs = new HashSet<String>();//stopwords list
	public static HashSet<String> hs2 = new HashSet<String>();//stopwordswithoutoperators
	static PorterStemmer stem1 = new PorterStemmer();

	static
    {//https://ourcodeworld.com/articles/read/839/how-to-read-parse-from-and-write-to-ini-files-easily-in-java
		lastNonUsedArticleID = Integer.valueOf(readFileAsString("lastNonUsedArticleID"));
		totArticlesLength = Integer.valueOf(readFileAsString("totArticlesLength"));
		nbProcessedArticles = Integer.valueOf(readFileAsString("nbProcessedArticles"));

		int len= Constants.stopwords.length;//Article.StopWords(); you do not need this anymore it's loaded on start
		for(int i=0;i<len;i++)
		{
			hs.add(Constants.stopwords[i]);
		}
		int len2= Constants.stopwordsWithoutOperators.length;//Article.StopWords(); you do not need this anymore it's loaded on start
		for(int i=0;i<len2;i++)
		{
			hs2.add(Constants.stopwordsWithoutOperators[i]);
		}
    }

	// Article attribute ------------------------------------------------------
	public int nb;
	public String uid;
	public String url;
	public List<String> authors;
	public String text;
	public String headline;
	public String publication_timestamp;
	public List<String> categories;
	public double score = 0.0; //


	public Article() {
		this(null);
	}	
	public Article(@Nullable Integer article_nb) {
		authors = new ArrayList<String>();
		categories = new ArrayList<String>();
		if (article_nb==null) {
		nb = lastNonUsedArticleID;
		lastNonUsedArticleID++;
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
		score=0;
	}
	//Faster Version for final Pretty print not for indexing
	public Article(@Nullable Integer article_nb, String article_url, List<String> article_authors,String article_text,
				   String article_headline,String article_publication_timestamp) {
		this(article_nb);
		uid=null;
		url=article_url;
		authors=article_authors.stream()
				.map(s -> tokenizeMinimumChange(s))
				.collect(Collectors.toList());
		text=tokenizeMinimumChange(article_text);
		headline=tokenizeMinimumChange(article_headline);
		publication_timestamp=tokenizeMinimumChange(article_publication_timestamp);
		categories = null;
		score=0;
	}
	public static Article ArticleFromLine(String line) {
		String[] part = line.split(Constants.CSV_SEPARATOR);
		//		part[5], part[6], Arrays.asList(part[7].split(Constants.LIST_SEPARATOR)));
		return new Article(Integer.valueOf(part[0]), "uid", part[1], Arrays.asList(part[2].split(Constants.LIST_SEPARATOR)), part[3],
				part[4], part[5], Arrays.asList(part[6].split(Constants.LIST_SEPARATOR)));
	}
	public static Article FastArticleFromLine(String line) {
		String[] part = line.split(Constants.CSV_SEPARATOR);
		//		part[5], part[6], Arrays.asList(part[7].split(Constants.LIST_SEPARATOR)));
		return new Article(Integer.valueOf(part[0]), part[1], Arrays.asList(part[2].split(Constants.LIST_SEPARATOR)), part[3],
				part[4], part[5]);
	}
	public static List<Article> getLightArticlesFromID(List<Integer> articleIDs,  IdxDico idxDico) {
		return getArticlesFromID( articleIDs,  idxDico.articleId_To_LightArticlePos, "LightDB.csv");
	}
	public static List<Article> getHeavyArticlesFromID(List<Integer> articleIDs,  IdxDico idxDico) {
		return getHeavyArticlesFromIDOffline( articleIDs,  idxDico.offlineArticleID_position, "offline.csv");
	}

	public static List<Article> getHeavyArticlesFromIDOffline(List<Integer> articleIDs, Map<Integer,Long> offlineArticleID_position, String file){
        List<Article> lista = new ArrayList<>();
        try {
            RandomAccessFile dicti = new RandomAccessFile(file,"r");
			//Long max=0L;
			String line;

            for(int articleID:articleIDs) {
				long startTime = System.currentTimeMillis();
                Long start = offlineArticleID_position.get(articleID);
				dicti.seek(start);
				Long nextArticlePos=offlineArticleID_position.get(articleID+1);
				if (nextArticlePos!=null && ((int) (nextArticlePos-start))>0 && ((int) (nextArticlePos-start))<200000){
					byte[] bytes = new byte[(int) (nextArticlePos-start)];
					dicti.read(bytes);
					line =new String(bytes);}
				else {
					line = dicti.readLine();
				}
                // DONE improve Java's I/O performance : RandomAccessFile + readLine are very slow
                // https://www.javaworld.com/article/2077523/java-tip-26--how-to-improve-java-s-i-o-performance.html
                lista.add( FastArticleFromLine(line) );
				//if ((System.currentTimeMillis() - startTime) > 5){ System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );}


			}
            dicti.close();
        } catch (IOException e) {e.printStackTrace();}
        return lista;
    }
	public static List<Article> getHeavyArticlesFromIDOffline1(List<Integer> articleIDs, Map<Integer,Long> offlineArticleID_position, String file){// much slower than the above
		List<Article> lista = new ArrayList<>();
		try {//TODO OPEN File ONCE for all queries **********
			RandomAccessFile dicti = new RandomAccessFile(file,"r");
			FileInputStream file1 = new FileInputStream(file);
			//long startTime = System.currentTimeMillis();
			//Long max=0L;
			String line;

			for(int articleID:articleIDs) {
				Long start = offlineArticleID_position.get(articleID);

				//int len = pair.getValue();
				//System.out.println("seek start: "+start);
				Long nextArticlePos=offlineArticleID_position.get(articleID+1);
				if (nextArticlePos!=null && ((int) (nextArticlePos-start))>0 && ((int) (nextArticlePos-start))<200000){
					//System.out.println("step1"+offlineArticleID_position.get(articleID+1));
					//max=Math.max(max,nextArticlePos-start);
					//System.out.println(articleID + " " +max);
					System.out.println("hereee");
					byte[] bytes = new byte[(int) (nextArticlePos-start)];
					System.out.println((int) (long) (start) + " " + (int) (nextArticlePos-start) +" " +bytes);
					file1.getChannel().position(start);
					file1.read(bytes);
					//dicti.read(bytes);
					line =new String(bytes);}
				else {
					dicti.seek(start);
					//System.out.println("step2");
					line = dicti.readLine();
				}
				//TODO improve Java's I/O performance : RandomAccessFile + readLine are very slow *****************************
				//  https://www.javaworld.com/article/2077523/java-tip-26--how-to-improve-java-s-i-o-performance.html
				//System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );
				//System.out.println(line);
				lista.add( ArticleFromLine(line) );

			}
			//System.out.println(max);

			dicti.close();
			file1.close();
		} catch (IOException e) {e.printStackTrace();}
		return lista;

	}
	public static List<Article> getHeavyArticlesFromIDOffline2(List<Integer> articleIDs, Map<Integer,Long> offlineArticleID_position, String file){// much slower than the above
		List<Article> lista = new ArrayList<>();

		try {//TODO OPEN File ONCE for all queries **********
			long startTime = System.currentTimeMillis();
			Stream<String> stream = Files.lines(Paths.get(file));
			stream.forEach(line ->{
				String temp=checkid(line, articleIDs);
				if(temp!=null){
					lista.add(ArticleFromLine(temp));}
				System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );
			});

			//Long max=0L;
			//System.out.println(max);

		} catch (IOException e) {e.printStackTrace();}
		return lista;

	}
	public static List<Article> getHeavyArticlesFromIDOffline4(List<Integer> articleIDs, Map<Integer,Long> offlineArticleID_position, String file){// much slower than the above
		List<Article> lista = new ArrayList<>();

		try {//TODO OPEN File ONCE for all queries **********
			FileReader in = new FileReader(file);
			BufferedReader br = new BufferedReader(in);
			long startTime = System.currentTimeMillis();
			String line;
			while ((line = br.readLine()) != null) {
				String temp=checkid(line, articleIDs);
				if(temp!=null){
					lista.add(ArticleFromLine(temp));}
				System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );
			}
			in.close();

			//Long max=0L;
			//System.out.println(max);

		} catch (IOException e) {e.printStackTrace();}
		return lista;

	}
	public static String checkid(String line, List<Integer> articleIDs){
		String[] part = line.split(Constants.CSV_SEPARATOR);

		if (articleIDs.contains(Integer.valueOf(part[0]))){
			return line;
		}

		return null;
	}


		public static List<Article> getArticlesFromID(List<Integer> articleIDs, Map<Integer, Pair<Integer, Integer>> articleIdToFullArticlePos, String file) {
		List<Article> lista = new ArrayList<>();
		try {//TODO OPEN File ONCE for all queries **********
			RandomAccessFile dicti = new RandomAccessFile(file,"r");

			for(int articleID:articleIDs) {
				Pair<Integer, Integer> pair = articleIdToFullArticlePos.get(articleID);
				int start = pair.getKey();
				//int len = pair.getValue();
				System.out.println("seek start: "+start);
				dicti.seek(start+1);

				long startTime = System.currentTimeMillis();
				String line = dicti.readLine();
				//TODO improve Java's I/O performance : RandomAccessFile + readLine are very slow *****************************
				//  https://www.javaworld.com/article/2077523/java-tip-26--how-to-improve-java-s-i-o-performance.html
				System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );

				System.out.println(line);
				lista.add( ArticleFromLine(line) );

			}

		dicti.close();
		} catch (IOException e) {e.printStackTrace();}
		return lista;
	}
	public static List<Article> getHeavyArticlesFromID(List<Integer> articleIDs, Map<Integer, Pair<Integer, Integer>> articleIdToFullArticlePos) {
		List<Article> lista = new ArrayList<>();
		try {//TODO OPEN File ONCE for all queries **********
			RandomAccessFile dicti = new RandomAccessFile("offline.csv","r");

			for(int articleID:articleIDs) {
				Pair<Integer, Integer> pair = articleIdToFullArticlePos.get(articleID);
				int start = pair.getKey();
				//int len = pair.getValue();

				dicti.seek(start);

				long startTime = System.currentTimeMillis();
				String line = dicti.readLine();
				//TODO improve Java's I/O performance : RandomAccessFile + readLine are very slow *****************************
				//  https://www.javaworld.com/article/2077523/java-tip-26--how-to-improve-java-s-i-o-performance.html
				System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );

				lista.add( ArticleFromLine(line) );

			}

			dicti.close();
		} catch (IOException e) {e.printStackTrace();}
		return lista;
	}
	public static void PrettyPrintSearchResult(String query, List<Article> searchResult,Set<String> setUniqueTokens, Integer topK, long startTime1) {
		//System.out.println("Query: "+query);
        System.out.println("==========================================================================================================================");
        System.out.println("Search Results");
		System.out.println("About " +searchResult.size()+ " Results" +"("+(System.currentTimeMillis()-startTime1)+ " ms)");
        //System.out.println("==========================================================================================================================");
		/*String format = "%-8s%-12s%-70s%s%s\n";
		System.out.println("==========================================================================================================================");
		System.out.printf(format, "ID", "score", "title", "link","text");
		System.out.println("==========================================================================================================================");

		for (Article a:searchResult) {
			System.out.printf(format, a.nb, round(a.score,5), a.headline, a.url,Summary(a.text,setUniqueTokens));
		}
		System.out.println("============================================================================");*/
		int resultno=1;

		for (Article a:searchResult){
			System.out.println("\n"+ resultno );
			//System.out.println(round(a.score,5));
			DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
			ZonedDateTime zdt = ZonedDateTime.parse(a.publication_timestamp.toUpperCase().replaceAll("\"|\"",""), dtf);

			//Instant instant=Instant.parse(a.publication_timestamp.toUpperCase().replaceAll("\"|\"",""));for time in nano


			System.out.println("Title:\t\t"+a.headline +"\nAuthors(s):\t"+ String.join(", ", a.authors) +"  -  "+zdt.toLocalDate() +" " + zdt.toLocalTime());
            //System.out.println(a.headline.replaceAll("(\")|(\")", "").trim());
            System.out.println(a.url.replaceAll("(\")|(\")", "").trim());


            List<String> description =Summary(a.text,setUniqueTokens);


			Random rand = new Random();
			for (int i=0; 0<description.size() && i<5; i++){//select random items from the list to print.. print only 5
				int next = rand.nextInt(description.size());
				System.out.println(description.get(next));
				description.remove(next);
            }


            //System.out.println("==========================================================================================================================");
			topK--;
            if (topK==0){
            	break;
			}

            resultno++;


        }
		if(resultno==1){
			System.out.println("No results Found");
			System.out.println("==========================================================================================================================");
		}
	}

	public static List Summary(String text,Set<String> setUniqueTokens){// can be used for offline.csv
		List<String> sentences= new ArrayList<>();
		if(setUniqueTokens.contains("")){// if the query contains stop words, the set has "". so have to remove that.
			setUniqueTokens.remove("");
		}
		for (String sentence:endOfSentence.split(text)){
			String sentence1=TokenizeBody(sentence);
			for(String a:setUniqueTokens){
				if(PorterStem(TokenizeBody(sentence1)).contains(a)){
					String[] sentenceinArray =sentence1.split(" +");
					int sentenceLength=sentenceinArray.length;

					for(int i=0;i<sentenceLength;i++){
						if (PorterStem(sentenceinArray[i]).equals(a)){
							String wordsAround= "..." + (i-4>-1 ? sentenceinArray[i-4] +" " : "") +
									(i-3>-1 ? sentenceinArray[i-3] +" " : "") +
									(i-2>-1 ? sentenceinArray[i-2] +" " : "") +
									(i-1>-1 ? sentenceinArray[i-1] +" " : "") +
									sentenceinArray[i] + " "+
									(i+1<sentenceLength ? sentenceinArray[i+1] +" " : "") +
									(i+2<sentenceLength ? sentenceinArray[i+2] +" " : "") +
									(i+3<sentenceLength ? sentenceinArray[i+3] +" " : "") +
									(i+4<sentenceLength ? sentenceinArray[i+4] +" " : "") +
									"...";
							sentences.add(wordsAround);
							i+=Math.min(4,sentenceLength);
						}
					}
				break;
				}
			}


		}
		Collections.shuffle(sentences);
		return sentences;



	}
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static List<Article> PhraseQuery(List<Article> searchResult, String exactquery) {
		List<Article> lista = new ArrayList<>();
		for (Article a:searchResult) {
			if (a.text.indexOf(exactquery)>=0) {
				lista.add(a);
				//if (lista.size()>=10) {break;} // maybe we should put this back
			}
		}
		if (lista.size()==0){
			System.out.println("no exact exp found :(");
			return searchResult;
		}
		return lista;
		// todo change the tokens to hilight later
	}

	private String encodeComma(String article_url) {
		return article_url.replaceAll(",", "%2C");
	}
	public void stemNTokenize() {
		text = PorterStem(TokenizeBody(text));
		headline = PorterStem(TokenizeTitle(headline));
		authors=authors.stream()
				.map(s -> TokenizeTitle(s))
				.collect(Collectors.toList());

		categories = categories.stream()
				.map(s -> TokenizeTitle(s))
				.collect(Collectors.toList());


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
	public int numberOfNonUniqueTokens(){
		return getNonUniqueTokens().length;
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
				.replaceAll("\\bUS\\b","USA")//no use since the offline.csv is in lowercase; Is it fixed now ?
				.toLowerCase()
				.replaceAll("http.*?\\s", " ")
				.replaceAll("\\s", " ")
				.replaceAll("[\\v]", " ")	//vertical spaces (newline)
				.trim().replaceAll(" +", " ");
		
		txt = txt.replaceAll("(\\d)(,)(\\d)", "$1$3"); //separateur de millier
		txt = txt.replaceAll("(\\w)([,/])(\\w)", "$1 $3")
				.replaceAll("(\\.){2,}"," ");
		txt = txt.replaceAll(Constants.CSV_SEPARATOR, " "); //no comma at all: we need this for storing
		
		return txt;
	}
	public static String TokenizeBody(String txt) {
		txt = txt.replaceAll(" (\\d) ", "")
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
	
	public String toString() {
		StringBuilder s = new StringBuilder(); 
		s.append(nb);
		//s.append(", ");
		//s.append(uid);
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
			data = new String(Files.readAllBytes(Paths.get(Constants.ROOT_DIR+fileName)));
			return data;
		}catch (Exception e){
			System.out.println("file "+fileName+" not found");
			return "1";
		}
	}

	public static <T> void saveStaticVar(String pattern,T var, String FileName ) {
		try {
			FileWriter fileWriter = new FileWriter(Constants.ROOT_DIR+FileName, false); //overwrites file
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.printf(pattern,var);
			printWriter.close();
		} catch (Exception e) {
			System.err.format("IOException: %s%n", e);
		}

	}
	static int countOccurences(String str, String word) {
		// split the string by spaces in a
		String a[] = str.split(" ");

		// search for pattern in a
		int count = 0;
		for (int i = 0; i < a.length; i++)
		{
			// if match found increase count
			if (word.equals(a[i]))
				count++;
		}

		return count;
	}
	public int countOfToken(String aUniqueToken) {
		return countOccurences(this.headline + " " +this.text, aUniqueToken );
	}

	public static Comparator<Article> scoreComparatorDESC = new Comparator<Article>(){

		@Override
		public int compare(Article c1, Article c2) {
			return (int) ( -10000.0*(c1.score - c2.score) ) ;
		}
	};

}
//FIXME add a method that goes through the crawled articles and chek if there is any duplicates