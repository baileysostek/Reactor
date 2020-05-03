package util;

public class Async implements Runnable {

    private Thread thread;
    private Callback callback;
    private Callback then;
    private boolean isRunning = false;


    public Async(Callback callback){
        this.callback = callback;
        this.thread = new Thread(this);
    }

    public void start(){
        if(!this.isRunning) {
            this.isRunning = true;
            this.thread.start();
        }
    }

    public void stop(){
        if(isRunning) {
            this.isRunning = false;
            this.thread.stop();
            this.then.callback();
        }
    }

    @Override
    public void run() {
        this.callback.callback();
    }

    public void then(Callback callback) {
        this.then = callback;
    }
}
