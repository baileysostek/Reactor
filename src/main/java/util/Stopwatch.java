package util;

public class Stopwatch {
    private long startTime = 0L; //(0L) for long
    private Double delta   = new Double(0);

    protected Stopwatch(){

    }


    public void start(){
        startTime = System.nanoTime();
    }

    public void stop(){
        delta += ((double)(System.nanoTime() - startTime) / (1000000000));
    }

    public Double getDelta() {
        return delta;
    }

    public void clear(){
        delta = Double.valueOf(0);
    }
}
