/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input.controller;

import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;

/**
 *
 * @author Bailey
 */
public enum EnumButtonType {
    LEFT_STICK_X("leftx"),
    LEFT_STICK_Y("lefty"),
    RIGHT_STICK_X("rightx"),
    RIGHT_STICK_Y("righty"),
    LEFT_TRIGGER("lefttrigger"),
    RIGHT_TRIGGER("righttrigger"),
    A("a"),
    B("b"),
    X("x"),
    Y("y"),
    LEFT_BUMPER("leftshoulder"),
    RIGHT_BUMPER("rightshoulder"),
    START("start"),
    SELECT(""),
    LEFT_STICK_PRESSED("leftstick"),
    RIGHT_STICK_PRESSED("rightstick"),
    D_PAD_UP("dpup"),
    D_PAD_DOWN("dpdown"),
    D_PAD_LEFT("dpleft"),
    D_PAD_RIGHT("dpright"),
    NULL(""),
    HOME("back"),
    ;

    private String name;
    EnumButtonType(String name){
        this.name = name;
    }

    public String getIdentifyingString(){
        return this.name;
    }
}
