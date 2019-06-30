package de.intsys.krestel.SearchEngine.search;

import de.intsys.krestel.SearchEngine.Article;
import de.intsys.krestel.SearchEngine.IdxDico;
import de.intsys.krestel.SearchEngine.InvertedIndexer;

import java.util.Set;

public class BM25 {
	static double k1 = 1.2f;
	static double b = 0.75f;
	static double k2 = 100;


	static public double compute(IdxDico idxDico, Set<String> setUniqueTokens, Article article) {
		double score = 0.0;
		// normalizes Term Freq component document length
		if(article.score != 0)
			return article.score;
		double dl = article.numberOfNonUniqueTokens();
		double avdl = Article.averageLengthPerArticle();
		double K = k1 * ((1 - b) + b * dl / avdl);		// me ici
		//Set<String> setUniqueTokens = new HashSet<>(queryTokens);
		for (String aUniqueToken : setUniqueTokens) {
			// count of token i in article
			int fi = article.countOfToken(aUniqueToken);
			// count of token i in query
			int qfi = countOfTermInQuery(aUniqueToken, setUniqueTokens);
			// k1 and k2 are set empirically
			// number of relevant documents containing token i
			int ri = 0;
			// number of relevant documents for query
			int R = 0;
			// number of documents containing term i
			long ni = InvertedIndexer.getArticleIdsInPostingList(aUniqueToken, idxDico).size();
			// total number of documents
			int N = Article.nbProcessedArticles;
			//I added one at inside the log to remove minus
			double idf = Math.log(((ri + 0.5) / (R - ri + 0.5)) / ((ni - ri + 0.5) / (N - ni - R + ri + 0.5)) + 1); 
			score += idf * ((k1 + 1) * fi) / (K + fi) * ((k2 + 1) * qfi) / (k2 + qfi);
		}
		article.score = score;
		return score;//*/
	}

	private static int countOfTermInQuery(String targetToken, Set<String> queryTokens) {
		int count = 0;
		for (String queryToken: queryTokens){
			if (queryToken.compareTo(targetToken) == 0)
				count++;
		}
		return count;
	}		
}
