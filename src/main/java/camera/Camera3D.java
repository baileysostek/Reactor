package camera;

import com.google.gson.JsonObject;
import input.Keyboard;
import input.MousePicker;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import serialization.SerializationHelper;

public class Camera3D extends Camera{

    //Camera controls
    float speed = 0.1f;

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

        if(Keyboard.getInstance().isKeyPressed(Keyboard.W)){
            this.translate(super.getLookingDirection().mul(-speed));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.S)){
            this.translate(super.getLookingDirection().mul(speed));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.A)){
            //TODO store a buffered quaternion set and Matrix 4f set to reduce memory fragmentation
            Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -90).normalize();
            super.translate((new Vector3f(0, 0, -1).mul(super.speed)).rotate(offsetRot.mul(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -1 * super.getRotationV().y())).normalize()));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.D)){
            //TODO store a buffered quaternion set and Matrix 4f set to reduce memory fragmentation
            Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), 90).normalize();
            super.translate((new Vector3f(0, 0, -1).mul(super.speed)).rotate(offsetRot.mul(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -1 * super.getRotationV().y())).normalize()));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.E)){
            super.translate(new Vector3f(0, -speed, 0));
        }
        if(Keyboard.getInstance().isKeyPressed(Keyboard.Q)){
            super.translate(new Vector3f(0, speed, 0));
        }

    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.add("pos", SerializationHelper.addClass(new Vector3f(this.getPosition()).mul(-1)));
        out.add("rot", SerializationHelper.addClass(this.getRotation()));
        out.add("off", SerializationHelper.addClass(this.getOffset()));
//        out.addProperty("rotSpeed", rotationSpeed);
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
