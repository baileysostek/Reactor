/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;


import engine.Engine;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWJoystickCallbackI;

import javax.script.ScriptEngine;

import java.util.LinkedList;

import static com.codedisaster.steamworks.SteamRemoteStorage.WorkshopFileType.Game;

/**
 *
 * @author Bailey
 */
public class ControllerManager extends Engine {

    private LinkedList<JavaController> controllers;

    public ControllerManager() {

    }

    private int connectedControllers = 0;

    public void init() {
        controllers = new LinkedList<>();


        GLFW.glfwSetJoystickCallback(new GLFWJoystickCallbackI(){
            @Override
            public void invoke(int jid, int event) {
                if (event == GLFW.GLFW_CONNECTED)
                {
                    controllers.add(new JavaController(jid));
                }
                else if (event == GLFW.GLFW_DISCONNECTED)
                {
                    controllers.remove(jid);
                }
            }
        });

        for(int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++){
            if(GLFW.glfwJoystickPresent(i)){
                controllers.add(new JavaController(i));
            }else{

            }
        }
    }

    public void tick() {
        for(JavaController controller: controllers){
            controller.poll();
        }
    }

    @Override
    public void onShutdown() {

    }

    public int getNumberOfControllers(){
        return this.connectedControllers;
    }

    public JavaController getController(int controllerIndex) {
       return this.controllers.get(controllerIndex);
    }
}
