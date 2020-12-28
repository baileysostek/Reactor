package camera;

import com.google.gson.JsonObject;
import input.Keyboard;
import input.MousePicker;
import org.joml.Vector3f;
import serialization.SerializationHelper;

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

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.add("pos", SerializationHelper.addClass(this.getPosition()));
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
            this.setRotation((Vector3f) SerializationHelper.toClass(data.get("rot").getAsJsonObject()));
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
