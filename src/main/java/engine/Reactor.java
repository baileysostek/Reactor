package engine;

import camera.CameraManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import editor.Editor;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.*;
import graphics.sprite.SpriteBinder;
import input.Chroma.ChromaManager;
import input.MousePicker;
import input.controller.ControllerManager;
import lighting.LightingManager;
import logging.LogManager;
import material.Material;
import material.MaterialManager;
import models.Model;
import models.ModelManager;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import particle.ParticleManager;
import physics.PhysicsEngine;
import platform.EnumDevelopment;
import platform.EnumPlatform;
import platform.PlatformManager;
import scene.SceneManager;
import scripting.ScriptingEngine;
import skybox.SkyboxManager;
import sound.SoundEngine;
import steam.SteamManager;
import util.StopwatchManager;
import util.StringUtils;

import java.util.LinkedList;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class Reactor {

    private static Window WINDOW;
    public static long WINDOW_POINTER;
    public static long vg;

    //Engine Variables
    private static boolean INITIALIZED = false;
    private static boolean RUNNING = false;

    //Global Variables
    private static int     WIDTH        = 900;
    private static int     HEIGHT       = 900;
    private static String  TITLE        = "Reactor V1.0";
    private static boolean VSYNC        = false;
    private static boolean FULLSCREEN   = false;

    //GLOBAL flags
    private static boolean REACTOR_ENABLE_DIRECT_DRAW = false;

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

                //Set the size to take up the screen.
                long primaryMonitorPointer = glfwGetPrimaryMonitor();
                WIDTH   = glfwGetVideoMode(primaryMonitorPointer).width();
                HEIGHT  = glfwGetVideoMode(primaryMonitorPointer).height()-64;

                //Creating our window.
                System.out.println("Creating Window:["+WIDTH+" x "+HEIGHT+"] Title:"+TITLE+" Vsync:"+VSYNC);
                WINDOW = new Window(WIDTH, HEIGHT).setTitle(TITLE);
                WINDOW.setVsyncEnabled(VSYNC);
                try {
                    WINDOW.setWindowIcon("textures/logo.png");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                WINDOW_POINTER = WINDOW.getWindow_p();

                //We have a GL context after this call
                //init
                // Set the clear color
                GL.createCapabilities();

                SpriteManager.initialize();

                //We can load shaders
                ShaderManager.initialize();

                //We have a known window size, a shader, and a GL context, we can make a window.
                Renderer.initialize(WIDTH, HEIGHT);
                VAOManager.initialize();
                SkyboxManager.initialize();

                vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS);
                if (vg == NULL) {
                    throw new RuntimeException("Could not init nanovg.");
                }else{
                    System.out.println("NanoVG handle:" + vg);
                }

                LogManager.initialize();
                SteamManager.initialize();
                ModelManager.initialize();
                CameraManager.initialize();
                MaterialManager.initialize();
                ParticleManager.initialize();
                EntityManager.initialize();
                SceneManager.initialize();
                SoundEngine.initialize();
                PhysicsEngine.initialize();
                LightingManager.initialize();
                ControllerManager.initialize();

                StopwatchManager.initialize();


                //Add our initialized instances to our ScriptingManager
                ScriptingEngine.initialize();

                //If we are in developent mode init the console.
                if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
                    Editor.initialize();
                }


                Renderer.getInstance();
                DirectDraw.initialize();

                int size = 16;

                LinkedList<Entity> toAdd = new LinkedList<>();

                for(int i = 0; i < size; i++){
                    for(int j = 0; j < size; j++){
                        for(int k = 0; k < size; k++){
                            Entity sphere = new Entity();
                            sphere.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
                            sphere.getAttribute("name").setData("Sphere");
                            sphere.setMaterial(MaterialManager.getInstance().getDefaultMaterial());

                            sphere.setPosition(i * 8, j * 8, k * 8);

                           toAdd.add(sphere);
                        }
                    }
                    System.out.println(i);
                }

                EntityManager.getInstance().addEntity(toAdd);

//                sun = new DirectionalLight();
//                EntityManager.getInstance().addEntity(sun);
//                Entity drag = new Entity();
//
//                Sprite sprite = new Sprite(1,1);
//                sprite.setPixelColor(0,0, Colors.RED);
//                sprite.flush();
//
//                float size = 6f;
//
//                LinkedList<Entity> group = new LinkedList<Entity>();

//                for(int m = 0; m < size; m++){
//                    for(int r = 0; r < size; r++){
//                        Entity sphere = new Entity();
//                        sphere.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
//                        sphere.setPosition(new Vector3f(m * 2.25f, r * 2.25f, 0));
//                        sphere.getAttribute("mat_m").setData((float)m / size + 0.0001f);
//                        sphere.getAttribute("mat_r").setData((float)r / size + 0.0001f);
//                        sphere.setTexture(sprite);
//                        EntityManager.getInstance().addEntity(sphere);
//                    }
//                }
//
//                drag.setTexture(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_A.png"));
//                drag.setMetallic(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_M.png"));
//                drag.setRoughness(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_R.png"));
//                drag.setNormal(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_N.png"));
//                drag.setScale(0.01f);
//                drag.setRotation(new Vector3f(270 , 0 ,0 ));

//                EntityManager.getInstance().addEntity(drag);
//                EntityManager.getInstance().addEntity(new DirectionalLight());
//                EntityManager.getInstance().addEntity(new SpotLight());
//                EntityManager.getInstance().addEntity(new ParticleSystem());
//                EntityManager.getInstance().addEntity(new ParticleSystem());
//                EntityManager.getInstance().addEntity(new PointLight());
//                EntityManager.getInstance().addEntity(new Skybox());

//                Entity sphere = new Entity();
//                sphere.setModel(ModelManager.getInstance().loadModel("cube2.obj").getFirst());
//                sphere.setPosition(new Vector3f(0, 5, 0));
//                sphere.addComponent(new Collision());
//                EntityManager.getInstance().addEntity(sphere);


                StopwatchManager.getInstance().addTimer("tick");
//                StopwatchManager.getInstance().addTimer("tick_editor");
//                StopwatchManager.getInstance().addTimer("tick_physics");
//                StopwatchManager.getInstance().addTimer("shadowCalculations");
//                StopwatchManager.getInstance().addTimer("uploadUniforms");
                StopwatchManager.getInstance().addTimer("render");
//                StopwatchManager.getInstance().addTimer("render_editor");
//                StopwatchManager.getInstance().addTimer("drawCalls");
//                StopwatchManager.getInstance().addTimer("sort");

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
//                    StopwatchManager.getInstance().printAllDeltas();
//                    StopwatchManager.getInstance().clearAll();
                }

                render();

                glfwSwapBuffers(WINDOW_POINTER); // swap the color buffer //Actual render call
                glfwPollEvents();
                frames++;


                last = now;
            }

            //Set running to false.
            RUNNING = false;

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
        StopwatchManager.getInstance().update(delta);
//        ChromaManager.getInstance().tick(delta);
        //Update all engine components, this gives the editors widgets time to update
        StopwatchManager.getInstance().getTimer("tick").start();
        CameraManager.getInstance().update(delta);
        MousePicker.getInstance().tick(delta);
        SceneManager.getInstance().update(delta);
        LightingManager.getInstance().update(delta);
        EntityManager.getInstance().update(delta);
        SoundEngine.getInstance().update(delta);
        ParticleManager.getInstance().update(delta);
        ControllerManager.getInstance().update(delta);

//        Vector3f pos = MousePicker.rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), MousePicker.getInstance().getRay(), new Vector3f(0), new Vector3f(0, 1, 0));
//        if(pos == null){
//            pos = new Vector3f(0);
//        }else{
////            pos = pos.add(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).mul(1, 0, 0));
//        }
//        test1.setPosition(pos);

        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
//            StopwatchManager.getInstance().getTimer("tick_editor").start();
            Editor.getInstance().update(delta);
//            StopwatchManager.getInstance().getTimer("tick_editor").start();
        }else{
//            StopwatchManager.getInstance().getTimer("tick_physics").start();
            PhysicsEngine.getInstance().update(delta);
//            StopwatchManager.getInstance().getTimer("tick_physics").start();
        }
        StopwatchManager.getInstance().getTimer("tick").stop();
    }

    private static void render(){
        //Render World.
        StopwatchManager.getInstance().getTimer("render").start();
        Renderer.getInstance().render();
        ParticleManager.getInstance().render();

//        StopwatchManager.getInstance().getTimer("render_editor").start();

//        StopwatchManager.getInstance().getTimer("render_editor").stop();

        SceneManager.getInstance().render();

//        StopwatchManager.getInstance().getTimer("render_editor").lapStart();
        //If Dev render UI
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().preUIRender();
        }
//        StopwatchManager.getInstance().getTimer("render_editor").stop();

        PhysicsEngine.getInstance().render();

//        //UI
//        NanoVG.nvgBeginFrame(vg, Renderer.getWIDTH(), Renderer.getHEIGHT(), 1);
//
//        NVGColor colorA = NVGColor.create();
//
//        NanoVG.nvgFontSize(vg, 18.0f);
//        NanoVG.nvgFontFace(vg, "sans");
//        NanoVG.nvgFillColor(vg, NanoVG.nvgRGBA((byte)255, (byte)255, (byte)255, (byte)64, colorA));
//        NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_RIGHT | NanoVG.NVG_ALIGN_MIDDLE);
//        NanoVG.nvgText(vg, 129, 129, "test");
//
//        NanoVG.nvgEndFrame(vg);

        Renderer.getInstance().postpare();

        //If Dev render UI
//        StopwatchManager.getInstance().getTimer("render_editor").lapStart();
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)){
            Editor.getInstance().render();
        }
//        StopwatchManager.getInstance().getTimer("render_editor").stop();
        StopwatchManager.getInstance().getTimer("render").stop();
    }

    private static void shutdown(){
        Renderer.getInstance().onShutdown();
        SpriteBinder.getInstance().onShutdown();
        SoundEngine.getInstance().onShutdown();
        ChromaManager.getInstance().onShutdown();
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            Editor.getInstance().onShutdown();
        }
        VAOManager.getInstance().onShutdown();

        Set<Thread> threads = Thread.getAllStackTraces().keySet();

        for (Thread t : threads) {
            String name = t.getName();
            Thread.State state = t.getState();
            int priority = t.getPriority();
            String type = t.isDaemon() ? "Daemon" : "Normal";
            System.out.printf("%-20s \t %s \t %d \t %s\n", name, state, priority, type);
        }

    }


    public static void main(String[] args){
        Reactor.initialize();
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




        Reactor.run();
    }

    public static void enableDirectDraw(boolean draw){
        REACTOR_ENABLE_DIRECT_DRAW = draw;
    }

    public static boolean canDirectDraw(){
        return REACTOR_ENABLE_DIRECT_DRAW;
    }

}
