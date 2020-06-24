package util;

import java.nio.file.*;
import java.util.LinkedList;

/**
 * Created by bhsostek on 7/26/2018.
 */
public class DirectoryWatcher implements Runnable{
    private Thread thread;
    private String threadName;
    private boolean running;

    private LinkedList<Callback> callbacks = new LinkedList<Callback>();

    public DirectoryWatcher( String directoryPath) {
        this.threadName = directoryPath;
        thread = new Thread(this, directoryPath);
        thread.start();
    }

    public void run() {
        running = true;
        String stringPath = this.threadName.replaceFirst("/", "");
        stringPath = (stringPath.substring(0, stringPath.length()-1));
        stringPath = stringPath.replaceAll("/", "\\\\");
        final Path path = Paths.get(stringPath);

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (running) {
                try {
                    final WatchKey wk = watchService.take();
                    for (WatchEvent<?> event : wk.pollEvents()) {
                        //we only register "ENTRY_MODIFY" so the context is always a Path.
                        final Path changed = (Path) event.context();
                        for (Callback callback : callbacks) {
                            callback.callback(changed);
                        }
                    }
                }catch (Exception e){
                    //Stuff the error under the rug... TODO:stop these watcher threads gracefully
                    running = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerCallback(Callback callback){
        this.callbacks.add(callback);
    }

    public void destrory(){
        running = false;
        if(thread.isAlive()){
            thread.stop();
        }
    }
}
