package example;

import java.io.FileInputStream;

public class fisTest {
	 public static void main(String [] args){
         FileInputStream fis = null;

         byte [] b = new byte[10];

         try{
                    fis = new FileInputStream("r.txt");     
                    fis.read(b);
                    System.out.println(new String(b));

         }catch(Exception e){
                    e.printStackTrace();
         }
}
}
