/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;

import graphics.animation.EnumLoop;
import graphics.animation.Timeline;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import graphics.ui.UIManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWJoystickCallbackI;
import util.Callback;

import javax.script.ScriptEngine;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.codedisaster.steamworks.SteamRemoteStorage.WorkshopFileType.Game;

/**
 *
 * @author Bailey
 */
public class ControllerManager{


    private LinkedList<JavaController> controllers;

    //Callbacks
    private LinkedList<Callback> onControllerConnect    = new LinkedList<>();
    private LinkedList<Callback> onControllerDisconnect = new LinkedList<>();

    private static ControllerManager controllerManager;

    private Lock lock;

    private Timeline connectTimeline = new Timeline();

    private final int CONTROLLER_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/gamepad.svg", 1, 1, 96f);

    private ControllerManager() {
        lock = new ReentrantLock();

        controllers = new LinkedList<>();

        //Init timeline
        connectTimeline.addKeyFrame("alpha", 0, 0);
        connectTimeline.addKeyFrame("alpha", 0.3f, 1);
        connectTimeline.addKeyFrame("alpha", 0.6f, 0);
        connectTimeline.addKeyFrame("alpha", 0.9f, 1);
        connectTimeline.addKeyFrame("alpha", 1.2f, 0);

        connectTimeline.setLoop(EnumLoop.STOP_LAST_VALUE);

        GLFW.glfwSetJoystickCallback(new GLFWJoystickCallbackI(){
            @Override
            public void invoke(int jid, int event) {
                try {
                    lock.lock();
                    if (event == GLFW.GLFW_CONNECTED) {
                        JavaController controller = new JavaController(jid);
                        controllers.add(controller);
                        for(Callback c : onControllerConnect){
                            c.callback(controller);
                        }
                        connectTimeline.start();
                    }
                    else if (event == GLFW.GLFW_DISCONNECTED){
                        if(controllers.contains(jid)) {
                            JavaController controller = controllers.get(jid);
                            for (Callback c : onControllerConnect) {
                                c.callback(controller);
                            }
                            controllers.remove(controller);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        });

        for(int i = 0; i < 16; i++){
            GLFW.glfwJoystickPresent(i);
        }

        for(int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++){
            if(GLFW.glfwJoystickPresent(i)){
                controllers.add(new JavaController(i));
            }
        }
    }

    public void update(double delta) {
        try {
            for (JavaController controller : controllers) {
                controller.poll();
            }
        }catch (Exception e){

        }

        if(connectTimeline.isRunning()) {
            connectTimeline.update(delta);
            UIManager.getInstance().drawImage(Renderer.getWIDTH() - 64, 16, 64, 64, CONTROLLER_SVG);
        }
    }

    public void onShutdown() {

    }

    public int getNumberofConnectedControllers(){
        return this.controllers.size();
    }

    public JavaController getController(int controllerIndex) {
       return this.controllers.get(controllerIndex);
    }

    public void onControllerConnect(Callback callback){
        if(!this.onControllerConnect.contains(callback)){
            this.onControllerConnect.add(callback);
        }
    }

    public boolean removeOnConnectCallback(Callback c){
        if(this.onControllerConnect.contains(c)){
            return this.onControllerConnect.remove(c);
        }
        return false;
    }

    public static void initialize(){
        if(controllerManager == null){
            controllerManager = new ControllerManager();
        }
    }

    public static ControllerManager getInstance(){
        return controllerManager;
    }
}
