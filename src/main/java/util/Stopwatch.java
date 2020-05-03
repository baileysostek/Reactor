package util;

public class Stopwatch {
    private long startTime = 0L; //(0L) for long
    private Double delta   = new Double(0);

    private int averageLastN = 144;
    private Double[] averages;

    public Stopwatch(){
        averages = new Double[averageLastN];
        for(int i = 0; i < averageLastN; i++){
            averages[i] = new Double(0);
        }
    }

    public void setAverageLastN(int averageLastN){
        this.averageLastN = averageLastN;
        averages = new Double[averageLastN];
        for(int i = 0; i < averageLastN; i++){
            averages[i] = new Double(0);
        }
    }

    public void start(){
        startTime = System.nanoTime();
    }

    public void stop(){
        for(int i = 0; i < averageLastN-1; i++){
            averages[(averageLastN)-(i)-1] = averages[(averageLastN)-(i)-2];
        }
        delta = ((double)(System.nanoTime() - startTime) / (1000000000));
        averages[0] = delta;
    }

    public Double getDelta() {
        Double out = new Double(0);
        for(int i = 0; i < averages.length; i++){
            out = (double)(out + averages[i]);
        }
        return (double)(out / (double)averages.length);
    }
}
