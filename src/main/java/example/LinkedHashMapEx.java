package example;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LinkedHashMapEx {
	public static void initData(Map<String, String> map){
		for(int i =0 ;i < 10; i++)
			map.put("key" + i, "" + i);
		System.out.println();
	}
	
	public static void printResult(Map<String, String> map){
//		Set<String> set = map.keySet();
//		Iterator<String> iter = set.iterator();
//		while(iter.hasNext()){
//			String key = ((String)iter.next());
//			String value = map.get(key);
//			System.out.println("key : " + key + ", value : " + value);
//		}
		
		for(String st : map.values()){
			System.out.println("value : " + st);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("==== HashMap Test ====");
		Map<String, String> hashMap = new HashMap<String, String>();
		initData(hashMap);
		printResult(hashMap);
		
		System.out.println("==== hashTable Test ====");
		Map<String, String> hashTable = new Hashtable<String, String>();
		initData(hashTable);
		printResult(hashTable);
		
		System.out.println("==== hashTable Test ====");
		Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();
		initData(linkedHashMap);
		printResult(linkedHashMap);
	}
}
