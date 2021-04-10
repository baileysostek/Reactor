package scene.transition;

import graphics.animation.EnumLoop;
import graphics.animation.Timeline;
import scene.Scene;
import scene.SceneManager;
import util.Callback;

public abstract class Transition {

    private float duration;
    private Timeline transitionTime;

    private Scene transitionTo;

    private Callback sceneSwitchover;
    private Callback onSwitchScene = new Callback() {
        @Override
        public Object callback(Object... objects) {
            return null;
        }
    };

    public Transition(float time){
        this.duration = time;
        this.transitionTime = new Timeline();
        this.transitionTime.setDuration(time);

        sceneSwitchover = new Callback() {
            @Override
            public Object callback(Object... objects) {
                return null;
            }
        };

        this.setCrossoverPoint(0);

        transitionTime.setLoop(EnumLoop.STOP_LAST_VALUE);
    }

    public void update(double delta){
        if(this.transitionTime.isRunning()){
            this.transitionTime.update(delta);
            this.render();
        }
    }

    public abstract void render();

    public float getDuration() {
        return duration;
    }

    public void start(Scene transitionTo){
        this.transitionTo = transitionTo;
        this.transitionTime.start();
    }

    protected void setCrossoverPoint(float time) {
        this.transitionTime.removeCallback(sceneSwitchover);
        sceneSwitchover = new Callback() {
            @Override
            public Object callback(Object... objects) {
                // If we have a callback do it
                if(onSwitchScene != null) {
                    onSwitchScene.callback();
                }
                // If we have a scene to go to, do it.
                if(transitionTo != null) {
                    SceneManager.getInstance().setScene(transitionTo);
                }
                return null;
            }
        };
        this.transitionTime.addCallback(time, sceneSwitchover);
    }

    public void addKeyframeForTrack(String track, float time, float value){
        this.transitionTime.addKeyFrame(track, time, value);
    }

    protected float getKeyframeValue(String track) {
        return this.transitionTime.getValueOf(track);
    }

    public boolean isRunning() {
        return transitionTime.isRunning();
    }

    public void onSwitchScene(Callback onSceneSwitch) {
        this.onSwitchScene = onSceneSwitch;
    }
}