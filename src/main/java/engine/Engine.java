package engine;


public abstract class Engine{
    public static Engine getInstance(){
        System.err.println("Implement Engine GetInstance");
        return null;
    }
    public abstract void onShutdown();

}
