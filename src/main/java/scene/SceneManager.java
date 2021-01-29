package scene;

import org.joml.Vector2f;

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
            this.loadedScene = scene;
            this.loadedScene.onLoad();
        }
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
