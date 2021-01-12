package util;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;

public class StopwatchManager{

    private static StopwatchManager manager;
    private HashMap<String, Stopwatch> stopwatches = new HashMap<String, Stopwatch>();

    private DecimalFormat format;

    private StopwatchManager(){
        format = new DecimalFormat("###.####");
    }

    public void printAllDeltas() {
        float frameTime = 0;
        for(String name : stopwatches.keySet()){
            Stopwatch watch = stopwatches.get(name);
            String number = format.format(watch.getDelta() * 1000d);
            frameTime += Float.parseFloat(number);
            System.out.println(padStart(name, 32, ' ')+":"+format.format(watch.getDelta() * 1000d)+"ms");
        }
        System.out.println(padStart("", 32, '-'));
        System.out.println("Total:" + frameTime);
    }


    private String padStart(String name, int length, char c){
        String out = name;
        while(out.length() < length){
            out += c;
        }
        return out;
    }

    public void update(double delta){
        float frameTime = 0.0f;
        for(Stopwatch stopwatch : stopwatches.values()){
            frameTime += stopwatch.getDelta();
        }

    }

    public void clearAll(){
        for(Stopwatch s : stopwatches.values()){
            s.clear();
        }
    }

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

    public static void initialize() {
        if(manager == null){
            manager = new StopwatchManager();
        }
    }

    public static StopwatchManager getInstance(){
        return manager;
    }
}
