package util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.LinkedList;

/**
 * Created by bhsostek on 7/26/2018.
 */
public class DirectoryWatcher implements Runnable{
    private Thread thread;
    private String threadName;
    private boolean running;

    private WatchService watchService;
    private WatchKey     key;

    private LinkedList<Callback> callbacks = new LinkedList<Callback>();

    public DirectoryWatcher( String directoryPath) {
        this.threadName = directoryPath;
        thread = new Thread(this, directoryPath);
        thread.start();
    }

    public void run() {
        running = true;
        String stringPath = this.threadName.replaceFirst("/", "");
        stringPath = StringUtils.getRelativePath() + stringPath;
        stringPath = stringPath.replaceAll("\\\\", "/");
        if(stringPath.endsWith("/")){
            stringPath = stringPath.substring(0, stringPath.length()-1);
        }

        try{
            watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(stringPath);

            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            while (running) {
                key = watchService.take();
                if(key != null) {
                    for (WatchEvent event : key.pollEvents()) {
                        for(Callback callback : callbacks){
                            callback.callback(event.kind(), event.context());
                        }
                    }
                    key.reset();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ClosedWatchServiceException e){
            System.out.println("Removing Directory Watcher from:"+this.threadName);
        }

    }

    public void registerCallback(Callback callback){
        this.callbacks.add(callback);
    }

    public void destrory(){
        running = false;
        if(key != null){
            try {
                key.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(watchService != null){
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
