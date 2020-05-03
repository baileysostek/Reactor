package scene;


import math.Vector2f;
import org.lwjgl.opengl.GL20;

import java.util.LinkedList;

public class SceneManager {
    private static SceneManager manager;

    private Scene loadedScene = null;
    private LinkedList<Scene> scenes = new LinkedList<Scene>();

    private SceneManager(){
//        this.setScene(new LogoScene());
//        this.setScene(new OverworldMap());
    }

    public void update(double delta){
        if(this.loadedScene != null){
            loadedScene.update(delta);
        }
    }

    public void onPress(Vector2f motionEvent) {
        if(this.loadedScene != null){
            this.loadedScene.onPress(motionEvent);
        }
    }

    public void onMove(Vector2f motionEvent) {
        if(this.loadedScene != null){
            this.loadedScene.onMove(motionEvent);
        }
    }

    public void onRelease(Vector2f motionEvent) {
        if(this.loadedScene != null){
            this.loadedScene.onRelease(motionEvent);
        }
    }

    public void render(){
        //Clear frame
        GL20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if(this.loadedScene != null){
            try {
                loadedScene.render();
            }catch(NullPointerException e){

            }
        }
    }

    public void setScene(Scene scene){
        if(scene != this.loadedScene){
            if(this.loadedScene != null) {
                this.loadedScene.onUnload();
            }
        }
        this.loadedScene = scene;
        this.loadedScene.onLoad();
    }

    //Singleton
    public static void initialize(){
        if(manager == null){
            manager = new SceneManager();
        }
    }

    public static SceneManager getInstance(){
        return manager;
    }
}
