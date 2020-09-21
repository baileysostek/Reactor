package engine;

import camera.CameraManager;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import editor.Editor;
import entity.Entity;
import entity.EntityManager;
import entity.EntityUtils;
import graphics.renderer.*;
import graphics.sprite.Colors;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import input.MousePicker;
import lighting.DirectionalLight;
import lighting.LightingManager;
import logging.LogManager;
import models.AABB;
import models.Model;
import models.ModelManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import physics.PhysicsEngine;
import platform.EnumDevelopment;
import platform.EnumPlatform;
import platform.PlatformManager;
import scene.SceneManager;
import scripting.ScriptingEngine;
import sound.SoundEngine;
import util.StringUtils;

import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class FraudTek {

    private static Window WINDOW;
    public static long WINDOW_POINTER;

    //Engine Variables
    private static boolean INITIALIZED = false;
    private static boolean RUNNING = false;

    //Global Variables
    private static int     WIDTH  = 900;
    private static int     HEIGHT = 900;
    private static String  TITLE  = "Reactor V1.0";
    private static boolean VSYNC  = false;

    //Local Static variables
    private static int     FRAMES = 0;

    public static DirectionalLight sun;

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
        //Welcome to Reactor!
        System.out.println( "_________                       __\n" +
                            "\\______   \\ ____ _____    _____/  |_  ___________ \n" +
                            " |       _// __ \\\\__  \\ _/ ___\\   __\\/  _ \\_  __ \\\n" +
                            " |    |   \\  ___/ / __ \\\\  \\___|  | (  <_> )  | \\/\n" +
                            " |____|_  /\\___  >____  /\\___  >__|  \\____/|__|   \n" +
                            "        \\/     \\/     \\/     \\/                   "
        );


        if(!INITIALIZED) {
            INITIALIZED = true;

            PlatformManager platformManager = PlatformManager.setTargetPlatform(EnumPlatform.WINDOWS);
            System.out.println("Initializing...");

            if (platformManager.targetIs(EnumPlatform.WINDOWS)) {
                GLFWErrorCallback.createPrint(System.err).set();

                System.out.print("Try init GLFW...");
                if (!glfwInit()) {
                    System.out.println("failed.");
                    throw new IllegalStateException("Unable to initialize GLFW");
                }
                System.out.println("success.");

                //Try to init steam
                try {
                    System.out.print("Try init Steamworks...");
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
                    System.out.println("sucess.");
                } catch (SteamException e) {
                    System.out.println("failed.");
                    // Error extracting or loading native libraries
                    e.printStackTrace();
                }

                //Set the size to take up the screen.
                long primaryMonitorPointer = glfwGetPrimaryMonitor();
                WIDTH   = glfwGetVideoMode(primaryMonitorPointer).width();
                HEIGHT  = glfwGetVideoMode(primaryMonitorPointer).height()-64;

                //Creating our window.
                System.out.print("Creating Window:["+WIDTH+" x "+HEIGHT+"] Title:"+TITLE+" Vsync:"+VSYNC);
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
                PhysicsEngine.initialize();
                LightingManager.initialize();

                //Add our initialized instances to our ScriptingManager
                ScriptingEngine.initialize();

                //If we are in developent mode init the console.
                if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
                    Editor.initialize();
                }


                Renderer.getInstance();

                sun = new DirectionalLight();
                Entity drag = new Entity();
                drag.setModel(ModelManager.getInstance().loadModel("Garden.obj").getFirst());
                drag.setTexture(SpriteBinder.getInstance().load("Garden_BaseColor.png"));
                EntityManager.getInstance().addEntity(drag);
                EntityManager.getInstance().addEntity(sun);
                EntityManager.getInstance().addEntity(new DirectionalLight());
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
        CameraManager.getInstance().update(delta);
        MousePicker.getInstance().tick(delta);
        SceneManager.getInstance().update(delta);
        LightingManager.getInstance().update(delta);
        EntityManager.getInstance().update(delta);

//        Vector3f pos = MousePicker.rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), MousePicker.getInstance().getRay(), new Vector3f(0), new Vector3f(0, 1, 0));
//        if(pos == null){
//            pos = new Vector3f(0);
//        }else{
////            pos = pos.add(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).mul(1, 0, 0));
//        }
//        test1.setPosition(pos);

        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().update(delta);
        }else{
            PhysicsEngine.getInstance().update(delta);
        }

    }

    private static void render(){
        //Render Shadow maps
//        LightingManager.getInstance().render();

        //Render World.
        Renderer.getInstance().render();

        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            //Draw Axis
            int size = 4096;
            Renderer.getInstance().drawLine(new Vector3f(-size, 0, 0), new Vector3f(size, 0, 0), new Vector3f(1, 0, 0));
            Renderer.getInstance().drawLine(new Vector3f(0, -size, 0), new Vector3f(0, size, 0), new Vector3f(0, 1, 0));
            Renderer.getInstance().drawLine(new Vector3f(0, 0, -size), new Vector3f(0, 0, size), new Vector3f(0, 0, 1));

            //Draw arrows point in +Axis direction.
            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(1, 0, 0));
            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(0, 1, 0));
            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(0, 0, 1));

            //Draw Grid
            size = 50;
            for(int i = -size; i <= size; i++){
                if(i == 0){
                    i++;
                }
                Vector3f color = new Vector3f(0.5f);
                if(i % 10 == 0){
                    color.add(0.5f , 0.5f, 0.5f);
                }
                Renderer.getInstance().drawLine(new Vector3f(-size, 0, i), new Vector3f(size, 0, i), color);
                Renderer.getInstance().drawLine(new Vector3f(i, 0, -size), new Vector3f(i, 0, size), color);
            }

            //Draw a Ring
//            Renderer.getInstance().drawRing(new Vector3f(0), new Vector2f(1), 6, new Vector3f(1, 1, 0));

//            Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
//            if(pos != null) {
//                DirectDrawData ddd = Renderer.getInstance().drawArrow(new Vector3f(5, 5, 5), new Vector3f(pos), new Vector3f(0.5f, 0.5f, 1.25f), 13, new Vector3f(1, 1, 1));
//                Renderer.getInstance().drawAABB(ddd.getAABB());
//                if(MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd.getAABB()) != null){
//                    Renderer.getInstance().redrawTriangleColor(ddd, new Vector3f(1, 0, 0));
//                }
//            }

        }

        SceneManager.getInstance().render();

        //If Dev render UI
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().preUIRender();
        }

        PhysicsEngine.getInstance().render();
        Renderer.getInstance().postpare();

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
                        Model model = ModelManager.getInstance().loadModel(action.get("model").getAsString()).getFirst();
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

//        String modelName = "Garden";
//////
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
//        test1 = new Entity().setModel(ModelManager.getInstance().loadModel(modelName+".tek")).setPosition(new Vector3f(0, 0, 0));
//        test1.setTexture(SpriteBinder.getInstance().load("garbo"));
//        EntityManager.getInstance().addEntity(test1);
//        test1.setScale(new Vector3f(0.25f));

//        LinkedList<Entity> spheres = new LinkedList<>();
//
//        for(int i = 0; i < 100; i++){
//            spheres.addLast(new Entity().setModel(ModelManager.getInstance().loadModel("sphere_smooth.tek")).setPosition(new Vector3f((float) (100 * Math.random()), (float) (100 * Math.random()), (float) (100 * Math.random()))));
//        }
//
//        Entity group = EntityUtils.group(spheres);
//        group.setPosition(new Vector3f(0));
//        group.setScale(new Vector3f(1));
//        group.setTexture(SpriteBinder.getInstance().getFileNotFoundID());
//        EntityManager.getInstance().addEntity(group);


//        Entity test2 = new Entity().setModel(ModelManager.getInstance().loadModel(modelName+".tek")).setPosition(new Vector3f( 4, 4, -2));
//        EntityManager.getInstance().addEntity(test);
//
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
