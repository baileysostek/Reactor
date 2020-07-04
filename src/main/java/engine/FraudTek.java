package engine;

import camera.CameraManager;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import editor.Editor;
import editor.components.container.Image;
import editor.components.views.LevelEditor;
import entity.Entity;
import entity.EntityEditor;
import entity.EntityManager;
import entity.component.Interactable;
import graphics.renderer.*;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import input.MousePicker;
import logging.LogManager;
import models.Model;
import models.ModelManager;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL20;
import platform.EnumDevelopment;
import platform.EnumPlatform;
import platform.PlatformManager;
import scene.SceneManager;
import scripting.ScriptingEngine;
import serialization.SerializationHelper;
import sound.SoundEngine;
import util.StringUtils;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBEasyFont.*;

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

    public static void initialize() {
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
                    String libraryPath = StringUtils.getRelativePath() + "/libs/steam";
                    libraryPath = libraryPath.replace("\\resources\\", "");
                    System.out.println("Lib Path:"+libraryPath);
                    SteamAPI.loadLibraries(libraryPath);
                    System.out.println(SteamAPI.isSteamRunning());
                    if (!SteamAPI.init()) {
                        // Steamworks initialization error, e.g. Steam client not running
                        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
                            SteamAPI.printDebugInfo(System.out);
                        }
                    }else{
                        System.out.println("Steam Running:"+SteamAPI.isSteamRunning());
                    }
                } catch (SteamException e) {
                    // Error extracting or loading native libraries
                    e.printStackTrace();
                }

                WINDOW = new Window(WIDTH, HEIGHT).setTitle(TITLE);
                WINDOW.setVsyncEnabled(VSYNC);
                try {
                    WINDOW.setWindowIcon("/textures/logo.png");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                WINDOW_POINTER = WINDOW.getWindow_p();

                //We have a GL context after this call
                SpriteManager.initialize();

                //We can load shaders
                ShaderManager.initialize();

                //We have a known window size, a shader, and a GL context, we can make a window.
                Renderer.initialize(WIDTH, HEIGHT);

                LogManager.initialize();
                ModelManager.initialize();
                CameraManager.initialize();
                EntityManager.initialize();
                SceneManager.initialize();
                SoundEngine.initialize();

                //Add our initialized instances to our ScriptingManager
                ScriptingEngine.initialize();

                //If we are in developent mode init the console.
                if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
                    Editor.initialize();
                }


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
//        ChromaManager.getInstance().tick(delta);
        //Update all engine components, this gives the editors widgets time to update
        MousePicker.getInstance().tick(delta);
        SceneManager.getInstance().update(delta);
        EntityManager.getInstance().update(delta);

        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().update(delta);
        }

    }

    private static void render(){
        //Render World.
        Renderer.getInstance().render();

        //If Dev render UI
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().render();
        }
    }

    private static void shutdown(){
        Renderer.getInstance().onShutdown();
        SpriteBinder.getInstance().onShutdown();
        SoundEngine.getInstance().onShutdown();
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            Editor.getInstance().onShutdown();
        }
    }


    public static void main(String[] args){
        FraudTek.initialize();
        if(args.length > 0) {
            try {
                JsonObject action = new JsonParser().parse(args[0]).getAsJsonObject();
                //All actions this object can perform
                switch (action.get("action").getAsString()) {
                    case ("convert"): {
                        Model model = ModelManager.getInstance().loadModel(action.get("model").getAsString());
                        JsonObject saveData = model.serialize();
                        System.out.println("Writing file to:" + "/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
                        StringUtils.write(saveData.toString(), "/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
                        break;
                    }
                }

                //Other items that can be in this initialization object.

            }catch(JsonParseException e){
                System.out.println("FraudTek Initialization Error: passed argument at position [0] is not a valid JSON object.");
            }
        }


        //Scripting Test
//        Script test = ScriptingEngine.getInstance().loadScript("MapGenerator/Main.js");
//        Script test = ScriptingEngine.getInstance().loadScript("Recursion.js");
//        test.getTokensInFile();
//        ScriptingEngine.getInstance().run(test, "fibonacci");

//        String modelName = "quad";
////
//        JsonObject action = new JsonObject();
//        action.addProperty("action", "convert");
//        action.addProperty("model", modelName+".obj");
//        switch (action.get("action").getAsString()) {
//            case ("convert"): {
//                Model model = ModelManager.getInstance().loadModel(action.get("model").getAsString());
//                JsonObject saveData = model.serialize();
//                System.out.println("Writing file to:"+"/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
//                StringUtils.write(saveData.toString(), "/models/" + action.get("model").getAsString().replace(".obj", ".tek"));
//            }
//        }
//
//        Entity test1 = new Entity().setModel(ModelManager.getInstance().loadModel(modelName+".tek")).setPosition(new Vector3f(-4, 4, -2));
//        Entity test2 = new Entity().setModel(ModelManager.getInstance().loadModel(modelName+".tek")).setPosition(new Vector3f( 4, 4, -2));
//        EntityManager.getInstance().addEntity(test);
//
//        Entity drag = new Entity();
//        drag.setModel(ModelManager.getInstance().loadModel("quad.tek"));
//        drag.setPosition(new Vector3f(1.1f, 1.2f, 1.3f));
//        Interactable interactable = new Interactable();
//        drag.addComponent(interactable);
//        drag.getAttribute("interactable_range").setData(13.5);
//        interactable.getAttribute("interactable_range").setData(94.2);
//
//        JsonObject savetest = drag.serialize();
//
//        StringUtils.write(savetest.toString(), "/entities/test.tek");
//
//        Entity rebuilt = new Entity(savetest);
//
//        System.out.println(rebuilt);


//        EntityManager.getInstance().addEntity(drag);
//
//        Editor.getInstance().addComponent(new LevelEditor(SpriteBinder.getInstance().loadSheet("Tileset.png", 16, 16)));

//        Sprite test = SpriteBinder.getInstance().load("fileNotFound.png");
//        Editor.getInstance().addComponent(new Image(SpriteBinder.getInstance().load("thisfiledoesnotexist.png").getTextureID()));
//
//        System.out.println(test.serialize());

//
//        test.setPixelColor(0, 0, new Vector4f(1, 0, 0, 1));
//        test.setPixelColor(1, 0, new Vector4f(0, 1, 0, 1));
//        test.setPixelColor(2, 0, new Vector4f(0, 0, 1, 1));
//        test.setPixelColor(3, 0, new Vector4f(1, 1, 1, 1));
//
//        test.drawLine( 1, 1, 18, 26,  new Vector4f(1, 1, 0, 1));
//
//        test.drawSquare( 18, 7, 7, 9,  new Vector4f(0, 0.5f, 0, 1));
//
//        test.drawCircle( 6, 23, 17,  new Vector4f(0.75f, 0.3f, .66f, 1));
//
//        test.flush();
//
//        Editor.getInstance().addComponent(new Image(test.getTextureID()));

//        Editor.getInstance().addComponent(new EntityEditor());



        FraudTek.run();
        FraudTek.shutdown();

    }

}
