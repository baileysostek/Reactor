package camera;

import input.Keyboard;
import input.MousePicker;
import org.joml.Vector3f;

public class DynamicCamera extends Camera{

    //Camera controls
    float speed = 0.1f;
    float rotationSpeed = 4.5f;

    public DynamicCamera(){
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

    }

}
