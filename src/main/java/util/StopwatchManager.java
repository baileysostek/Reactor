package util;

import engine.Engine;

import java.util.Collection;
import java.util.HashMap;

public class StopwatchManager extends Engine {

    private static StopwatchManager manager;
    private HashMap<String, Stopwatch> stopwatches = new HashMap<String, Stopwatch>();

    private StopwatchManager(){

    }

    public void update(double delta){
        float frameTime = 0.0f;
        for(Stopwatch stopwatch : stopwatches.values()){
            frameTime += stopwatch.getDelta();
        }

    }

    @Override
    public void onShutdown() {

    }

    public void addTimer(String timer){
        Stopwatch stopwatch = new Stopwatch();
        this.stopwatches.put(timer, stopwatch);
    }

    public void removeTimer(String timer){
        this.stopwatches.remove(timer);
    }

    public Stopwatch getTimer(String timer){
        return this.stopwatches.get(timer);
    }

    public Collection<Stopwatch> getStopwatches(){
        return this.stopwatches.values();
    }

    public static StopwatchManager getInstance(){
        if(manager == null){
            manager = new StopwatchManager();
        }
        return manager;
    }
}
