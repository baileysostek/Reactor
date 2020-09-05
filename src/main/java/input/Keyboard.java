package input;

import engine.Engine;
import engine.FraudTek;
import input.Chroma.ChromaManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import util.Callback;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;

public class Keyboard extends Engine {

    private static Keyboard keyboard;
    private boolean[] keys = new boolean[1024]; // No out of bounds
    private LinkedList<Callback>[] pressedCallbacks  = new LinkedList[keys.length];
    private LinkedList<Callback>[] releasedCallbacks = new LinkedList[keys.length];

    //Hahsing to store where a callback lives
    private HashMap<Callback, Integer> keyLookup = new HashMap<>();

    //Key List
    public static final int A = KeyEvent.VK_A;
    public static final int B = KeyEvent.VK_B;
    public static final int C = KeyEvent.VK_C;
    public static final int D = KeyEvent.VK_D;
    public static final int E = KeyEvent.VK_E;
    public static final int F = KeyEvent.VK_F;
    public static final int G = KeyEvent.VK_G;
    public static final int H = KeyEvent.VK_H;
    public static final int I = KeyEvent.VK_I;
    public static final int J = KeyEvent.VK_J;
    public static final int K = KeyEvent.VK_K;
    public static final int L = KeyEvent.VK_L;
    public static final int M = KeyEvent.VK_M;
    public static final int N = KeyEvent.VK_N;
    public static final int O = KeyEvent.VK_O;
    public static final int P = KeyEvent.VK_P;
    public static final int Q = KeyEvent.VK_Q;
    public static final int R = KeyEvent.VK_R;
    public static final int S = KeyEvent.VK_S;
    public static final int T = KeyEvent.VK_T;
    public static final int U = KeyEvent.VK_U;
    public static final int V = KeyEvent.VK_V;
    public static final int W = KeyEvent.VK_W;
    public static final int X = KeyEvent.VK_X;
    public static final int Y = KeyEvent.VK_Y;
    public static final int Z = KeyEvent.VK_Z;


    //SPECIALTY
    public static final int ESCAPE    = 0x100;
    public static final int DELETE    = 261;
    public static final int BACKSPACE = 259;
    public static final int SPACE     = KeyEvent.VK_SPACE;

    public static final int CONTROL   = KeyEvent.VK_CONTROL;


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
//                System.out.println(key);
            if(key < keys.length && key >= 0){
                if(action == GLFW.GLFW_RELEASE){
                    keys[key] = false;
                    for(Callback c : releasedCallbacks[key]){
                        c.callback();
                    }
                }else{
                    keys[key] = true;
//                    ChromaManager.getInstance().setKeyColor(key, 0, 0, 255);
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
        //index where this callback lives
        keyLookup.put(callback, key);
        //Set the callback
        this.pressedCallbacks[key].add(callback);
    }

    public void addReleaseCallback(int key, Callback callback){
        //index where this callback lives
        keyLookup.put(callback, key);
        //Set the callback
        this.releasedCallbacks[key].add(callback);
    }

    public void switchCallbackKey(Callback callback, int key) {
        //If no callback do nothing
        if(callback == null){
            return;
        }

        //Check if we have the callback
        if(keyLookup.containsKey(callback)){
            //We have it
            int index = keyLookup.get(callback);
            //Remove from where it was
            this.pressedCallbacks[index].remove(callback);

        }
        //Add it to where it should go
        addPressCallback(key, callback);
    }


    public boolean isKeyPressed(int ... keys){
        for(int a_key : keys){
            if(this.keys[a_key]){
                return true;
            }
        }
        return false;
    }

    public boolean isKeyPressed(float key){
        int iKey = (int)key;
        return keys[iKey];
    }
}
