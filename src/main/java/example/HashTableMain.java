package example;

import java.util.Hashtable;

public class HashTableMain {
	public static void main(String[] args) {
		Hashtable<String, Integer> hashTable = new Hashtable<String, Integer>();
		hashTable.put("abc", 0);
		hashTable.put("def", 0);
		//System.out.println(hashTable);
		hashTable.put("abc", hashTable.get("abc") + 1);
		//System.out.println(hashTable);
		hashTable.put("abc", hashTable.get("abc") + 1);
		System.out.println(hashTable);
	}
}
