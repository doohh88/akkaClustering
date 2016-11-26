package example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileInputStreamExam {
	public static void main(String[] args) {
		String filePath ="LICENSE.txt";
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		BufferedInputStream bufferedInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		
		try {
			inputStream = new FileInputStream(filePath);
			bufferedInputStream = new BufferedInputStream(inputStream);
			
			outputStream = new FileOutputStream("w.txt");
			bufferedOutputStream = new BufferedOutputStream(outputStream);
					
			
			//byte[] readBuffer = new byte[1024];
			byte[] readBuffer = new byte[bufferedInputStream.available()];
			/*while(inputStream.read(readBuffer, 0, readBuffer.length) != -1)
				outputStream.write(readBuffer);*/
			
			while(bufferedInputStream.read(readBuffer, 0, readBuffer.length) != -1)
				bufferedOutputStream.write(readBuffer);
			/*while(bufferedInputStream.read(readBuffer) != -1)
				bufferedOutputStream.write(readBuffer);*/
			
		} catch (Exception e) {
			System.out.println("error");
		} finally {
			try {
//				inputStream.close();
//				outputStream.close();
				bufferedInputStream.close();
				bufferedOutputStream.close();
			} catch (Exception e2) {
				System.out.println("fail to close file");
			}
		}
	}
}
