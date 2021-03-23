/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author Bailey
 */
public class JavaController {
    
    private Vector3f leftThumbStick = new Vector3f(0,0,0);
    private Vector3f rightThumbStick = new Vector3f(0,0,0);
    private EnumButtonType[] button_names = new EnumButtonType[]{};
    private EnumButtonType[] axis_names = new EnumButtonType[]{};
    private float[] button_values = new float[]{};
    private float[] axis_values = new float[]{};

    private Vector3f axis_modifier = new Vector3f(1, 1, 1);

    private int index;

    private EnumControllerType type = EnumControllerType.UNKNOWN;
    
    private final float DEAD_ZONE = 0.1f;

    public JavaController(int index){
        this.index = index;
        EnumButtonType[] mapping = EnumButtonType.values();
        EnumButtonType[] axis_mapping = EnumButtonType.values();
        
        if(GLFW.glfwGetJoystickName(index).toLowerCase().contains("xbox")){
            System.out.println("Xbox controller Recognised.");
            mapping = new EnumButtonType[]{
                EnumButtonType.A,
                EnumButtonType.B,
                EnumButtonType.X,
                EnumButtonType.Y,
                EnumButtonType.LEFT_BUMPER,
                EnumButtonType.RIGHT_BUMPER,
                EnumButtonType.START,
                EnumButtonType.SELECT,
                EnumButtonType.LEFT_STICK_PRESSED,
                EnumButtonType.RIGHT_STICK_PRESSED,
                EnumButtonType.D_PAD_UP,
                EnumButtonType.D_PAD_RIGHT,
                EnumButtonType.D_PAD_DOWN,
                EnumButtonType.D_PAD_LEFT,
            };
            axis_mapping = new EnumButtonType[]{
                    EnumButtonType.LEFT_STICK_X,
                    EnumButtonType.LEFT_STICK_Y,
                    EnumButtonType.RIGHT_STICK_X,
                    EnumButtonType.RIGHT_STICK_Y,
                    EnumButtonType.LEFT_TRIGGER,
                    EnumButtonType.RIGHT_TRIGGER,
            };
            axis_modifier = new Vector3f(1, -1, 1);
        }

        if(GLFW.glfwGetJoystickName(index).toLowerCase().contains("wireless")){
            System.out.println("PS4 controller Recognised.");
            mapping = new EnumButtonType[]{
                EnumButtonType.X,
                EnumButtonType.A,
                EnumButtonType.B,
                EnumButtonType.Y,
                EnumButtonType.LEFT_BUMPER,
                EnumButtonType.RIGHT_BUMPER,
                EnumButtonType.LEFT_TRIGGER,
                EnumButtonType.RIGHT_TRIGGER,
                EnumButtonType.START,
                EnumButtonType.SELECT,
                EnumButtonType.LEFT_STICK_PRESSED,
                EnumButtonType.RIGHT_STICK_PRESSED,
                EnumButtonType.HOME,
                EnumButtonType.NULL,
                EnumButtonType.D_PAD_UP,
                EnumButtonType.D_PAD_RIGHT,
                EnumButtonType.D_PAD_DOWN,
                EnumButtonType.D_PAD_LEFT,
            };
            axis_mapping = new EnumButtonType[]{
                    EnumButtonType.LEFT_STICK_X,
                    EnumButtonType.LEFT_STICK_Y,
                    EnumButtonType.RIGHT_STICK_X,
                    EnumButtonType.LEFT_TRIGGER,
                    EnumButtonType.RIGHT_TRIGGER,
                    EnumButtonType.RIGHT_STICK_Y,
            };
        }

        button_names = mapping;
        axis_names = axis_mapping;
        button_values = new float[button_names.length];
        axis_values = new float[axis_names.length];
        for(int i = 0; i < button_values.length; i++){
            button_values[i] = 0.0f;
        }
        for(int i = 0; i < axis_values.length; i++){
            axis_values[i] = 0.0f;
        }
    }

    public void poll(){
//        Game.logManager.println();
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index);
        int buttonIndex = 0;

        while (buttons.hasRemaining()) {
            int state = buttons.get();
            if (state == GLFW.GLFW_PRESS) {
                button_values[buttonIndex] = 1.0f;
            }else{
                button_values[buttonIndex] = 0.0f;
            }

            buttonIndex++;

            if(buttonIndex >= button_values.length){
                break;
            }
        }

        FloatBuffer axis = GLFW.glfwGetJoystickAxes(index);

        int axisIndex = 0;
        while (axis.hasRemaining()) {
            float state = axis.get();
            if(Math.abs(state) > DEAD_ZONE) {
                axis_values[axisIndex] = state;
                if(axis_names[axisIndex].equals(EnumButtonType.LEFT_STICK_X)){
                    axis_values[axisIndex] *= axis_modifier.x();
                }
                if(axis_names[axisIndex].equals(EnumButtonType.LEFT_STICK_Y)){
                    axis_values[axisIndex] *= axis_modifier.y();
                }
                if(axis_names[axisIndex].equals(EnumButtonType.RIGHT_STICK_X)){
                    axis_values[axisIndex] *= axis_modifier.x();
                }
                if(axis_names[axisIndex].equals(EnumButtonType.RIGHT_STICK_Y)){
                    axis_values[axisIndex] *= axis_modifier.y();
                }
//                Game.logManager.println(axis_names[axisIndex] + ":" + axis_values[axisIndex] + ":" + axisIndex);
            }else{
                axis_values[axisIndex] = 0.0f;
            }
            axisIndex++;
            if(axisIndex >= axis_values.length){
                break;
            }
        }
        leftThumbStick = (new Vector3f(getButton(EnumButtonType.LEFT_STICK_X), getButton(EnumButtonType.LEFT_STICK_Y), getButton(EnumButtonType.LEFT_TRIGGER))).mul(axis_modifier);
        rightThumbStick = (new Vector3f(getButton(EnumButtonType.RIGHT_STICK_X), getButton(EnumButtonType.RIGHT_STICK_Y), getButton(EnumButtonType.RIGHT_TRIGGER))).mul(axis_modifier);
    }
    
    public float getButton(EnumButtonType type){
        int index = 0;
        for(EnumButtonType button : button_names){
            if(button.equals(type)){
                return button_values[index];
            }
            index++;
        }
        index = 0;
        for(EnumButtonType button : axis_names){
            if(button.equals(type)){
                return axis_values[index];
            }
            index++;
        }
        return 0;
    }
    
    private void setButtonValue(EnumButtonType type, float value){
        int index = 0;
        for(EnumButtonType button : button_names){
            if(button.equals(type)){
                button_values[index] = value;
            }
            index++;
        }
    }
    
    public Vector3f getLeftThumbStick(){
        return this.leftThumbStick;
    }
    
    public Vector3f getRightThumbStick(){
        return this.rightThumbStick;
    }

    public int getIndex() {
        return index;
    }

    public EnumControllerType getType(){
        return type;
    }
}
