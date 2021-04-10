package scene;

import org.joml.Vector2f;
import org.joml.Vector3f;
import scene.transition.FadeOutFadeIn;
import scene.transition.Transition;
import util.Callback;

import java.util.LinkedList;

public class SceneManager {
    private static SceneManager manager;

    private Scene loadedScene = null;
    private LinkedList<Scene> scenes = new LinkedList<Scene>();

    private Transition transition;

    private static final Transition FADE_TO_BLACK = new FadeOutFadeIn(2, new Vector3f(0));

    private SceneManager(){
//        this.setScene(new LogoScene());
//        this.setScene(new OverworldMap());
    }

    public void update(double delta){
        if(transition != null){
            if(transition.isRunning()){
                transition.update(delta);
            }
        }
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

    public void setScene(Scene scene, Transition transition){
        this.transition = transition;
        this.transition.start(scene);
    }

    public void setScene(Scene scene, Transition transition, Callback onSceneSwitch){
        this.transition = transition;
        this.transition.onSwitchScene(onSceneSwitch);
        this.transition.start(scene);
    }

    //Baked transitions
    public final Transition FadeToBlack(){
        return FADE_TO_BLACK;
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
