package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

public class SequenceInputStream2 {
	public static void main(String[] args) {
		try {
			byte[] b = new byte[8];
			InputStream f = new FileInputStream("c.txt");
			InputStream in = new SequenceInputStream(new FileInputStream("a.txt"),	new FileInputStream("b.txt"));
			//in.skip(18);
			
			//in.read(b);
			//System.out.println(new String(b));
//			in.read(b);
//			System.out.println(new String(b));
//			in.read(b);
//			System.out.println(new String(b));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
