package util;


import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.LinkedList;

public class FileObject{
    File file;
    String name;
//    String data;
    String fileExtension;
    String relativePath;
    LinkedList<FileObject> children = new LinkedList<>();

    private DirectoryWatcher watcher;

    public FileObject(String directory){
        file = new File(StringUtils.getRelativePath()+directory);
        name = directory;
        relativePath = directory;
        if(file.isDirectory()){
            watcher = new DirectoryWatcher(directory);
            watcher.registerCallback(new Callback() {
                @Override
                public Object callback(Object... objects) {
                    System.out.println("Changed within file");
                    System.out.println(objects[0]);

                    String fileName = objects[1].toString();
                    System.out.println(fileName);

                    WatchEvent.Kind<StandardWatchEventKinds> event = (WatchEvent.Kind<StandardWatchEventKinds>) objects[0];
                    //Create
                    if(event.equals(StandardWatchEventKinds.ENTRY_CREATE)){
                        children.addLast(new FileObject(directory +"/"+ objects[1]));
                    }
                    //Delete
                    if(event.equals(StandardWatchEventKinds.ENTRY_DELETE)){
                        for(FileObject file : children){
                            if(file.getRelativePath().endsWith(objects[1].toString())){
                                children.remove(file);
                                break;
                            }
                        }
                    }
                    //Modify
                    if(event.equals(StandardWatchEventKinds.ENTRY_MODIFY)){
                        System.out.println("File:" + relativePath + "/" + fileName);
                        StringUtils.recacheFile(relativePath + "/" + fileName);
                    }
                    return null;
                }
            });

            for(String name : file.list()){
                children.addLast(new FileObject(directory +"/"+ name));
            }
            fileExtension = ".dir";
        }else{
//            data = StringUtils.load(directory);
            relativePath = directory;
            System.out.println(directory);
            fileExtension = directory.substring(directory.lastIndexOf("."), directory.length()).toLowerCase();
            name = directory.substring(directory.lastIndexOf("/", directory.length()));
        }
    }

    public void onShutdown(){
        watcher.destroy();
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

    public String getFileExtension(){
        return fileExtension;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
