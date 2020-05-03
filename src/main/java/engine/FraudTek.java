package engine;

import camera.CameraManager;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xpath.internal.operations.Mod;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.*;
import graphics.sprite.SpriteBinder;
import input.Chroma.ChromaManager;
import input.MousePicker;
import models.Model;
import models.ModelManager;
import org.lwjgl.glfw.GLFWErrorCallback;
import platform.EnumDevelopment;
import platform.EnumPlatform;
import platform.PlatformManager;
import scene.SceneManager;
import scripting.Script;
import scripting.ScriptingEngine;
import util.Async;
import util.Callback;
import util.StringUtils;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class FraudTek {

    private static Window WINDOW;
    public static long WINDOW_POINTER;

    //Engine Variables
    private static boolean INITIALIZED = false;
    private static boolean RUNNING = false;

    //Global Variables
    private static int     WIDTH  = 900;
    private static int     HEIGHT = 900;
    private static String  TITLE  = "FraudTek V1.0";
    private static boolean VSYNC  = false;

    //Local Static variables
    private static int     FRAMES = 0;

    //Interface Methods
    public static void setWindowSize(int width, int height){
        if(!INITIALIZED) {
            WIDTH = width;
            HEIGHT = height;
        }else{
            System.out.println("Error: Setting window size after window is created.");
        }
    }

    public static void setVsyncEnabled(boolean vsync){
        if(!INITIALIZED) {
            VSYNC = vsync;
        }else{
            WINDOW.setVsyncEnabled(VSYNC);
        }
    }

    public static void setWindowTitle(String title){
        if(!INITIALIZED) {
            TITLE = title;
        }else{
            WINDOW.setTitle(TITLE);
        }
    }

    public static void setDevelopmentLevel(EnumDevelopment dev){
        PlatformManager.getInstance().setDevelopmentLevel(dev);
    }

    //Access these varaibels
    public static int getFPS(){
        return FRAMES;
    }

    public static void init() {
        if(!INITIALIZED) {
            INITIALIZED = true;

            PlatformManager platformManager = PlatformManager.setTargetPlatform(EnumPlatform.WINDOWS);

            if (platformManager.targetIs(EnumPlatform.WINDOWS)) {
                GLFWErrorCallback.createPrint(System.err).set();

                if (!glfwInit()) {
                    throw new IllegalStateException("Unable to initialize GLFW");
                }

                //Try to init steam
                try {
                    SteamAPI.loadLibraries("./libs/steam");
                    System.out.println(SteamAPI.isSteamRunning());
                    if (!SteamAPI.init()) {
                        // Steamworks initialization error, e.g. Steam client not running
                        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
                            SteamAPI.printDebugInfo(System.out);
                        }
                    }
                } catch (SteamException e) {
                    // Error extracting or loading native libraries
                    e.printStackTrace();
                }

                ChromaManager.getInstance();

                WINDOW = new Window(WIDTH, HEIGHT).setTitle(TITLE);
                WINDOW.setVsyncEnabled(VSYNC);
                WINDOW_POINTER = WINDOW.getWindow_p();

                //We have a GL context after this call
                SpriteManager.initialize();

                //We can load shaders
                ShaderManager.initialize();

                //We have a known window size, a shader, and a GL context, we can make a window.
                Renderer.initialize(WIDTH, HEIGHT);


                //Initialize all of our modules
                VAOManager.initialize();
                ModelManager.initialize();
                CameraManager.initialize();
                EntityManager.initialize();
                SceneManager.initialize();

                //Add our initialized instances to our ScriptingManager
                ScriptingEngine.initialize();


                Renderer.getInstance();
            }
        }
    }

    public static void run(){
        if(!RUNNING){

            RUNNING = true;

            double last = System.nanoTime();
            double runningDelta = 0;
            double frameDelta = 0;
            int frames = 0;

            while (!glfwWindowShouldClose(WINDOW_POINTER) && RUNNING) {
                double now = System.nanoTime();
                frameDelta = ((now - last) / (double) 1000000000);
                runningDelta += frameDelta;

                tick(frameDelta);
                if (runningDelta > 1) {
                    System.out.println("FPS:" + frames + " Entities:" + EntityManager.getInstance().getSize());
                    FRAMES = frames;
                    frames = 0;
                    runningDelta -= 1;
                }

                render();

                glfwSwapBuffers(WINDOW_POINTER); // swap the color buffer //Actual render call
                glfwPollEvents();
                frames++;


                last = now;
            }


            //Close all managers that we have instantiated
            shutdown();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(WINDOW_POINTER);
            glfwDestroyWindow(WINDOW_POINTER);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();

        }
    }

    public static void exit(){
        if(RUNNING){
            RUNNING = false;
        }
    }

    private static void tick(double delta){//Scalar to multiply positions by
        ChromaManager.getInstance().tick(delta);
        MousePicker.getInstance().tick(delta);
        SceneManager.getInstance().update(delta);
        EntityManager.getInstance().update(delta);
    }

    private static void render(){
        Renderer.getInstance().render();
    }

    private static void shutdown(){
        Renderer.getInstance().onShutdown();
        SpriteBinder.getInstance().onShutdown();
        ChromaManager.getInstance().onShutdown();
    }


    public static void main(String[] args){
        FraudTek.init();
        if(args.length > 0) {
            JsonObject action = new JsonParser().parse(args[0]).getAsJsonObject();
            switch (action.get("action").getAsString()) {
                case ("convert"): {
                    Model model = ModelManager.getInstance().loadModel(action.get("model").getAsString());
                    JsonObject saveData = model.serialize();
                    System.out.println("Writing file to:"+"/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
                    StringUtils.write(saveData.toString(), "/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
                }
            }
        }
//
//        JsonObject action = new JsonObject();
//        action.addProperty("action", "convert");
//        action.addProperty("model", "untitled.obj");
//        switch (action.get("action").getAsString()) {
//            case ("convert"): {
//                Model model = ModelManager.getInstance().loadModel(action.get("model").getAsString());
//                JsonObject saveData = model.serialize();
//                System.out.println("Writing file to:"+"/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
//                StringUtils.write(saveData.toString(), "/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
//            }
//        }
//
//
//        Entity test = new Entity().setModel(ModelManager.getInstance().loadModel("untitled.tek")).setPosition(0, 4, -2);
//        EntityManager.getInstance().addEntity(test);
//        Script test = ScriptingEngine.getInstance().loadScript("MapGenerator/Main.js");
        Script test = ScriptingEngine.getInstance().loadScript("Recursion.js");
        test.getTokensInFile();
        ScriptingEngine.getInstance().run(test, "fibonacci");

        FraudTek.run();
        FraudTek.shutdown();

    }

}
