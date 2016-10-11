package example;

import java.io.File;

import com.doohh.akkaClustering.util.Util;

public class FileList {
	public static void main(String[] args) {
		String path = Util.getHomeDir() + "/conf";		
		File dirFile = new File(path);
		File[] fileList = dirFile.listFiles();
		for (File tempFile : fileList) {
			if (tempFile.isFile()) {
				String tempFileName = tempFile.getName();
				System.out.println("FileName=" + tempFileName);
			}
		}
	}
}
