package de.intsys.krestel.SearchEngine;

import java.io.*;
import java.util.*;

public class InvertedIndexer {
	

	public InvertedIndexer() {
		// TODO Auto-generated constructor stub
		 
	}
	
	 static void buildIndex(String fileName){
		 Map<Integer, HashSet<Integer>> subIndex;//[document id,[pos1,pos2,pos3]]
		 Map<Integer, HashSet<Integer>> subIndex2;
		 HashMap<String, Map<Integer, HashSet<Integer>>> index;//[word, subIndex]
		 index = new HashMap<String, Map<Integer, HashSet<Integer>>>();
		 try(BufferedReader file = new BufferedReader(new FileReader(fileName)))
		 {
			 //int i = 0;//article number
             String line;
             while( (line = file.readLine()) !=null) {
            	 String[] part = line.split(Constants.CSV_SEPARATOR);

            	 String text=part[5]+" "+part[4]; // i need the title first to have a better position later

            	 String[] text1 = text.trim().split(" ");
            	 int j=0;//position
                 for(String word:text1){
                	 //System.out.println(word);
                	 subIndex2 = new HashMap<Integer, HashSet<Integer>>();
                	 
                    if (!index.containsKey(word)) {
                    	 subIndex = new HashMap<Integer, HashSet<Integer>>();
                    	 subIndex.put(Integer.valueOf(part[0]),new HashSet<Integer>());
                    	 index.put(word, subIndex);
                    	 
                    	 
                    }
                    else {
                    	
                    	if(!index.get(word).containsKey(Integer.valueOf(part[0]))) {
                        subIndex2 = index.get(word);
                        subIndex2.put(Integer.valueOf(part[0]), new HashSet<Integer>());
                       
                        index.put(word,subIndex2);
                    	}
                    
                    	
                    }
                    if(index.get(word).containsKey(Integer.valueOf(part[0]))) {
                      	 index.get(word).get(Integer.valueOf(part[0])).add(j);
                       //System.out.println(index.get(word).get(i));
                      	 } 
                     
                     j++;
                     
                 }
                
             }
         } catch (IOException e){
             System.out.println("File "+fileName+" not found. Skip it");
         }
		 
		 try {
			 FileWriter fIndex	 = new FileWriter("index.txt");
			 BufferedWriter OutIndex = new BufferedWriter(fIndex);
			
			for(String key:index.keySet()) {
				subIndex2=index.get(key);
				
				OutIndex.write(key + ": [");
				int n = subIndex2.size();
				int j=n-1;
				for(Integer key2:subIndex2.keySet()) {
				OutIndex.write("["+key2 +","+subIndex2.get(key2) + "]" );
				if(j>0) {
					OutIndex.write(",");
				}j--;
				}
				OutIndex.write("]");
				OutIndex.newLine();
				
			}
			
			
			
			fIndex.flush();
			OutIndex.flush();
			fIndex.close();
			OutIndex.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 
		
		 }
	 
	 static Map<String, Long> buildDict(String fileName){
		 
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
	 
	 static String searchQuery(String query, Map<String, Long> dictionary) {
		 String[] text1 = null;
		 
		 try {
				RandomAccessFile dicti = new RandomAccessFile("index.txt","r");
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
	 
         
         }
         
         
	 
		 
	 

