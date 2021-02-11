package models;

import com.google.gson.JsonObject;
import graphics.renderer.Handshake;
import graphics.renderer.VAO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIBone;
import serialization.Serializable;
import util.FileObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Model implements Serializable<Model> {

    //Metadata
    private String path;
    private int numIndicies = 0;
    private int id;

    private AABB aabb = new AABB();

    public Joint rootJoint;
    public LinkedHashMap<String, Joint> joints = new LinkedHashMap<>();
    public LinkedHashMap<String, Animation> animations = new LinkedHashMap<>();

    //VAO
    private Handshake handshake;
    private VAO vao;

    //Just used to create a new pointer so deserilize can be called
    public Model(int id){
        this.id             = id;
    }

    public Model(int id, String path){
        this.id             = id;
        this.path           = path;
    }

    public Model(int id, String path, Handshake handshake, int numIndicies, Vector3f[] AABB, Joint rootJoint, LinkedHashMap<String, Animation> animations, LinkedHashMap<String, Matrix4f> boneOffsets, LinkedHashMap<String, Joint> joints){
        this.id             = id;
        this.path           = path;
        this.handshake      = handshake;
        this.numIndicies    = numIndicies;
        aabb = new AABB(AABB[0], AABB[1]);

        this.rootJoint = rootJoint;

        this.animations.clear();
        this.animations = animations;

        this.joints = joints;

        vao = new VAO(this);
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
    public JsonObject serialize(JsonObject meta) {
        JsonObject saveData = new JsonObject();
//        saveData.add("handshake", handshake.serialize());
//        saveData.addProperty("indices", this.numIndicies);
//        saveData.add("aabb", aabb.serialize());
        saveData.addProperty("file", this.path);

        return saveData;
    }

    @Override
    public Model deserialize(JsonObject data) {
//        this.numIndicies = data.get("indices").getAsInt();
//        this.handshake = new Handshake().deserialize(data.get("handshake").getAsJsonObject());
//        if(data.has("aabb")) {
//            this.aabb = new AABB().deserialize(data.get("aabb").getAsJsonObject());
//        }
        if(data.has("file")) {
            return ModelManager.getInstance().loadModel(data.get("file").getAsString()).getFirst();
        }

        return this;
    }

    public void setJoints(LinkedHashMap<String, Joint> joints) {
        this.joints = joints;
    }

    //todo refactor
    public void update(double delta){

    }

    public void setRootJoint(Joint joint) {
        this.rootJoint = joint;
    }

    public Joint getRootJoint() {
        return rootJoint;
    }

    public LinkedHashMap<String, Matrix4f> getAnimatedBoneTransforms(String animation, double deltaTime) {
        if(animations.containsKey(animation)) {
            return animations.get(animation).getBoneTransformsForTime(deltaTime);
        }else{
            //Get the first entry.
            return ((Animation)animations.values().toArray()[0]).getBoneTransformsForTime(deltaTime);
        }
    }

    public String getPath(){
        return path;
    }

    public VAO getVAO() {
        return vao;
    }

    public LinkedHashMap<String, Animation> getAnimations() {
        return animations;
    }

    public boolean hasAnimations() {
        return this.animations.size() > 0;
    }
}
