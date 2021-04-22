/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import util.Callback;
import util.Debouncer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 *
 * @author Bailey
 */
public class JavaController {
    
    private Vector3f leftThumbStick = new Vector3f(0,0,0);
    private Vector3f rightThumbStick = new Vector3f(0,0,0);
    private EnumButtonType[] button_names = new EnumButtonType[]{};
    private Debouncer[] buttonBouncers = new Debouncer[]{};
    private EnumButtonType[] axis_names = new EnumButtonType[]{};
    private float[] button_values = new float[]{};
    private float[] axis_values = new float[]{};

    private LinkedHashMap<EnumButtonType, Callback> onButtonPress   = new LinkedHashMap<>();
    private LinkedHashMap<EnumButtonType, Callback> whileButtonPress   = new LinkedHashMap<>();
    private LinkedHashMap<EnumButtonType, Callback> onButtonRelease = new LinkedHashMap<>();
    private LinkedHashMap<EnumButtonType, Callback> whileButtonRelease = new LinkedHashMap<>();

    private Vector3f axis_modifier = new Vector3f(1, 1, 1);

    private int index;

    private EnumControllerType type = EnumControllerType.UNKNOWN;
    
    private final float DEAD_ZONE = 0.1f;

    public JavaController(int index, LinkedList<String> buttonMapping){
        this.index = index;
        EnumButtonType[] mapping = EnumButtonType.values();
        EnumButtonType[] axis_mapping = EnumButtonType.values();

        System.out.println("Connected Gamepad["+index+"]:" + buttonMapping.get(0));

        mapping = new EnumButtonType[30];
        axis_mapping = new EnumButtonType[10];

        for(String entry : buttonMapping){
            if(entry.contains(":")) {
                String[] nameMapping = entry.split(":");
                EnumButtonType buttonType = ButtonUtils.getButtonForName(nameMapping[0]);
                String layoutLocation = nameMapping[1];
                if(layoutLocation.startsWith("a")){
                    try {
                        int location = Integer.parseInt(layoutLocation.replaceAll("a", ""));
                        axis_mapping[location] = buttonType;
                        System.out.println("Adding map for [" + buttonType + "] = " + location);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(layoutLocation.startsWith("b")){
                    try {
                        int location = Integer.parseInt(layoutLocation.replaceAll("b", ""));
                        mapping[location] = buttonType;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("Finished button mapping!");

        button_names = mapping;
        axis_names = axis_mapping;
        button_values = new float[button_names.length];
        buttonBouncers = new Debouncer[button_names.length];
        axis_values = new float[axis_names.length];
        for(int i = 0; i < button_values.length; i++){
            button_values[i] = 0.0f;
            buttonBouncers[i] = new Debouncer(false);
        }
        for(int i = 0; i < axis_values.length; i++){
            axis_values[i] = 0.0f;
        }

        System.out.println("Finished axis mapping!");
    }

//    public void removeButtonListener(Callback callback){
//        if(onButtonPress.containsValue(callback)){
//
//        }
//    }

    protected void poll(){
//        Game.logManager.println();
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index);
        int buttonIndex = 0;

        while (buttons.hasRemaining()) {
            int state = buttons.get();
            EnumButtonType button = this.button_names[buttonIndex];
            if (state == GLFW.GLFW_PRESS) {
                button_values[buttonIndex] = 1.0f;
                if(buttonBouncers[buttonIndex].risingAction(true)) {
                    if (onButtonPress.containsKey(button)) {
                        onButtonPress.get(button).callback(button_values[buttonIndex]);
                    }
                }

                if (whileButtonPress.containsKey(button)) {
                    whileButtonPress.get(button).callback(button_values[buttonIndex]);
                }

            }else{
                button_values[buttonIndex] = 0.0f;

                if(buttonBouncers[buttonIndex].fallingAction(false)) {
                    if(onButtonRelease.containsKey(button)){
                        onButtonRelease.get(button).callback(button_values[buttonIndex]);
                    }
                }

                if (whileButtonRelease.containsKey(button)) {
                    whileButtonRelease.get(button).callback(button_values[buttonIndex]);
                }
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

    public void onPress(EnumButtonType type, Callback callback){
        this.onButtonPress.put(type, callback);
    }

    public void whilePressed(EnumButtonType type, Callback callback){
        this.whileButtonPress.put(type, callback);
    }

    public void onRelease(EnumButtonType type, Callback callback){
        this.onButtonRelease.put(type, callback);
    }

    public void whileReleased(EnumButtonType type, Callback callback){
        this.whileButtonRelease.put(type, callback);
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
