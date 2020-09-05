package camera;

import engine.FraudTek;
import graphics.renderer.Renderer;
import input.Keyboard;
import input.MousePicker;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.event.KeyEvent;

public class Camera3D extends Camera{

    //Camera controls
    float speed = 0.1f;
    float rotationSpeed = 45f;

    public Camera3D(){
        //Pass by reference or value
//        System.out.println("Forward: "+ getForwardDir());
    }

    public void onActive(){
        MousePicker.getInstance().requestLockMouse();
    }

    public void onDeactivated(){
        MousePicker.getInstance().unlockMouse();
    }

    @Override
    public void update(double delta){

        super.setRotation(new Vector3f((float)(MousePicker.getInstance().getMouseDeltaY() / (Renderer.getHEIGHT() / 2f)) * rotationSpeed,(float)(MousePicker.getInstance().getMouseDeltaX() / (Renderer.getWIDTH() / 2f)) * rotationSpeed,0).add(super.getRotationV()));
//
        if(Keyboard.getInstance().isKeyPressed(Keyboard.W)){
            this.translate(super.getLookingDirection().mul(-speed));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.S)){
            this.translate(super.getLookingDirection().mul(speed));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.A)){
            //TODO store a buffered quaternion set and Matrix 4f set to reduce memory fragmentation
            Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), -90);
            Quaternionf out = new Quaternionf(super.getRotation().mul(offsetRot).normalize());
//
            super.translate((super.getLookingDirection().mul(super.speed)).rotate(out));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.A)){
            //TODO store a buffered quaternion set and Matrix 4f set to reduce memory fragmentation
            Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), 90);
            Quaternionf out = new Quaternionf(super.getRotation().mul(offsetRot).normalize());

            super.translate((super.getLookingDirection().mul(super.speed)).rotate(out));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.E)){
            super.translate(new Vector3f(0, -speed, 0));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.Q)){
            super.translate(new Vector3f(0, speed, 0));
        }

    }

}
