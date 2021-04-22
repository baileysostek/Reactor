package util;


import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.LinkedList;

public class FileObject{
    File file;
    String name;
//    String data;
    String fileExtension;
    String relativePath;
    LinkedList<FileObject> children = new LinkedList<>();

    private HashMap<EnumFileAction, LinkedList<Callback>> fileCallbacks = new HashMap<EnumFileAction, LinkedList<Callback>>();

    private DirectoryWatcher watcher;

    public FileObject(String directory){
        //Get the file Object
        file = new File(StringUtils.getPathToResources()+directory);
        //Define our name
        name = directory;
        //Define our Path
        relativePath = directory;
        //If we are a directory, get our children.
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
                        String newFileName = directory +"/"+ objects[1];
                        children.addLast(new FileObject(newFileName));
                        executeCallbacksFor(EnumFileAction.CREATE, newFileName);
                    }
                    //Delete
                    if(event.equals(StandardWatchEventKinds.ENTRY_DELETE)){
                        for(FileObject file : children){
                            if(file.getRelativePath().endsWith(objects[1].toString())){
                                executeCallbacksFor(EnumFileAction.DELETE, objects[1].toString());
                                children.remove(file);
                                break;
                            }
                        }
                    }
                    //Modify
                    if(event.equals(StandardWatchEventKinds.ENTRY_MODIFY)){
                        String modifiedFileName = relativePath + "/" + fileName;
                        executeCallbacksFor(EnumFileAction.MODIFY, modifiedFileName);
                        StringUtils.recacheFile(modifiedFileName);
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

    public String getFileExtension(){
        return fileExtension;
    }

    public String getRelativePath() {
        return relativePath;
    }

    private void executeCallbacksFor(EnumFileAction action, Object ... args){
        if(this.fileCallbacks.containsKey(action)){
            for(Callback callback : this.fileCallbacks.get(action)){
                callback.callback(args);
            }
        }
    }

    public void registerCallback(EnumFileAction action, Callback callback) {
        if(!this.fileCallbacks.containsKey(action)){
            this.fileCallbacks.put(action, new LinkedList<Callback>());
        }
        this.fileCallbacks.get(action).add(callback);
    }
}
