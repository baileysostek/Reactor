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
    LEFT_STICK_X("A.png"),
    LEFT_STICK_Y("A.png"),
    RIGHT_STICK_X("A.png"),
    RIGHT_STICK_Y("A.png"),
    LEFT_TRIGGER("A.png"),
    RIGHT_TRIGGER("A.png"),
    A("A.png"),
    B("B.png"),
    X("X.png"),
    Y("Y.png"),
    LEFT_BUMPER("A.png"),
    RIGHT_BUMPER("A.png"),
    START("A.png"),
    SELECT("A.png"),
    LEFT_STICK_PRESSED("A.png"),
    RIGHT_STICK_PRESSED("A.png"),
    D_PAD_UP("A.png"),
    D_PAD_DOWN("A.png"),
    D_PAD_LEFT("A.png"),
    D_PAD_RIGHT("A.png"),
    NULL("A.png"),
    HOME("A.png"),

    ;

    protected Sprite sprite;
    EnumButtonType(String image){
        this.sprite = SpriteBinder.getInstance().load(image);
    }

}
