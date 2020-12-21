package input.Chroma;

import engine.Engine;
import org.jglr.jchroma.JChroma;
import org.jglr.jchroma.effects.CustomKeyboardEffect;
import org.jglr.jchroma.utils.ColorRef;
import org.jglr.jchroma.utils.KeyboardKeys;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ChromaManager extends Engine implements Runnable{

    private static ChromaManager chromaManager;
    private static JChroma chroma;
    private static CustomKeyboardEffect effect;

    private Thread thread;
    private boolean running = false;
    private static boolean redraw  = true;

    //Mapping GLFW KEYS to RAZER keys
    private HashMap<Integer, Integer> keyMapping = new HashMap<>();

    //Desired colors to set keys to
    private static ConcurrentHashMap<Integer, KeyColorDuration> keyColorsAdd = new ConcurrentHashMap<>();
    private static HashMap<Integer, KeyColorDuration> keyColors = new HashMap<>();

    private boolean isInitialized = false;

    private ChromaManager(){

        try{
            chroma = JChroma.getInstance();
            chroma.init();
        }catch(UnsatisfiedLinkError noLink){
            isInitialized = false;
            System.out.println("Error: No razor SDK found on this computer, there are probably no Razer devices attached.");
            return;
        }

        //Init chroma effect
        try {
            effect = new CustomKeyboardEffect();
//            setBackgroundColor(255, 0, 0);
            chroma.createKeyboardEffect(effect);
            //For some reason this is needed..?.
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Key mapping
        //Letter keys
        keyMapping.put(GLFW.GLFW_KEY_A, KeyboardKeys.RZKEY_A);
        keyMapping.put(GLFW.GLFW_KEY_B, KeyboardKeys.RZKEY_B);
        keyMapping.put(GLFW.GLFW_KEY_C, KeyboardKeys.RZKEY_C);
        keyMapping.put(GLFW.GLFW_KEY_D, KeyboardKeys.RZKEY_D);
        keyMapping.put(GLFW.GLFW_KEY_E, KeyboardKeys.RZKEY_E);
        keyMapping.put(GLFW.GLFW_KEY_F, KeyboardKeys.RZKEY_F);
        keyMapping.put(GLFW.GLFW_KEY_G, KeyboardKeys.RZKEY_G);
        keyMapping.put(GLFW.GLFW_KEY_H, KeyboardKeys.RZKEY_H);
        keyMapping.put(GLFW.GLFW_KEY_I, KeyboardKeys.RZKEY_I);
        keyMapping.put(GLFW.GLFW_KEY_J, KeyboardKeys.RZKEY_J);
        keyMapping.put(GLFW.GLFW_KEY_K, KeyboardKeys.RZKEY_K);
        keyMapping.put(GLFW.GLFW_KEY_L, KeyboardKeys.RZKEY_L);
        keyMapping.put(GLFW.GLFW_KEY_M, KeyboardKeys.RZKEY_M);
        keyMapping.put(GLFW.GLFW_KEY_N, KeyboardKeys.RZKEY_N);
        keyMapping.put(GLFW.GLFW_KEY_O, KeyboardKeys.RZKEY_O);
        keyMapping.put(GLFW.GLFW_KEY_P, KeyboardKeys.RZKEY_P);
        keyMapping.put(GLFW.GLFW_KEY_Q, KeyboardKeys.RZKEY_Q);
        keyMapping.put(GLFW.GLFW_KEY_R, KeyboardKeys.RZKEY_R);
        keyMapping.put(GLFW.GLFW_KEY_S, KeyboardKeys.RZKEY_S);
        keyMapping.put(GLFW.GLFW_KEY_T, KeyboardKeys.RZKEY_T);
        keyMapping.put(GLFW.GLFW_KEY_U, KeyboardKeys.RZKEY_U);
        keyMapping.put(GLFW.GLFW_KEY_V, KeyboardKeys.RZKEY_V);
        keyMapping.put(GLFW.GLFW_KEY_W, KeyboardKeys.RZKEY_W);
        keyMapping.put(GLFW.GLFW_KEY_X, KeyboardKeys.RZKEY_X);
        keyMapping.put(GLFW.GLFW_KEY_Y, KeyboardKeys.RZKEY_Y);
        keyMapping.put(GLFW.GLFW_KEY_Z, KeyboardKeys.RZKEY_Z);

        //Numbers
        keyMapping.put(GLFW.GLFW_KEY_1, KeyboardKeys.RZKEY_1);
        keyMapping.put(GLFW.GLFW_KEY_2, KeyboardKeys.RZKEY_2);
        keyMapping.put(GLFW.GLFW_KEY_3, KeyboardKeys.RZKEY_3);
        keyMapping.put(GLFW.GLFW_KEY_4, KeyboardKeys.RZKEY_4);
        keyMapping.put(GLFW.GLFW_KEY_5, KeyboardKeys.RZKEY_5);
        keyMapping.put(GLFW.GLFW_KEY_6, KeyboardKeys.RZKEY_6);
        keyMapping.put(GLFW.GLFW_KEY_7, KeyboardKeys.RZKEY_7);
        keyMapping.put(GLFW.GLFW_KEY_8, KeyboardKeys.RZKEY_8);
        keyMapping.put(GLFW.GLFW_KEY_9, KeyboardKeys.RZKEY_9);
        keyMapping.put(GLFW.GLFW_KEY_0, KeyboardKeys.RZKEY_0);

        //Numpad
        keyMapping.put(GLFW.GLFW_KEY_KP_0, KeyboardKeys.RZKEY_NUMPAD0);
        keyMapping.put(GLFW.GLFW_KEY_KP_1, KeyboardKeys.RZKEY_NUMPAD1);
        keyMapping.put(GLFW.GLFW_KEY_KP_2, KeyboardKeys.RZKEY_NUMPAD2);
        keyMapping.put(GLFW.GLFW_KEY_KP_3, KeyboardKeys.RZKEY_NUMPAD3);
        keyMapping.put(GLFW.GLFW_KEY_KP_4, KeyboardKeys.RZKEY_NUMPAD4);
        keyMapping.put(GLFW.GLFW_KEY_KP_5, KeyboardKeys.RZKEY_NUMPAD5);
        keyMapping.put(GLFW.GLFW_KEY_KP_6, KeyboardKeys.RZKEY_NUMPAD6);
        keyMapping.put(GLFW.GLFW_KEY_KP_7, KeyboardKeys.RZKEY_NUMPAD7);
        keyMapping.put(GLFW.GLFW_KEY_KP_8, KeyboardKeys.RZKEY_NUMPAD8);
        keyMapping.put(GLFW.GLFW_KEY_KP_9, KeyboardKeys.RZKEY_NUMPAD9);
        keyMapping.put(GLFW.GLFW_KEY_KP_DECIMAL, KeyboardKeys.RZKEY_NUMPAD_DECIMAL);
        keyMapping.put(GLFW.GLFW_KEY_KP_ENTER, KeyboardKeys.RZKEY_NUMPAD_ENTER);
        keyMapping.put(GLFW.GLFW_KEY_KP_ADD, KeyboardKeys.RZKEY_NUMPAD_ADD);
        keyMapping.put(GLFW.GLFW_KEY_KP_SUBTRACT, KeyboardKeys.RZKEY_NUMPAD_SUBTRACT);
        keyMapping.put(GLFW.GLFW_KEY_KP_MULTIPLY, KeyboardKeys.RZKEY_NUMPAD_MULTIPLY);
        keyMapping.put(GLFW.GLFW_KEY_KP_DIVIDE, KeyboardKeys.RZKEY_NUMPAD_DIVIDE);
        keyMapping.put(GLFW.GLFW_KEY_NUM_LOCK, KeyboardKeys.RZKEY_NUMLOCK);


        //Special keys
        keyMapping.put(GLFW.GLFW_KEY_ESCAPE, KeyboardKeys.RZKEY_ESC);
        keyMapping.put(GLFW.GLFW_KEY_SPACE, KeyboardKeys.RZKEY_SPACE);
        keyMapping.put(GLFW.GLFW_KEY_LEFT_ALT, KeyboardKeys.RZKEY_LALT);
        keyMapping.put(GLFW.GLFW_KEY_RIGHT_ALT, KeyboardKeys.RZKEY_RALT);
        keyMapping.put(GLFW.GLFW_KEY_LEFT_SHIFT, KeyboardKeys.RZKEY_LSHIFT);
        keyMapping.put(GLFW.GLFW_KEY_RIGHT_SHIFT, KeyboardKeys.RZKEY_RSHIFT);
        keyMapping.put(GLFW.GLFW_KEY_LEFT_CONTROL, KeyboardKeys.RZKEY_LCTRL);
        keyMapping.put(GLFW.GLFW_KEY_RIGHT_CONTROL, KeyboardKeys.RZKEY_RCTRL);
        keyMapping.put(GLFW.GLFW_KEY_CAPS_LOCK, KeyboardKeys.RZKEY_CAPSLOCK);
        keyMapping.put(GLFW.GLFW_KEY_TAB, KeyboardKeys.RZKEY_TAB);
//        keyMapping.put(GLFW.GLFW_KEY_GRAVE_ACCENT, KeyboardKeys.);

        //Function Keys
        keyMapping.put(GLFW.GLFW_KEY_F1, KeyboardKeys.RZKEY_F1);
        keyMapping.put(GLFW.GLFW_KEY_F2, KeyboardKeys.RZKEY_F2);
        keyMapping.put(GLFW.GLFW_KEY_F3, KeyboardKeys.RZKEY_F3);
        keyMapping.put(GLFW.GLFW_KEY_F4, KeyboardKeys.RZKEY_F4);
        keyMapping.put(GLFW.GLFW_KEY_F5, KeyboardKeys.RZKEY_F5);
        keyMapping.put(GLFW.GLFW_KEY_F6, KeyboardKeys.RZKEY_F6);
        keyMapping.put(GLFW.GLFW_KEY_F7, KeyboardKeys.RZKEY_F7);
        keyMapping.put(GLFW.GLFW_KEY_F8, KeyboardKeys.RZKEY_F8);
        keyMapping.put(GLFW.GLFW_KEY_F9, KeyboardKeys.RZKEY_F9);
        keyMapping.put(GLFW.GLFW_KEY_F10, KeyboardKeys.RZKEY_F10);
        keyMapping.put(GLFW.GLFW_KEY_F11, KeyboardKeys.RZKEY_F11);
        keyMapping.put(GLFW.GLFW_KEY_F12, KeyboardKeys.RZKEY_F12);

        running = true;
        thread = new Thread(this);
        thread.start();

        isInitialized = true;
    }

    public static ChromaManager getInstance(){
        if(chromaManager == null){
            chromaManager = new ChromaManager();
        }
        return chromaManager;
    }

    public void tick(double delta){
        if(!isInitialized){
            return;
        }
        for(KeyColorDuration color : keyColors.values()){
            color.tick(delta);
        }
    }

    //Color functions
    public void setBackgroundColor(int r, int g, int b){
        if(!isInitialized){
            return;
        }

        ChromaManager.redraw = true;
        //Init colors
        ColorRef[][] colors = new ColorRef[6][22];
        ColorRef ref = new ColorRef(r, g, b);
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[0].length; j++) {
                colors[i][j] = ref;
            }
        }

        effect = new CustomKeyboardEffect(colors);
    }

    public void setKeyColor(int KEY, int r, int g, int b){
        if(!isInitialized){
            return;
        }

        if(keyMapping.containsKey(KEY)) {
            ChromaManager.redraw = true;
            keyColorsAdd.put(keyMapping.get(KEY), new KeyColorDuration(keyMapping.get(KEY), r, g, b));
        }
    }

    //Rendering
    private void drawKeys(){
        //Synch arrays

        Iterator<Integer> it = keyColorsAdd.keySet().iterator();
        while(it.hasNext()) {
            Integer key = it.next();
            KeyColorDuration value = keyColorsAdd.get(key);
            keyColors.put(key,value);
            keyColorsAdd.remove(key);
        }

        //Draw newly synced map
        for(KeyColorDuration color : keyColors.values()){
            effect.setKeyColor(color.key, color.color);
        }
    }

    @Override
    public void onShutdown() {
        running = false;
        if(chroma != null){
            try{
                chroma.free();
            }catch(org.jglr.jchroma.JChromaException e){
                //Suppress error
            }
        }
    }

    @Override
    public void run() {
        while(running){
            if(ChromaManager.redraw) {
                drawKeys();
                chroma.createKeyboardEffect(effect);
//                ChromaManager.redraw = false;
                try {
                    //For some reason this is needed..?.
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    //For some reason this is needed..?.
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class KeyColorDuration{

    int key;
    int r;
    int g;
    int b;
    float duration    = 8.0f;
    float runningTime = 0f;
    KeyEffect effect;

    ColorRef color;

    boolean remove = false;

    protected KeyColorDuration(int key, int r, int g, int b){
        this.key = key;
        this.r = r;
        this.g = g;
        this.b = b;

        effect = KeyEffect.STATIC;

        color = new ColorRef(r, g, b);
    }

    public KeyColorDuration setDuration(float duration){
        this.duration = duration;
        return this;
    }

    public void tick(double delta){
        runningTime += (double)(delta / duration);
        runningTime = Math.min(runningTime, duration);
        color = ColorUtils.lerp(color, new ColorRef(0,255,0), runningTime / duration);
        if(runningTime == duration){
            remove = true;
        }
    }
}


enum KeyEffect{
    STATIC(),
    REACTIVE(),
    FADE_BETWEEN(),
    FADE_TO(),
}