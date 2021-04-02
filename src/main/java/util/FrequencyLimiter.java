package util;

public class FrequencyLimiter {

    private int callsPerSecond = 1;
    private float delta;
    private float target;
    private Callback callback;

    public FrequencyLimiter(int callsPerSecond, Callback callback){
        this.callsPerSecond = callsPerSecond;
        this.callback = callback;
        this.target = 1.0f / (float)callsPerSecond;
    }

    public void update(double delta){
        this.delta += delta;
        if(this.delta >= target){
            this.delta -= target;
            callback.callback();
        }
    }
}