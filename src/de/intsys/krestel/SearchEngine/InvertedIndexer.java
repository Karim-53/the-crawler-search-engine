package de.intsys.krestel.SearchEngine;

import de.intsys.krestel.SearchEngine.hdt.Mutable;
import de.intsys.krestel.SearchEngine.hdt.VByte;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static jdk.nashorn.internal.ir.debug.ObjectSizeCalculator.getObjectSize;

//import com.google.guava;
public class InvertedIndexer {
	static ArrayList<Integer> articleIDList = new ArrayList<Integer>();
	

	private InvertedIndexer() {}

	/**
	 * Output 3 compressed files
	 * outputFileName.Postings
	 * outputFileName.dico.key
	 * outputFileName.dico.val
	 * @param inputFileName
	 * @param outputFileName
	 */
	static void buildCompressedIndex(String inputFileName, String outputFileName) {
		Pair< HashMap<String, Map<Integer, HashSet<Integer>>> ,
				Map<Integer, Pair<Integer, Integer>> > pair = buildIndex(inputFileName);

		IdxDico oidxDico = IdxDico.LoadIdxDicoFromfile();// this should contain already articleId_To_HeavyArticlePos //= new IdxDico();
		HashMap<String, Map<Integer, HashSet<Integer>>> indexInRAM = pair.getKey();
		oidxDico.articleId_To_LightArticlePos = pair.getValue();

		long startTime = System.currentTimeMillis();
		System.out.println("mistreat:");
		System.out.println(  indexInRAM.get("mistreat")   );
		System.out.println("illumin:");
		System.out.println(  indexInRAM.get("illumin")   );

		Map<String, Pair<Integer, Integer>> idxDico = compressIndex(indexInRAM, outputFileName);
		//System.out.println("idxDico from ram:");
		//System.out.println(idxDico);
		System.out.println("elapsedTime:: compressIndex : "+ (System.currentTimeMillis() - startTime) );

		oidxDico.tokenToPostingPos = idxDico;
		oidxDico.writeThisToFile();
		System.out.println( "idxDico size: " + getObjectSize(idxDico) );
		//writestringOf01ToFile( indexInRAM, outputFileName);
	}


	static Map<String, Pair<Integer, Integer>> compressIndex(HashMap<String, Map<Integer, HashSet<Integer>>> index, String outputIndex_FileName) {

		Map<String, Pair<Integer, Integer>> tokenToPostingListPos_Dico = new HashMap<>();
		//List<String> tokenToPostingListPos_Dico_keyList = new ArrayList<String>();
		//List<Long> tokenToPostingListPos_Dico_valList = new ArrayList<Long>();
		StringBuilder tokenToPostingListPos_Dico_keyList = new StringBuilder();

		int nextPostingList_StartPositionInFile = 0;
		try (OutputStream outIndex = new BufferedOutputStream(new FileOutputStream(outputIndex_FileName+".Postings", false), 1024)) {
			try (OutputStream outDicoVal = new BufferedOutputStream(new FileOutputStream(outputIndex_FileName+".dico.val", false), 1024)) {

			// Loop on all posting lists
		Iterator< Map.Entry<String, Map<Integer, HashSet<Integer>> > > PostingEntries = index.entrySet().iterator();
		int lastDocID, lastPos;
		while (PostingEntries.hasNext()) {
			if (Math.random() < 0.0005) { System.out.print("."); }
			Map.Entry<String, Map<Integer, HashSet<Integer>>> PostingEntry = PostingEntries.next();
			String token = PostingEntry.getKey();
			if (token.length()<1) continue;
			Map<Integer, HashSet<Integer>> onePostingList = PostingEntry.getValue();


			//String token = "pretenc";
			//Map<Integer, HashSet<Integer>> onePostingList = index.get(token);
			//System.out.println(onePostingList);
			SortedMap<Integer, HashSet<Integer>> onePostringListWithSortedDocID = getTreeMap(onePostingList);
			//System.out.println(onePostringListWithSortedDocID);

			List<Integer> onePostringList_DeltaDocID_count_DeltaPos = new ArrayList<>();
			lastDocID = 0; //need to be re-init for each posting list
			Iterator<Map.Entry<Integer, HashSet<Integer>>> entries = onePostringListWithSortedDocID.entrySet().iterator();
			while (entries.hasNext()) {
				// DocID: delta
				Map.Entry<Integer, HashSet<Integer>> entry = entries.next();
				int curentDocID = entry.getKey();
				assert lastDocID <= curentDocID : "DocID order foobar: from " + lastDocID + " to " + curentDocID;
				onePostringList_DeltaDocID_count_DeltaPos.add(curentDocID - lastDocID);
				lastDocID = curentDocID;
				//count
				HashSet<Integer> positionsNotSorted = entry.getValue();
				List<Integer> list = new ArrayList<>(positionsNotSorted);
				Collections.sort(list);
				onePostringList_DeltaDocID_count_DeltaPos.add(list.size());
				//positions
				lastPos = 0;
				for (int currentPos : list) {
					onePostringList_DeltaDocID_count_DeltaPos.add(currentPos - lastPos);
					lastPos = currentPos;
				}
			}
			//System.out.println(onePostringList_DeltaDocID_count_DeltaPos);

			// Applying V Byte encoding
			// V Byte is not suitable to encode 1 and we have a lot of 1 (for now)
			// after limiting the nb of tokens and eliminating rare words in a doc we will see XD
			int onePostingList_StartPositionInFile = nextPostingList_StartPositionInFile;
			//System.out.println(token);
			tokenToPostingListPos_Dico_keyList.append(token);
			if (PostingEntries.hasNext()) {tokenToPostingListPos_Dico_keyList.append(Constants.CSV_SEPARATOR);}
			//tokenToPostingListPos_Dico_valList.add(onePostingList_StartPositionInFile);
			VByte.encode(outDicoVal, onePostingList_StartPositionInFile);

			if (token.compareTo("mistreat")==0){
				System.out.println("mistreat DeltaPos:");
				System.out.println(onePostringList_DeltaDocID_count_DeltaPos);
			}
			if (token.compareTo("illumin")==0){
				System.out.println("illumin DeltaPos:");
				System.out.println(onePostringList_DeltaDocID_count_DeltaPos);
			}


			int bytesizeOfOnePostingList = 0;
			for (int e : onePostringList_DeltaDocID_count_DeltaPos) {
				bytesizeOfOnePostingList += VByte.encode(outIndex, e);
			}
			tokenToPostingListPos_Dico.put(token, new Pair<>(onePostingList_StartPositionInFile, bytesizeOfOnePostingList) );

			int onePostingList_EndPositionInFile = onePostingList_StartPositionInFile + bytesizeOfOnePostingList;
			nextPostingList_StartPositionInFile = onePostingList_EndPositionInFile;
		}

			System.out.println("stat::max val nextPostingList_StartPositionInFile "+nextPostingList_StartPositionInFile);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//TODO tokenToPostingListPos_Dico  write to file this can be deleted to improve the compresion time
		String outputIndexDicoNonCompressed_FileName= outputIndex_FileName+".dico.NonCompressed_key";
		long startTime = System.currentTimeMillis();
		try (OutputStream outDicoKey = new BufferedOutputStream(new FileOutputStream(outputIndexDicoNonCompressed_FileName, false), 1024)) {
			outDicoKey.write( tokenToPostingListPos_Dico_keyList.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("elapsedTime:: writing down dico keys non compressed : "+ (System.currentTimeMillis() - startTime) );


		//you can use kimHuffman here
		//kim huffman  (outputIndexDicoNonCompressed_FileName, outputIndex_FileName+".dico.key", 0);

		System.out.println(outputIndex_FileName+".dico.key");


		//FIXME check that none of the docID is 0
		//FIXME later check that docID are as small as possible

		return tokenToPostingListPos_Dico;
	}

	// Generic function to construct a new TreeMap (Sorted) from HashMap
	public static SortedMap<Integer, HashSet<Integer>> getTreeMap(Map<Integer, HashSet<Integer>> hashMap)
	{
		SortedMap<Integer, HashSet<Integer>> treeMap = new TreeMap<>();
		treeMap.putAll(hashMap);

		return treeMap;
	}



	static void buildIndex(String inputFileName, String outputFileName) {
		Pair< HashMap<String, Map<Integer, HashSet<Integer>>> ,
				Map<Integer, Pair<Integer, Integer>> > pair = buildIndex(inputFileName);

		HashMap<String, Map<Integer, HashSet<Integer>>> indexInRAM = pair.getKey();

		long startTime = System.currentTimeMillis();
		writeIndexToFile( indexInRAM, outputFileName);
		System.out.println("elapsedTime:: write NonCompressed Index : "+ (System.currentTimeMillis() - startTime) );

	}

	/**
	 * this build the index and return it (so we can compress it
	 * @param inputFileName
	 * @return index the index in the RAM
	 */
	static Pair< HashMap<String, Map<Integer, HashSet<Integer>>> ,
			     Map<Integer, Pair<Integer, Integer>> >
	buildIndex(String inputFileName){
		 // for Index
		 Map<Integer, HashSet<Integer>> subIndex;//[document id,[pos1,pos2,pos3]]
		 Map<Integer, HashSet<Integer>> subIndex2;
		 HashMap<String, Map<Integer, HashSet<Integer>>> index= new HashMap<>();//[word, subIndex]

		 Map<Integer, Pair<Integer, Integer>> articleIdToPostingPos = new HashMap<>();
		 int articleStartPos = 0;
		 try(BufferedReader file = new BufferedReader(new FileReader(inputFileName)))
		 {
             String line;
             while( (line = file.readLine()) !=null) {
            	 String[] part = line.split(Constants.CSV_SEPARATOR);

				 articleIdToPostingPos.put(Integer.parseInt(part[0]) , new Pair<>(articleStartPos ,line.length() ));
				 articleStartPos += line.length()+1; // \n char


            	 String text=part[5]+" "+part[4]; // i need the title first to have a better position later

            	 String[] textToIndex = text.trim().split(" ");
				 Article.totArticlesLength +=  textToIndex.length;
				 Article.nbProcessedArticles ++ ;
				 int currentDocID = Integer.valueOf(part[0]);
				 articleIDList.add(currentDocID);
				 int wordPositionInTextToIndex=0;//position is 0-based but for no reason XD
				 for(String word:textToIndex){
					 //subIndex2 = new HashMap<>();

					 if (!index.containsKey(word)) {
						//create index for word $word
						subIndex = new HashMap<>();
						subIndex.put(Integer.valueOf(part[0]), new HashSet<>());
						index.put(word, subIndex);
					}
                    else {
                    	if( !index.get(word).containsKey(currentDocID) ) {
                    		//create a doc for it
							subIndex2 = index.get(word);
							subIndex2.put(currentDocID, new HashSet<>());
							index.put(word,subIndex2);
                    	}
                    }
                    if(index.get(word).containsKey(currentDocID)) {
                    	index.get(word).get(Integer.valueOf(part[0])).add(wordPositionInTextToIndex);
                    }

                    wordPositionInTextToIndex++;
                 }
                
             }
         } catch (IOException e){
             System.out.println("inputFileName "+inputFileName+" is maybe not found. but some err occured Skip it");
         }

        // for BM25
		Article.saveStaticVar("%d", Article.totArticlesLength, "totArticlesLength" );
		Article.saveStaticVar("%d", Article.nbProcessedArticles, "nbProcessedArticles" );
		return new Pair<>(index, articleIdToPostingPos);
		 }

	/**
	 * this do not compress the index while writing
 	 * @param index
	 * @param outputFileName
	 */
	/**change all to Treemap so that it gets sorted**/
	static void writeIndexToFile( HashMap<String, Map<Integer, HashSet<Integer>>> index, String outputFileName){
			 Map<Integer, HashSet<Integer>> subIndex2;

			 try {
				 FileWriter fIndex	 = new FileWriter(outputFileName);
				 BufferedWriter OutIndex = new BufferedWriter(fIndex);

				 for(String key:index.keySet()) {
					 TreeMap<Integer, HashSet<Integer>> sorted = new TreeMap<>(index.get(key));
					 OutIndex.write(key +":"+ sorted);
					 OutIndex.newLine();

				 }



				 fIndex.flush();
				 OutIndex.flush();
				 fIndex.close();
				 OutIndex.close();
			 } catch (IOException e) {
				 // Auto-generated catch block
				 e.printStackTrace();
			 }
		 }

	 static Map<String, Long> buildDict(String fileName){
		 Article.totArticlesLength  = Integer.valueOf(Article.readFileAsString("totArticlesLength"));// for BM25
		 Article.nbProcessedArticles  = Integer.valueOf(Article.readFileAsString("nbProcessedArticles"));// for BM25

		 Map<String, Long> dictionary = new HashMap<String, Long>();
		 try {
			RandomAccessFile dicti = new RandomAccessFile(fileName,"r");
			//ArrayList<Long> arrayList = new ArrayList<Long>();
			
			String curLine = "";
			
		    try {
		    	Long tempFilePointer= dicti.getFilePointer();
		    	while((curLine=dicti.readLine())!=null){
		    	String[] text1 = curLine.split(":");
		    	dictionary.put(text1[0],tempFilePointer);
		    	//arrayList.add(tempFilePointer);
		    	tempFilePointer = dicti.getFilePointer();
		    	} 
				
			dicti.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return dictionary;
		 
		 
	 }


	static public Map<Integer, List<Integer> >  getPostingList(String token, IdxDico idxDico){
		System.out.println("getPostingList " + token);
		//TODO Add Caching cause we call get multiple time on the same tokens
		Map<String, Pair<Integer, Integer>> dictionary = idxDico.tokenToPostingPos;

		Pair<Integer, Integer> pair = dictionary.get(token);
		return  loadOnePostingListFromCompressedIndex(pair.getKey(), pair.getValue());
	}
	static Map<Integer, List<Integer>> loadOnePostingListFromCompressedIndex(int start, int length) {
		//System.out.println("start pos: " + start);
		byte[] b=new byte[length] ;

		try {//TODO OPEN File ONCE **********
			RandomAccessFile dicti = new RandomAccessFile(Constants.ROOT_DIR+"compressedIndex.Postings","r");
			dicti.seek(start);
			dicti.read(b,0, length);
			dicti.close();
		} catch (IOException e) {
			System.out.println("error in loadFromCompressedIndex");
			e.printStackTrace();
		}
		List<Integer> decodedInt = new ArrayList();
		Mutable<Integer> aInt = new Mutable<>(0);
		for( int offset = 0; offset<length ; ){
			offset += VByte.decode(b,  offset, aInt);
			decodedInt.add(aInt.getValue());
		}

		Map<Integer, List<Integer>> postingList = new HashMap<>();

		Iterator<Integer> decodedIntIterator = decodedInt.iterator();
		int lastArticleID = 0;
		while(decodedIntIterator.hasNext()){
			int ArticleID = lastArticleID + decodedIntIterator.next();
			int occurenceInArticle = decodedIntIterator.next();
			List<Integer> positions = new ArrayList<>();
			int LastPos = 0;
			for(int i=0;i<occurenceInArticle;i++){
				int curPos = LastPos + decodedIntIterator.next();
				positions.add( curPos   ); //TODO Delta encoding
				LastPos = curPos;
			}
			postingList.put(ArticleID, positions);
			lastArticleID = ArticleID;
		}

		return postingList;
	}

	/**
	 * for NoncompressedIndex.txt only (!) plz don t use it
	 * @param query
	 * @param dictionary
	 * @return
	 */
	 static String searchQuery(String query, Map<String, Long> dictionary) {
		 String[] text1 = null;
		 
		 try {
			 System.out.println(query);
				RandomAccessFile dicti = new RandomAccessFile("NoncompressedIndex.txt","r");
				if(dictionary.get(query)!=null) {

				//System.out.println(dictionary.get(query));
				dicti.seek(dictionary.get(query));
				text1 = dicti.readLine().split(":");}
				else {
					text1=(" : No Match found").split(":");
				}
				
				
				dicti.close();
				
		 } catch (IOException e) {
				// TODO Auto-generated catch block
			 System.out.println("error in searchQuery");
				e.printStackTrace();
			}
		 
		 return text1[1];
	 }


	static public List<Integer> getArticleIdsInPostingList(String aToken, IdxDico idxDico) {

		Map<Integer, List<Integer> > postingList = InvertedIndexer.getPostingList(aToken, idxDico);
		System.out.println("getArticleIdsInPostingList  "+aToken);
		//System.out.println(postingList.keySet());
		List<Integer> lista = new ArrayList<>( postingList.keySet() );
		Collections.sort(lista);
		return lista;

/*		String text=searchQuery(query,dictionary);
		List<Integer> postingList = new ArrayList<>();
		if(!text.equals(" No Match found")){

		String[] tempPostingList =text.split("],"); //example govern:{38982=[50, 440], 38983=[133, 197, 86, 375, 347, 399], 38985=[176, 236]} splits at ], and in the below regex it replaces all values after =
		//Convert from text to list posting list
		for(String temp:tempPostingList) {
			postingList.add(Integer.valueOf(temp.trim().replaceAll("(=.+)|(\\{)", "")));
		}
		System.out.println(postingList);

		}
		return postingList;
*/
	}

         
         }
         
         
	 
		 
	 

