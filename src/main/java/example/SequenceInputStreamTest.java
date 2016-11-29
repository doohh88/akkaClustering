package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

public class SequenceInputStreamTest {
	public static void main(String[] args) {
		File BASE_DIR = new File(System.getProperty("user.home"));
		String localDir = "cifar";
		String dataBinFile = "cifar-10-batches-bin";
		String testFileName = "test_batch.bin";
		File fullDir = new File(BASE_DIR, localDir);

		try {
			InputStream in = null;
			Collection<File> subFiles = FileUtils.listFiles(new File(fullDir, dataBinFile), new String[] { "bin" }, true);
			System.out.println(subFiles);
			Iterator trainIter = subFiles.iterator();
			in = new SequenceInputStream(new FileInputStream((File) trainIter.next()),	new FileInputStream((File) trainIter.next()));
			
			
			while (trainIter.hasNext()) {
				File nextFile = (File) trainIter.next();
				if (!testFileName.equals(nextFile.getName()))
					in = new SequenceInputStream(in, new FileInputStream(nextFile));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
