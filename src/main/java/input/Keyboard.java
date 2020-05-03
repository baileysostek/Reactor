package input;

import engine.Engine;
import engine.FraudTek;
import input.Chroma.ChromaManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import util.Callback;

import java.util.LinkedList;

public class Keyboard extends Engine {

    private static Keyboard keyboard;
    private boolean[] keys = new boolean[1024]; // No out of bounds
    private LinkedList<Callback>[] pressedCallbacks  = new LinkedList[keys.length];
    private LinkedList<Callback>[] releasedCallbacks = new LinkedList[keys.length];

    //Key List


    private Keyboard(){
        super();
        //Array init
        for(int i = 0; i < keys.length; i++){
            keys[i] = false;
            pressedCallbacks[i] = new LinkedList<Callback>();
            releasedCallbacks[i] = new LinkedList<Callback>();
        }
        //Callback stuff
        GLFW.glfwSetKeyCallback(FraudTek.WINDOW_POINTER, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                System.out.println(key);
            if(key < keys.length && key >= 0){
                if(action == GLFW.GLFW_RELEASE){
                    keys[key] = false;
                    for(Callback c : releasedCallbacks[key]){
                        c.callback();
                    }
                }else{
                    keys[key] = true;
                    ChromaManager.getInstance().setKeyColor(key, 0, 0, 255);
                    for(Callback c : pressedCallbacks[key]){
                        c.callback();
                    }
                }
            }
            }
        });

    }

    @Override
    public void onShutdown() {

    }

    public static Keyboard getInstance(){
        if(keyboard == null){
            keyboard = new Keyboard();
        }
        return keyboard;
    }

    public void addPressCallback(int key, Callback callback){
        this.pressedCallbacks[key].add(callback);
    }

    public boolean isKeyPressed(int key){
        return keys[key];
    }

    public boolean isKeyPressed(float key){
        int iKey = (int)key;
        return keys[iKey];
    }
}
