package de.intsys.krestel.SearchEngine.kimhuffman;

import java.io.Serializable;

public class TrieNode implements Serializable {

	static int size; 		// size of the trie

	public char nodechar;			// e
	public int frequency;			// 141
	StringBuffer code;		// 010
	public TrieNode left,right;	// None
	public TrieNode(char ch,int fr){
		nodechar=ch;
		frequency=fr;
		size++;
		left=right=null;
	}
	public TrieNode(char ch,int fr,TrieNode left,TrieNode right){
		nodechar=ch;
		frequency=fr;
		size++;
		this.left=left;
		this.right=right;
	}
}
