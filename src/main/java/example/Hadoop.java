package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Hadoop {
	public static void main(String[] args) throws Exception {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(new URI("hdfs://163.152.21.205:9000"), conf);
			//Path filePath = new Path("/helloWorld");
			Path filePath = new Path("/LICENSE.txt");
			FSDataInputStream fsDataInputStream = fs.open(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fsDataInputStream));
			String line;
			line = br.readLine();
			for(int i =0 ;i < 10; i++){
				line = br.readLine();
				System.out.println(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
