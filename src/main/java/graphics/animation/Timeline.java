package graphics.animation;

import util.Callback;

import java.util.HashMap;
import java.util.LinkedList;

public class Timeline {

    private EnumLoop loop = EnumLoop.STOP;
    private HashMap<Callback, Float> callbacks = new HashMap<>();
    private HashMap<String, KeyFrame> keyFrames = new HashMap<>();
    private float time     = 0f;
    private float duration = 0f;

    private boolean running = false;

    //We only want to execute each callback once per cycle
    private LinkedList<Callback> closedList = new LinkedList<>();

    public Timeline(){

    }

    public void addCallback(float position, Callback callback ){
        callbacks.put(callback, position);
    }

    public void setDuration(float v) {
        this.duration = v;
    }

    public void start() {
        this.running = true;
        this.time = 0f;
        closedList.clear();
        for(Callback c : callbacks.keySet()){
            if(callbacks.get(c) == 0){
                c.callback();
                closedList.add(c);
            }
        }
    }

    public void pause(){
        this.running = false;
    }

    public void resume(){
        this.running = true;
    }

    public void stop(){
        this.running = false;
        this.time = 0;
    }

    public float getValueOf(String key){
        if(this.keyFrames.containsKey(key)){
            return this.keyFrames.get(key).getValue(this.time);
        }

        return 0;
    }

    public void addKeyFrame(String key, float position, float value){
        KeyFrame frame = new KeyFrame(value, position);
        KeyFrame parent = this.keyFrames.get(key);

        //If we dont have the frame, add it
        if(parent == null){
            this.keyFrames.put(key, frame);
            return;
        }

        //Get parents until we have none
        while(parent != null){
            if(parent.getNext() == null){
                break;
            }
            parent = parent.getNext();
        }

        //Get last parent
        parent.setNext(frame);
    }

    public void update(double delta) {
        if(this.running){
            float lastTime = this.time;
            this.time += delta;
            for(Callback callback : callbacks.keySet()){
                if(!closedList.contains(callback)) {
                    if (lastTime <= callbacks.get(callback) && this.time >= callbacks.get(callback)) {
                        callback.callback();
                        closedList.add(callback);
                    }
                }
            }
            if(time >= this.duration){
                switch (loop){
                    case STOP:{
                        this.stop();
                        break;
                    }
                    case STOP_LAST_VALUE:{
                        this.stop();
                        this.time = duration;
                        break;
                    }
                    case LOOP:{
                        this.time -= this.duration;
                        break;
                    }
                }
            }
        }
    }

    public float getDuration() {
        return this.duration;
    }

    public void removeCallback(Callback callback){
        if(this.callbacks.containsKey(callback)){
            this.callbacks.remove(callback);
        }
        if(this.closedList.contains(callback)){
            this.closedList.remove(callback);
        }
    }

    public float getTime(){
        return time;
    }

    public void setLoop(EnumLoop loop) {
        this.loop = loop;
    }

    public boolean isRunning() {
        return this.running;
    }
}
