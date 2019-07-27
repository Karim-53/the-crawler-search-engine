package de.intsys.krestel.SearchEngine;

import de.intsys.krestel.SearchEngine.hdt.Mutable;
import de.intsys.krestel.SearchEngine.hdt.VByte;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static jdk.nashorn.internal.ir.debug.ObjectSizeCalculator.getObjectSize;

public class InvertedIndexer {
	static ArrayList<Integer> articleIDList = new ArrayList<Integer>();


	private InvertedIndexer() {
	}

	/**
	 * Output 3 compressed files
	 * outputFileName.Postings
	 * outputFileName.dico.key
	 * outputFileName.dico.val
	 *
	 * @param inputFileName
	 * @param outputFileName
	 */
	static void buildCompressedIndex(HashMap<String, Map<Integer, Integer>> Index, String inputFileName, String outputFileName) {
		System.out.println("buildCompressedIndex");
		Pair<HashMap<String, Map<Integer, Integer>>,
				Map<Integer, Pair<Integer, Integer>>> pair = buildIndex(Index, inputFileName);// the first hashmap contains index and the map contains articleIdToPostingPos (articleid=startpos=lengthofarticle)

		IdxDico oidxDico = IdxDico.LoadIdxDicoFromfile();// this should contain already articleId_To_HeavyArticlePos //= new IdxDico();
		HashMap<String, Map<Integer, Integer>> indexInRAM = pair.getKey();
		oidxDico.articleId_To_LightArticlePos = pair.getValue();//loading articleIdToPostingPos

		long startTime = System.currentTimeMillis();

		Map<String, Pair<Integer, Integer>> idxDico = compressIndex(indexInRAM, outputFileName);
		//System.out.println("elapsedTime:: compressIndex : " + (System.currentTimeMillis() - startTime));

		oidxDico.tokenToPostingPos = idxDico;
		oidxDico.writeThisToFile();
		System.out.println("idxDico size: " + getObjectSize(idxDico));
	}


	static Map<String, Pair<Integer, Integer>> compressIndex(HashMap<String, Map<Integer, Integer>> index, String outputIndex_FileName) {

		Map<String, Pair<Integer, Integer>> tokenToPostingListPos_Dico = new HashMap<>();
		StringBuilder tokenToPostingListPos_Dico_keyList = new StringBuilder();

		int nextPostingList_StartPositionInFile = 0;
		try (OutputStream outIndex = new BufferedOutputStream(new FileOutputStream(outputIndex_FileName + ".Postings", false), 1024)) {
			try (OutputStream outDicoVal = new BufferedOutputStream(new FileOutputStream(outputIndex_FileName + ".dico.val", false), 1024)) {

				// Loop on all posting lists
				Iterator<Map.Entry<String, Map<Integer, Integer>>> PostingEntries = index.entrySet().iterator();
				int lastDocID, lastPos;
				while (PostingEntries.hasNext()) {
					if (Math.random() < 0.0005) {
						System.out.print(";");
					}
					Map.Entry<String, Map<Integer, Integer>> PostingEntry = PostingEntries.next();
					String token = PostingEntry.getKey();
					if (token.length() < 1) continue;
					Map<Integer, Integer> onePostingList = PostingEntry.getValue();//***********

					SortedMap<Integer, Integer> onePostringListWithSortedDocID = getTreeMap(onePostingList);

					List<Integer> onePostringList_DeltaDocID_count = new ArrayList<>();
					lastDocID = 0; //need to be re-init for each posting list
					Iterator<Map.Entry<Integer, Integer>> entries = onePostringListWithSortedDocID.entrySet().iterator();
					while (entries.hasNext()) {
						// DocID: delta
						Map.Entry<Integer, Integer> entry = entries.next();
						int curentDocID = entry.getKey();
						//assert lastDocID <= curentDocID : "DocID order foobar: from " + lastDocID + " to " + curentDocID;
						onePostringList_DeltaDocID_count.add(curentDocID - lastDocID);
						lastDocID = curentDocID;
						//count
						Integer positionsCount = entry.getValue();

						onePostringList_DeltaDocID_count.add(positionsCount);
						//positions
						//not anymore
					}

					// Applying V Byte encoding
					int onePostingList_StartPositionInFile = nextPostingList_StartPositionInFile;

					tokenToPostingListPos_Dico_keyList.append(token);
					if (PostingEntries.hasNext()) {
						tokenToPostingListPos_Dico_keyList.append(Constants.CSV_SEPARATOR);
					}

					VByte.encode(outDicoVal, onePostingList_StartPositionInFile);

					/*if (token.compareTo("mistreat") == 0) {
						System.out.println("mistreat DeltaPos:");
						System.out.println(onePostringList_DeltaDocID_count);
					}
					if (token.compareTo("illumin") == 0) {
						System.out.println("illumin DeltaPos:");
						System.out.println(onePostringList_DeltaDocID_count);
					}*/


					int bytesizeOfOnePostingList = 0;
					for (int e : onePostringList_DeltaDocID_count) {
						bytesizeOfOnePostingList += VByte.encode(outIndex, e);
					}
					tokenToPostingListPos_Dico.put(token, new Pair<>(onePostingList_StartPositionInFile, bytesizeOfOnePostingList));

					int onePostingList_EndPositionInFile = onePostingList_StartPositionInFile + bytesizeOfOnePostingList;
					nextPostingList_StartPositionInFile = onePostingList_EndPositionInFile;
				}

				//System.out.println("stat::max val nextPostingList_StartPositionInFile " + nextPostingList_StartPositionInFile);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		// tokenToPostingListPos_Dico  write to file this can be deleted to improve the compresion time
		String outputIndexDicoNonCompressed_FileName = outputIndex_FileName + ".dico.NonCompressed_key";
		long startTime = System.currentTimeMillis();
		try (OutputStream outDicoKey = new BufferedOutputStream(new FileOutputStream(outputIndexDicoNonCompressed_FileName, false), 1024)) {
			outDicoKey.write(tokenToPostingListPos_Dico_keyList.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("elapsedTime:: writing down dico keys non compressed : " + (System.currentTimeMillis() - startTime));
		*/

		//you can use kimHuffman here
		//kim huffman  (outputIndexDicoNonCompressed_FileName, outputIndex_FileName+".dico.key", 0);

		//System.out.println(outputIndex_FileName + ".dico.key");

		//FIXME later check that docID are as small as possible

		return tokenToPostingListPos_Dico;
	}

	// Generic function to construct a new TreeMap (Sorted) from HashMap
	public static SortedMap<Integer, Integer> getTreeMap(Map<Integer, Integer> hashMap) {
		SortedMap<Integer, Integer> treeMap = new TreeMap<>();
		treeMap.putAll(hashMap);

		return treeMap;
	}


	static void buildIndex(String inputFileName, String outputFileName) {
		/*Pair<HashMap<String, Map<Integer, HashSet<Integer>>>,
				Map<Integer, Pair<Integer, Integer>>> pair = buildIndex(inputFileName);

		HashMap<String, Map<Integer, HashSet<Integer>>> indexInRAM = pair.getKey();

		long startTime = System.currentTimeMillis();
		writeIndexToFile(indexInRAM, outputFileName);
		System.out.println("elapsedTime:: write NonCompressed Index : " + (System.currentTimeMillis() - startTime));
*/
	}

	/**
	 * this build the index and return it (so we can compress it
	 *
	 * @param inputFileName
	 * @return index the index in the RAM
	 */
	static Pair<HashMap<String, Map<Integer, Integer>>,
			Map<Integer, Pair<Integer, Integer>>>
	buildIndex(HashMap<String, Map<Integer, Integer>> index,String inputFileName) {
		// for Index
		Map<Integer, Integer> subIndex;//[document id,[pos1,pos2,pos3]]
		Map<Integer, Integer> subIndex2;
		//HashMap<String, Map<Integer, HashSet<Integer>>> index = new HashMap<>(62000);//[word, subIndex]

		Map<Integer, Pair<Integer, Integer>> articleIdToPostingPos = new HashMap<>();
		int articleStartPos = 0;
		try (BufferedReader file = new BufferedReader(new FileReader(inputFileName))) {
			String line;
			int i=0;
			while ((line = file.readLine()) != null) {
				//System.out.println(i);
				if ((++i) %1000==0) {
					System.out.println(i);
					//System.out.println("     "+index.size());
				}
				/*if ((i) %40000==0) {
					System.out.println("in : "+index.size());
					// let's remove all books which are greater than 39.00 USD from map
					// get a set of entries
					Set<Entry<String, Map<Integer, HashSet<Integer>>>> setOfEntries = index.entrySet();
					// get the iterator from entry set
					Iterator<Entry<String, Map<Integer, HashSet<Integer>>>> iterator = setOfEntries.iterator();
					// iterate over map
					while (iterator.hasNext()) {
						Entry<String, Map<Integer, HashSet<Integer>>> entry = iterator.next();
						//String token = entry.getKey();
						Map<Integer, HashSet<Integer>> value = entry.getValue();
						//System.out.println(value.size());
						if (value.size() < 2) {
							//System.out.println("removing : " + token);
							// priceMap.remove(entry.getKey()); // wrong - will throw ConcurrentModficationException
							// priceMap.remove(entry.getKey(), entry.getValue()); // wrong - will throw error
							iterator.remove(); // always use remove() method of iterator
						}
					}
					Runtime.getRuntime().gc();
					System.out.println("out: "+index.size());
				}*/


				String[] part = line.split(Constants.CSV_REGEX_SEPARATOR);

				articleIdToPostingPos.put(Integer.parseInt(part[0]), new Pair<>(articleStartPos, line.length()));
				articleStartPos += line.length() + 1; // \n char


				//String text = part[5] + " " + part[4]; // title+text // i need the title first to have a better position later
				String text = part[4] + " " + part[3]; //TODO add search on the author

				String[] textToIndex = text.trim().split(" ");
				Article.totArticlesLength += textToIndex.length;
				Article.nbProcessedArticles++;
				int currentDocID = Integer.valueOf(part[0]);
				articleIDList.add(currentDocID);
				int wordPositionInTextToIndex = 0;//position is 0-based but for no reason XD
				for (String word : textToIndex) {

					if (index.containsKey(word)) {
						if (!index.get(word).containsKey(currentDocID)) {
							//create a doc for it
							index.get(word).put(currentDocID, 1);
							//index.put(word, subIndex2);//do i need to put it back ? i thought it s just a reference ...
						}else{
							index.get(word).put(currentDocID, index.get(word).get(currentDocID)+1 );
						}
					}

					wordPositionInTextToIndex++;
				}

			}
		} catch (IOException e) {
			System.out.println("inputFileName " + inputFileName + " is maybe not found. but some err occured Skip it");
		}

		// for BM25
		Article.saveStaticVar("%d", Article.totArticlesLength, "totArticlesLength");
		Article.saveStaticVar("%d", Article.nbProcessedArticles, "nbProcessedArticles");
		return new Pair<>(index, articleIdToPostingPos);
	}

	/**
	 * this do not compress the index while writing
	 * @param index
	 * @param outputFileName
	 */
	/**
	 * change all to Treemap so that it gets sorted
	 **/
	static void writeIndexToFile(HashMap<String, Map<Integer, HashSet<Integer>>> index, String outputFileName) {
		Map<Integer, HashSet<Integer>> subIndex2;

		try {
			FileWriter fIndex = new FileWriter(outputFileName);
			BufferedWriter OutIndex = new BufferedWriter(fIndex);

			for (String key : index.keySet()) {
				TreeMap<Integer, HashSet<Integer>> sorted = new TreeMap<>(index.get(key));
				OutIndex.write(key + ":" + sorted);
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

	static Map<String, Long> buildDict(String fileName) {
		Article.totArticlesLength = Integer.valueOf(Article.readFileAsString("totArticlesLength"));// for BM25
		Article.nbProcessedArticles = Integer.valueOf(Article.readFileAsString("nbProcessedArticles"));// for BM25

		Map<String, Long> dictionary = new HashMap<String, Long>();
		try {
			RandomAccessFile dicti = new RandomAccessFile(fileName, "r");
			//ArrayList<Long> arrayList = new ArrayList<Long>();

			String curLine = "";

			try {
				Long tempFilePointer = dicti.getFilePointer();
				while ((curLine = dicti.readLine()) != null) {
					String[] text1 = curLine.split(":");
					dictionary.put(text1[0], tempFilePointer);
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

	static Map<String, Long> testOffline(String fileName) {//random acess file works on offline.csv
		try {
			RandomAccessFile dicti = new RandomAccessFile(fileName, "r");
			String curLine = "";

			try {
				Long tempFilePointer = dicti.getFilePointer();
				while ((curLine = dicti.readLine()) != null) {
					System.out.println(curLine);
					System.out.println(tempFilePointer);
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

		return null;

	}

	static Map<String, Map<Integer, Integer> > cachedPostingList = new HashMap<>();

	static public Map<Integer, Integer> getPostingList(String token, IdxDico idxDico) {
		//System.out.println("getPostingList " + token);
		// Add Caching cause we call get multiple time on the same tokens
		if (cachedPostingList.containsKey(token)){
			//System.out.println("from cache :)");
			return cachedPostingList.get(token);
		}
		Map<String, Pair<Integer, Integer>> dictionary = idxDico.tokenToPostingPos;

		Pair<Integer, Integer> pair = dictionary.get(token);
		if (pair != null) {
			Map<Integer, Integer> postingList =  loadOnePostingListFromCompressedIndex(pair.getKey(), pair.getValue());
			cachedPostingList.put(token,postingList);
			return postingList;
		} else {
			return new HashMap<>();
		}
	}

	static Map<Integer, Integer> loadOnePostingListFromCompressedIndex(int start, int length) {
		byte[] b = new byte[length];

		try {//TODO OPEN File ONCE **********
			RandomAccessFile dicti = new RandomAccessFile(Constants.ROOT_DIR + "compressedIndex.Postings", "r");
			dicti.seek(start);
			dicti.read(b, 0, length);
			dicti.close();
		} catch (IOException e) {
			System.out.println("error in loadFromCompressedIndex");
			e.printStackTrace();
		}
		List<Integer> decodedInt = new ArrayList();
		Mutable<Integer> aInt = new Mutable<>(0);
		for (int offset = 0; offset < length; ) {
			offset += VByte.decode(b, offset, aInt);
			decodedInt.add(aInt.getValue());
		}

		Map<Integer, Integer> postingList = new HashMap<>();

		Iterator<Integer> decodedIntIterator = decodedInt.iterator();
		int lastArticleID = 0;
		while (decodedIntIterator.hasNext()) {
			int ArticleID = lastArticleID + decodedIntIterator.next();
			int occurenceInArticle = decodedIntIterator.next();
			/*List<Integer> positions = new ArrayList<>();
			int LastPos = 0;
			for (int i = 0; i < occurenceInArticle; i++) {
				int curPos = LastPos + decodedIntIterator.next();
				positions.add(curPos);
				LastPos = curPos;
			}*/
			postingList.put(ArticleID, occurenceInArticle);
			lastArticleID = ArticleID;
		}

		return postingList;
	}

	/**
	 * for NoncompressedIndex.txt only (!) plz don t use it
	 *
	 * @param query
	 * @param dictionary
	 * @return
	 */
	static String searchQuery(String query, Map<String, Long> dictionary) {
		String[] text1 = null;

		try {
			if (!Constants.SilentOutput){ System.out.println(query);}
			RandomAccessFile dicti = new RandomAccessFile("NoncompressedIndex.txt", "r");
			if (dictionary.get(query) != null) {

				//System.out.println(dictionary.get(query));
				dicti.seek(dictionary.get(query));
				text1 = dicti.readLine().split(":");
			} else {
				text1 = (" : No Match found").split(":");
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
		Map<Integer, Integer> postingList = InvertedIndexer.getPostingList(aToken, idxDico);
		//System.out.println("getArticleIdsInPostingList  "+aToken);
		//System.out.println(postingList.keySet());
		List<Integer> lista = new ArrayList<>(postingList.keySet());
		Collections.sort(lista);
		return lista;
	}
	static public HashMap<String,List<Integer>> getArticleIdsInPostingListForBM25(Set<String> setUniqueTokens, IdxDico idxDico) {
		HashMap<String,List<Integer>> uniqueTokenPostingList= new HashMap<>();
		for (String a: setUniqueTokens){

		uniqueTokenPostingList.put(a,getArticleIdsInPostingList(a, idxDico));
		}
		return uniqueTokenPostingList;

	}

	static public void createDictOfflinecsv(String fileName) {
		Map<Integer, Long> dictionary = new HashMap<Integer, Long>();
		System.out.println("createDictOfflinecsv: Creating dictionary for Offline.csv file. This might take a while. 600Mb <-> 26min/42min");
		try {
			RandomAccessFile dicti = new RandomAccessFile(fileName, "r");

			String curLine = "";
			long startTime = System.currentTimeMillis();

			try {
				Long tempFilePointer = dicti.getFilePointer();
				while ((curLine = dicti.readLine()) != null) {
					if (Math.random() < 0.0005) {System.out.print(":");}
					String[] text1 = curLine.split(Constants.CSV_REGEX_SEPARATOR);
					dictionary.put(Integer.valueOf(text1[0]), tempFilePointer);

					tempFilePointer = dicti.getFilePointer();
					//System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );
				}

				dicti.close();
			} catch (IOException e) {e.printStackTrace();}
			System.out.println("elapsedTime:: ToCreateDictforOfflinecsv : " + (System.currentTimeMillis() - startTime));
		} catch (FileNotFoundException e) {e.printStackTrace();}
		//IdxDico idxDico = new IdxDico();
		IdxDico idxDico = IdxDico.LoadIdxDicoFromfile();
		//System.out.println(dictionary);

		idxDico.offlineArticleID_position = dictionary;
		idxDico.writeThisToFile();
	}

	static public void createDictOfflinecsv1(String fileName) {
		Map<Integer, Long> dictionary = new HashMap<Integer, Long>();
		System.out.println("Creating dictionary for Offline.csv file. This might take a while.");
		Long articleStartPos = 0L;
		try {
			//RandomAccessFile dicti = new RandomAccessFile(fileName, "r");
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			//ArrayList<Long> arrayList = new ArrayList<Long>();

			String curLine = "";
			long startTime = System.currentTimeMillis();

			try {
				while ((curLine = file.readLine()) != null) {
					String[] text1 = curLine.split(Constants.CSV_REGEX_SEPARATOR);
					dictionary.put(Integer.valueOf(text1[0]), articleStartPos);
					articleStartPos+=curLine.getBytes().length+2;
					//System.out.println("elapsedTime:: read : "+ (System.currentTimeMillis() - startTime) );
				}
				System.out.println(dictionary);

				//dicti.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("elapsedTime:: ToCreateDictforOfflinecsv : " + (System.currentTimeMillis() - startTime));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//IdxDico idxDico = new IdxDico();
		IdxDico idxDico = IdxDico.LoadIdxDicoFromfile();

		idxDico.offlineArticleID_position1 = dictionary;
		idxDico.writeThisToFile();

	}


}
         
         
	 
		 
	 

