package scene;

import input.MousePicker;
import org.joml.Vector2f;
import org.joml.Vector3f;
import scene.transition.FadeOutFadeIn;
import scene.transition.Transition;
import util.Callback;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;

public class SceneManager {
    private static SceneManager manager;

    private Scene loadedScene = null;
    private Stack<Scene> overlays = new Stack<>();
    private LinkedHashMap<Scene, LinkedList<OverlayFlags>> overrideFlags = new LinkedHashMap<>();

    private Transition transition;

    private static final Transition FADE_TO_BLACK = new FadeOutFadeIn(2, new Vector3f(0));
    private static final Transition FADE_TO_BLACK_QUICK = new FadeOutFadeIn(0.5f, new Vector3f(0));

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

        boolean canTickBase = true;

        if(this.overlays.size() > 0){
            if(overrideFlags.containsKey(this.overlays.firstElement())){
                LinkedList<OverlayFlags> flags = overrideFlags.get(this.overlays.firstElement());
                if(flags.contains(OverlayFlags.PAUSE_UPDATE_PREVIOUS_SCENE)){
                    canTickBase = false;
                }
            }
        }

        if(canTickBase) {
            if (this.loadedScene != null) {
                loadedScene.update(delta);
            }
        }

        for(Scene overlay : overlays){
            overlay.update(delta);
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

        boolean canRenderBase = true;

        if(this.overlays.size() > 0){
            if(overrideFlags.containsKey(this.overlays.firstElement())){
                LinkedList<OverlayFlags> flags = overrideFlags.get(this.overlays.firstElement());
                if(flags.contains(OverlayFlags.PAUSE_RENDER_PREVIOUS_SCENE)){
                    canRenderBase = false;
                }
            }
        }

        if(canRenderBase) {
            if (this.loadedScene != null) {
                loadedScene.render();
            }
        }

        for (Scene overlay : overlays){
            overlay.render();
        }
    }

    public void popOverlay(Scene overlay){
        if(overlays.contains(overlay)){
            overlays.remove(overlay);
            if(overrideFlags.containsKey(overlay)){
                overrideFlags.remove(overlay);
            }
            overlay.onUnload();
        }
    }

    public void overlayScene(Scene overlay, Transition transition, OverlayFlags ... flags){
        this.transition = transition;
        this.transition.onSwitchScene(new Callback() {
            @Override
            public Object callback(Object... objects) {
                SceneManager.this.overlayScene(overlay);
                return null;
            }
        });
        this.transition.start(null);

        if(!overrideFlags.containsKey(overlay)){
            overrideFlags.put(overlay, new LinkedList<>());
        }

        for(OverlayFlags flag : flags){
            overrideFlags.get(overlay).push(flag);
        }

    }

    public void overlayScene(Scene overlay, OverlayFlags ... flags){
        if(!overrideFlags.containsKey(overlay)){
            overrideFlags.put(overlay, new LinkedList<>());
        }

        for(OverlayFlags flag : flags){
            overrideFlags.get(overlay).push(flag);
        }

        overlayScene(overlay);
    }

    public void overlayScene(Scene overlay){
        this.overlays.add(overlay);
        overlay.onLoad();
    }

    public void setScene(Scene scene){
        if(scene == null){
            return;
        }
        if(scene != this.loadedScene){
            if(this.loadedScene != null) {
                this.loadedScene.onUnload();
            }
            for(Scene overlay : overlays){
                overlay.onUnload();
            }
            overlays.clear();
            this.loadedScene = scene;
            this.loadedScene.onLoad();
            this.onChangeScene(this.loadedScene);
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

    private void onChangeScene(Scene newScene){
        // Get all of our hooks working
        MousePicker.getInstance().onSceneChange(newScene);
    }

    public Scene getCurrentScene(){
        return this.loadedScene;
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
