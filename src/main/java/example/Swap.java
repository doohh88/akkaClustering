package example;

public class Swap {
	static void swap(String a, String b){
		a = "world";
		b = "hello";
	}
	
	public static void main(String[] args) {
		String a = "hello";
		String b = "world";
		swap(a, b);
		System.out.println(a);
		System.out.println(b);
	}
	
}
