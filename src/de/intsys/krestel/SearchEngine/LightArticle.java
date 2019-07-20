package de.intsys.krestel.SearchEngine;

import java.util.*;

public class LightArticle {
    int articleID ;
    int preScore;
    public static Comparator<LightArticle> preScoreComparatorDESC = new Comparator<LightArticle>(){

        @Override
        public int compare(LightArticle c1, LightArticle c2) {
            return (int) ( -1*(c1.preScore - c2.preScore) ) ;
        }
    };
    public LightArticle(int articleID) {
        this.articleID = articleID;
        preScore = 0;
    }
    public static List<Integer> eliminateSomeArticles(List<Integer> ArticleIDs, Set<String> setUniqueTokens, IdxDico idxDico){
        int newSize = 5000;
        if (ArticleIDs.size()< newSize*1.5 ) return ArticleIDs;
        System.out.println("LightArticle.eliminateSomeArticles : before : "+ ArticleIDs.size());
        List<LightArticle> lightArticles = new ArrayList<>(ArticleIDs.size());
        for( int id: ArticleIDs){
            lightArticles.add( new LightArticle(id) );
        }
        for (String aToken:setUniqueTokens){
            Map<Integer, Integer> postingList = InvertedIndexer.getPostingList(aToken, idxDico);
            for(LightArticle lightArticle: lightArticles){
                if ( postingList.containsKey(lightArticle.articleID) ){
                    lightArticle.preScore += postingList.get( lightArticle.articleID );
                }
            }
        }

        lightArticles.sort(LightArticle.preScoreComparatorDESC);
        ArticleIDs = new ArrayList<>(newSize);
        int added = 0;
        for( LightArticle lightArticle: lightArticles){
            if ( added > newSize ) break;
            if ( added > newSize/4 && lightArticle.preScore<=1 ) break;
            //System.out.println(lightArticle.preScore);
            ArticleIDs.add( lightArticle.articleID );
            added++;
        }
        System.out.println("after : "+ ArticleIDs.size());
        Collections.sort(ArticleIDs);
        return ArticleIDs;
    }
}
