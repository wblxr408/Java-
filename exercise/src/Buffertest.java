import java.io.*;
import java.util.ArrayList;

public class Buffertest {
    private ArrayList<Integer> intStoreSource;
    private ArrayList<Integer> intStoreTarget;
    private final int MAX=500000;
    private File testfile;

    public Buffertest(){}
    public Buffertest(File file){
        intStoreSource=new ArrayList<Integer>();
        intStoreTarget=new ArrayList<Integer>();
        for(int i = 0;i<=MAX;i++){
            this.initialSource();
        }
        this.testfile=file;
    }

    public int randomInt(){
        return (int)(Math.random()*255);
    }

    public void initialSource(){
        intStoreSource.add(randomInt());
    }
    /*
    * 使用FileInputStream读取文件里的整数，并存储到intStoreTarget列表里
    * */
    public void useFileInput() throws IOException{
        FileInputStream fis=new FileInputStream(testfile);
        int value=fis.read();
        while(value!=-1){
            this.intStoreTarget.add(value);
            value=fis.read();
        }
    }



}
