package graphics.renderer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpriteManager {
    //Singleton instance
    private static SpriteManager spriteManager;

    private LinkedList<Integer> textureIDs = new LinkedList<>();
    private HashMap<String, Integer> loadedImages = new HashMap<String, Integer>();
    private LinkedList<Integer> toGenerate = new LinkedList<>();

    //Lock for locking our entity set
    private Lock lock;

    //Private constructor because singleton.
    private SpriteManager(){
        lock = new ReentrantLock();
    }

    //Singleton instances
    public static void initialize(){
        if(spriteManager == null){
            spriteManager = new SpriteManager();
        }
    }

    public static SpriteManager getInstance(){
        return spriteManager;
    }

    public void onShutdown(){
        for(int texture: textureIDs){

        }
    }

}
