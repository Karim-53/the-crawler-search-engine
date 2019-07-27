package de.intsys.krestel.SearchEngine;

import java.util.*;

public class LightArticle {
    public int articleID ;
    private int preScore;
    public double score;



    public static Comparator<LightArticle> preScoreComparatorDESC = new Comparator<LightArticle>(){
        @Override
        public int compare(LightArticle c1, LightArticle c2) {
            return (int) ( -1*(c1.preScore - c2.preScore) ) ;
        }
    };
    public static Comparator<LightArticle> scoreComparatorDESC = new Comparator<LightArticle>(){
        @Override
        public int compare(LightArticle c1, LightArticle c2) {
            if (c1.score < c2.score) return  1;
            if (c1.score > c2.score) return -1;
            return 0;
        }
    };
    public LightArticle(int articleID) {
        this.articleID = articleID;
        preScore = 0;
        score = 0.0;
    }
    public static List<Integer> eliminateSomeArticles(List<Integer> articleIDs, Set<String> setUniqueTokens, IdxDico idxDico){
        int newSize = 5000;
        if (articleIDs.size()< newSize*1.5 ) return articleIDs;
        //System.out.println("LightArticle.eliminateSomeArticles : before : "+ ArticleIDs.size());
        List<LightArticle> lightArticles = articleIDsToLightArticlesList(articleIDs);
        for (String aToken:setUniqueTokens){
            Map<Integer, Integer> postingList = InvertedIndexer.getPostingList(aToken, idxDico);
            for(LightArticle lightArticle: lightArticles){
                if ( postingList.containsKey(lightArticle.articleID) ){
                    lightArticle.preScore += postingList.get( lightArticle.articleID );
                }
            }
        }

        lightArticles.sort(LightArticle.preScoreComparatorDESC);
        articleIDs = new ArrayList<>(newSize);
        int added = 0;
        for( LightArticle lightArticle: lightArticles){
            if ( added >= newSize ) break;
            if ( added > newSize/4 && lightArticle.preScore<=1 ) break;
            //System.out.println(lightArticle.preScore);
            articleIDs.add( lightArticle.articleID );
            added++;
        }
        //System.out.println("after : "+ ArticleIDs.size());
        Collections.sort(articleIDs);
        return articleIDs;
    }

    public static List<LightArticle> articleIDsToLightArticlesList(List<Integer> articleIDs) {
        List<LightArticle> lightArticles= new ArrayList<>(articleIDs.size());
        for( int id: articleIDs){
            lightArticles.add( new LightArticle(id) );
        }
        return lightArticles;
    }
}
