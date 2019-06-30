package de.intsys.krestel.SearchEngine.kimhuffman;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class kimHuffman implements Serializable  {
    TrieNode root; // code to char
    HashMap<Character,String> charToCode;
    boolean treeConstructed;
    int compressedTxtSize;
    public static Comparator<TrieNode> idComparator = new Comparator<TrieNode>(){

        @Override
        public int compare(TrieNode c1, TrieNode c2) {
            return (c1.frequency - c2.frequency);
        }
    };
    public kimHuffman(){
        charToCode = new HashMap<>();
        treeConstructed = false;
    }

    public String encodeOnce(String Tx){
        if (treeConstructed) {
            throw new RuntimeException("Tree of huffman already constructed use an other huffman Obj");
        }

        treeConstruct(frequencyCount(Tx));
        createCharToCodeMap();
        treeConstructed = true;

        StringBuilder strBuilder = new StringBuilder();
        for( Character c : Tx.toCharArray() ) {
            strBuilder.append(charToCode.get(c));
        }
        String str01 = strBuilder.toString();
        compressedTxtSize = str01.length();
        return str01;
    }
    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }
    public static void fileEncodeNwrite(String inputFile, String outputFile) {
        try {
            String Tx = readFile(inputFile);
            kimHuffman huff = new kimHuffman();
            huff.encodeNwrite( Tx,  outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void encodeNwrite(String Tx, String outputFile){
        String str01 = this.encodeOnce(Tx);
        // you can only append Bytes
        String add = "";
        if (str01.length() % 8 > 0) {
            for (int i = 0; i < (8 - (str01.length() % 8)); i++) add += "0";
        }
        str01+=add;

        //combine each 8 bits
        int i = 0;
        int build = 0;
        StringBuilder result = new StringBuilder();
        for (char c :str01.toCharArray()) {
            if (c == '1') {
                build += 1;
            }
            build <<= 1;
            i++;
            if (i == 8) {
                result.append( (char) build );
                i = 0;
                build = 0;
            }
        }
        if (i!=0) throw new RuntimeException("WOT");
        //to file
        try (OutputStream outstream = new BufferedOutputStream(new FileOutputStream(outputFile, false), 1024)) {
            outstream.write( result.toString().getBytes());
        } catch (IOException e) {e.printStackTrace();}
        writeThisToFile(outputFile+".huffmanTree");
    }
    public static String fileDecode(String file){
        try{
            FileInputStream fileInputStream
                    = new FileInputStream(file+".huffmanTree");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            kimHuffman huff = (kimHuffman) objectInputStream.readObject();
            objectInputStream.close();

            return huff.fileDecodeWithTree(file);

    } catch (Exception e) {e.printStackTrace();}
        return "";
    }
    public String fileDecodeWithTree(String file){
        StringBuilder decoded = new StringBuilder();
        TrieNode actualPos = root;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file) );
            int totread = 0;
            for (byte b:bytes) {
                byte test = 1;
                for (int i = 0; i < 8; i++) {
                    if ( (b&test) ==0){//go left
                        actualPos = actualPos.left;
                    }else{
                        actualPos = actualPos.right;
                    }
                    test<<=1;

                    if ('!' != actualPos.nodechar ){
                        decoded.append(actualPos.nodechar);
                        actualPos = root;
                    }
                    totread++;
                    if (totread>=compressedTxtSize) return decoded.toString();
                }
            }
        } catch (IOException e) {e.printStackTrace();}
        throw new RuntimeException("error look for me in kimHuffman");
    }
    public void treeConstruct(HashMap<Character, Integer> frequencyMap){
        //PriorityQueue<TreeNode> leafNode = new PriorityQueue<TreeNode>(binaryFrequency.size(), idComparator);

        PriorityQueue<TrieNode> pqueue = new PriorityQueue<TrieNode>(frequencyMap.size() , idComparator);
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet())
            pqueue.add(new TrieNode(entry.getKey(), entry.getValue()));

        while (pqueue.size() > 1) {
            TrieNode first = pqueue.poll();
            TrieNode second = pqueue.poll();
            pqueue.add(new TrieNode('!',first.frequency + second.frequency ,first, second));
        }

        root = pqueue.peek();
    }
    public static HashMap<Character, Integer> frequencyCount(String Tx) {
        HashMap<Character, Integer> binaryFrequency = new HashMap<>();

            for( Character binary : Tx.toCharArray() ) {

            if (binaryFrequency.containsKey(binary))
                binaryFrequency.put(binary, binaryFrequency.get(binary) + 1);
            else
                binaryFrequency.put(binary, 1);
        }
        return binaryFrequency;
    }
    public void createCharToCodeMap() {
        createCharToCodeMap(root, "");
        System.out.println(charToCode);
    }
    private void createCharToCodeMap(TrieNode actualPos, String prefix){
        if ('!' != actualPos.nodechar ){
            charToCode.put(actualPos.nodechar, prefix);
            return;
        }else{
            if (actualPos.left!=null){//go left
                createCharToCodeMap(actualPos.left,prefix+'0');
            }
            if (actualPos.right!=null){//go right
                createCharToCodeMap(actualPos.right, prefix+'1');
            }
        }

    }

    public static void main(String[] args) {


        long startTime1 = System.currentTimeMillis();
        fileEncodeNwrite("C:\\Inn\\Github\\the-crawler-search-engine\\tokens.txt", "testo");
        System.out.println("");
        System.out.println("elapsedTime::  kimHuffman fileEncodeNwrite: "+ (System.currentTimeMillis() - startTime1) );

        long startTime = System.currentTimeMillis();
        System.out.println(fileDecode("testo") ) ;
        System.out.println("elapsedTime::  kimHuffman fileDecode: "+ (System.currentTimeMillis() - startTime) );

        /*kimHuffman huffman = new kimHuffman();
        huffman.encodeNwrite("Semper Fi","testo");
        huffman.writeThisToFile("testo");
        huffman.fileDecode("testo");*/
    }
    public void writeThisToFile(String file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
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
