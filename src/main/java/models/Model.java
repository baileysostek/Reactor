package models;

import com.google.gson.JsonObject;
import graphics.renderer.Handshake;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import serialization.Serializable;
import util.FileObject;

import java.util.HashMap;
import java.util.LinkedList;

public class Model implements Serializable<Model> {

    //Metadata
    private String name;
    private int numIndicies = 0;
    private int id;

    private AABB aabb = new AABB();

    public Joint rootJoint;
    public LinkedList<Joint> joints = new LinkedList<Joint>();
    public Animation animation;

    //VAO
    private Handshake handshake;

    double time = 0;

    //Just used to create a new pointer so deserilize can be called
    public Model(int id){
        this.id = id;
    }

    public Model(int id, Handshake handshake, int numIndicies, Vector3f[] AABB){
//        this.name      = name;
        this.id = id;
        this.handshake = handshake;
        this.numIndicies = numIndicies;
        aabb = new AABB(AABB[0], AABB[1]);
    }

    public Handshake getHandshake(){
        return this.handshake;
    }

    public int getID(){
        return this.id;
    }

    public int getNumIndicies() {
        return this.numIndicies;
    }

    public AABB getAABB(){
        return this.aabb;
    }

    @Override
    public JsonObject serialize() {
        JsonObject saveData = new JsonObject();
        saveData.add("handshake", handshake.serialize());
        saveData.addProperty("indices", this.numIndicies);
        saveData.add("aabb", aabb.serialize());
        return saveData;
    }

    @Override
    public Model deserialize(JsonObject data) {
        this.numIndicies = data.get("indices").getAsInt();
        this.handshake = new Handshake().deserialize(data.get("handshake").getAsJsonObject());
        if(data.has("aabb")) {
            this.aabb = new AABB().deserialize(data.get("aabb").getAsJsonObject());
        }
        return this;
    }

    public void setJoints(LinkedList<Joint> joints) {
        this.joints = joints;
    }

    //todo refactor
    public void update(double delta){
        time+=delta;
    }

    public void setRootJoint(Joint joint) {
        this.rootJoint = joint;
    }

    public Joint getRootJoint() {
        return rootJoint;
    }

    public HashMap<String, Matrix4f> getAnimatedBoneTransforms() {
        return animation.getBoneTransformsForTime(time);
    }
}
