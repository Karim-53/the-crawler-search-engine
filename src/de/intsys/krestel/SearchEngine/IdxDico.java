package de.intsys.krestel.SearchEngine;

import javafx.util.Pair;

import java.io.*;
import java.util.Map;

public class IdxDico implements Serializable {
    Map<String, Pair<Integer, Integer>> tokenToPostingPos ;

    Map<Integer, Pair<Integer, Integer>> articleId_To_LightArticlePos;
    Map<Integer, Pair<Integer, Integer>> articleId_To_HeavyArticlePos;
    Map<Integer,Long> offlineArticleID_position;
    Map<Integer,Long> offlineArticleID_position1;


    public static IdxDico LoadIdxDicoFromfile(){
        try{
            FileInputStream fileInputStream
                    = new FileInputStream(Constants.ROOT_DIR+"IdxDico");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            IdxDico idxDico = (IdxDico) objectInputStream.readObject();
            objectInputStream.close();


            return idxDico;

        } catch (Exception e) {e.printStackTrace();}
        return null;
    }
    public void writeThisToFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.ROOT_DIR+"IdxDico",false);
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
