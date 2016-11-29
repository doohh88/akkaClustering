package example;

import java.util.Hashtable;

public class HashTableTest {
	public static void main(String[] args) {
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		//for(int i =0 ;i < 10; i++)
		ht.put("asdf", 1);
		ht.put("gsdfg", 2);
		ht.put("qwer", 3);
		ht.put("sdfgh", 4);
		ht.put("zxcvb", 5);

		
		
		for(int i: ht.values())
			System.out.println(i);
	}
}
