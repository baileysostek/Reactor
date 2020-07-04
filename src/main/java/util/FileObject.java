package util;


import java.io.File;
import java.util.LinkedList;

public class FileObject{
    File file;
    String name;
//    String data;
    String relativePath;
    LinkedList<FileObject> children = new LinkedList<>();

    public FileObject(String directory){
        file = new File(StringUtils.getRelativePath()+directory);
        name = directory;
        relativePath = directory;
        if(file.isDirectory()){
            for(String name : file.list()){
                children.addLast(new FileObject(directory +"/"+ name));
            }
        }else{
//            data = StringUtils.load(directory);
            relativePath = directory;
        }
    }

    public File getFile(){
        return file;
    }

    public LinkedList<FileObject> getChildren(){
        return this.children;
    }

    public boolean isDirectory(){
        return file.isDirectory();
    }

    public String getName(){
        return name;
    }

//    public String getData(){
//        return this.data;
//    }

    public String getRelativePath() {
        return relativePath;
    }
}
