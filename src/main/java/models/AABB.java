package models;

import com.google.gson.JsonObject;
import entity.component.Event;
import org.joml.Vector3f;
import serialization.Serializable;
import serialization.SerializationHelper;

public class AABB implements Serializable<AABB> {
    private Vector3f min = new Vector3f(Integer.MAX_VALUE);
    private Vector3f max = new Vector3f(-Integer.MAX_VALUE);

    //8 Vertex Points
    private Vector3f[] vertecies = new Vector3f[8];

    public AABB(){
        recalculate();
    }

    public AABB(int size){
        min = new Vector3f(-Math.abs(size));
        max = new Vector3f( Math.abs(size));
        recalculate();
    }

    public AABB(Vector3f min, Vector3f max){
        this.min = min;
        this.max = max;
        recalculate();
    }

    //Takes in a new point and resizes AABB to abide by this new point possibly being bigger.
    public void recalculateFromPoint(Vector3f point){
        //X axis
        if(min.x > point.x){
            min.x = point.x;
        }
        if(max.x < point.x){
            max.x = point.x;
        }

        //Y axis
        if(min.y > point.y){
            min.y = point.y;
        }
        if(max.y < point.y){
            max.y = point.y;
        }

        //Z axis
        if(min.z > point.z){
            min.z = point.z;
        }
        if(max.z < point.z){
            max.z = point.z;
        }
        recalculate();
    }

    public Vector3f getMIN(){
        return min;
    }

    public Vector3f getMAX(){
        return max;
    }

    private void recalculate(){
        //Bottom
        this.vertecies[0] = new Vector3f(min.x, min.y, min.z);//-, -
        this.vertecies[1] = new Vector3f(max.x, min.y, min.z);//+, -
        this.vertecies[2] = new Vector3f(min.x, min.y, max.z);//-, +
        this.vertecies[3] = new Vector3f(max.x, min.y, max.z);//+, +
        //TOP
        this.vertecies[4] = new Vector3f(min.x, max.y, min.z);//-, -
        this.vertecies[5] = new Vector3f(max.x, max.y, min.z);//+, -
        this.vertecies[6] = new Vector3f(min.x, max.y, max.z);//-, +
        this.vertecies[7] = new Vector3f(max.x, max.y, max.z);//+, +
    }

    public Vector3f[] getVerteces(){
        return this.vertecies;
    }

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        min:{
            JsonObject helperObject = new JsonObject();
            helperObject.addProperty("class", min.getClass().getName());
            helperObject.add("value", SerializationHelper.getGson().toJsonTree(min));
            out.add("min", helperObject);
        }
        max:{
            JsonObject helperObject = new JsonObject();
            helperObject.addProperty("class", max.getClass().getName());
            helperObject.add("value", SerializationHelper.getGson().toJsonTree(max));
            out.add("max", helperObject);
        }
        return out;
    }

    @Override
    public AABB deserialize(JsonObject data) {
        if(data.has("min")) {
            JsonObject helper = data.get("min").getAsJsonObject();
            //Try resolve the class that was encoded
            Class<?> classType = null;
            try {
                classType = Class.forName(helper.get("class").getAsString());
                min = (Vector3f) SerializationHelper.getGson().fromJson(helper.get("value"), classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(data.has("max")) {
            JsonObject helper = data.get("max").getAsJsonObject();
            //Try resolve the class that was encoded
            Class<?> classType = null;
            try {
                classType = Class.forName(helper.get("class").getAsString());
                max = (Vector3f) SerializationHelper.getGson().fromJson(helper.get("value"), classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        recalculate();
        return this;
    }
}
