package camera;

import com.google.gson.JsonObject;
import graphics.renderer.Renderer;
import input.Keyboard;
import input.MousePicker;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import serialization.SerializationHelper;

public class FPSCamera extends Camera{

    //Camera controls
    float speed = 0.1f;
    float rotationSpeed = 45f;

    public FPSCamera(){
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
    }

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        out.add("pos", SerializationHelper.addClass(new Vector3f(this.getPosition()).mul(-1)));
        out.add("rot", SerializationHelper.addClass(this.getRotation()));
        out.add("off", SerializationHelper.addClass(this.getOffset()));
        out.addProperty("rotSpeed", rotationSpeed);
        out.addProperty("speed", speed);
        return out;
    }

    @Override
    public Camera deserialize(JsonObject data) {
        if(data.has("pos")){
            this.setPosition((Vector3f) SerializationHelper.toClass(data.get("pos").getAsJsonObject()));
        }
        if(data.has("rot")){
            this.setRotation((Quaternionf) SerializationHelper.toClass(data.get("rot").getAsJsonObject()));
        }
        if(data.has("off")){
            this.setOffset((Vector3f) SerializationHelper.toClass(data.get("off").getAsJsonObject()));
        }
        if(data.has("rotSpeed")){
            this.speed = data.get("rotSpeed").getAsFloat();
        }
        if(data.has("speed")){
            this.speed = data.get("speed").getAsFloat();
        }
        return this;
    }
}
