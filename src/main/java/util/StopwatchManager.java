package util;

import graphics.ui.UIManager;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedHashMap;

public class StopwatchManager{

    private static StopwatchManager manager;
    private LinkedHashMap<String, Stopwatch> stopwatches = new LinkedHashMap<String, Stopwatch>();

    private DecimalFormat format;
    private DecimalFormat format2;

    private StopwatchManager(){
        format  = new DecimalFormat("##.########");
        format2 = new DecimalFormat("####.##");
    }

    public void drawTextDeltas(int xOffset, int yOffset) {
        float frameTime = 0;
        int index = 0;

        double total = 0;
        for(Stopwatch stopwatch : stopwatches.values()){
            total += stopwatch.getDelta();
        }

        double totalFrameTimeMS = 0;

        for(String name : stopwatches.keySet()){
            Stopwatch watch = stopwatches.get(name);
            String number = format.format(watch.getDelta());
            frameTime += Float.parseFloat(number);
            double frameDelta = watch.getDelta() * 1000f;
            totalFrameTimeMS += frameDelta;
            UIManager.getInstance().drawText(xOffset, index * UIManager.getInstance().getCurrentTextSize() + yOffset, padEnd(name, 32, ' ')+ ":" + padStart(format.format(frameDelta)+"ms", 8, ' ') + " " + padStart(format2.format(100f * (watch.getDelta()  / total)) +"%", 8, ' '));
            index++;
        }
        UIManager.getInstance().drawText(xOffset, index * UIManager.getInstance().getCurrentTextSize() + yOffset,"FPS:" +  padStart(format2.format(1f / frameTime), 6, ' ') + "MS:" + totalFrameTimeMS);
    }

    public void drawFrameTimer() {
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
            out = c + out;
        }
        return out;
    }

    private String padEnd(String body, int length, char c){
        String out = body;
        while(out.length() < length){
            out += c;
        }
        return out;
    }

    public void update(double delta){
//        float frameTime = 0.0f;
//        for(Stopwatch stopwatch : stopwatches.values()){
//            frameTime += stopwatch.getDelta();
//        }

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
