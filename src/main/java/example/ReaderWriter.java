package example;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReaderWriter {
	public static void main(String[] args) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader("LICENSE.txt");
			bufferedReader = new BufferedReader(fileReader);
			
			/*int i =0;
			while(i != -1){
				i = fileReader.read();
				System.out.print((char)i);
			}*/
			
			String string = new String();
			do{
				string = bufferedReader.readLine();
				System.out.println(string);
			} while(string != null);
		} catch (Exception e) {
			System.out.println("error");
		} finally {
			try{
				//fileReader.close();
				bufferedReader.close();
			}
			catch(Exception e) {
			  System.out.println("fail to close file");
			}
		}
	}
}
