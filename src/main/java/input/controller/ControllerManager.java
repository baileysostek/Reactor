/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;

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

    private ControllerManager() {
        lock = new ReentrantLock();

        controllers = new LinkedList<>();

        GLFW.glfwSetJoystickCallback(new GLFWJoystickCallbackI(){
            @Override
            public void invoke(int jid, int event) {
                if (event == GLFW.GLFW_CONNECTED) {
                    JavaController controller = new JavaController(jid);
                    controllers.add(controller);
                    for(Callback c : onControllerConnect){
                        c.callback(controller);
                    }
                }
                else if (event == GLFW.GLFW_DISCONNECTED){
                    try {
                        lock.unlock();
                        JavaController controller = controllers.get(jid);
                        for (Callback c : onControllerConnect) {
                            c.callback(controller);
                        }
                        controllers.remove(controller);
                    }finally{
                        lock.unlock();
                    }
                }
            }
        });

//        for(int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++){
//            if(GLFW.glfwJoystickPresent(i)){
//                controllers.add(new JavaController(i));
//            }else{
//
//            }
//        }
    }

    private int connectedControllers = 0;

    public void update(double delta) {
        for(JavaController controller: controllers){
            controller.poll();
        }
    }

    public void onShutdown() {

    }

    public int getNumberofConnectedControllers(){
        return this.connectedControllers;
    }

    public JavaController getController(int controllerIndex) {
       return this.controllers.get(controllerIndex);
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
