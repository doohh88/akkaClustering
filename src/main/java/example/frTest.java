package example;

import java.io.FileReader;

public class frTest {
    public static void main(String [] args){
        FileReader fr = null;
       
        char [] c = new char[10];
       
        try{
                   fr = new FileReader("r.txt");
                   fr.read(c);
                   System.out.println(new String(c));
                  
        }catch(Exception e){
                   e.printStackTrace();
        }
}
}
