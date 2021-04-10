package models;

import com.google.gson.JsonObject;
import entity.Entity;
import graphics.renderer.Handshake;
import graphics.renderer.VAO;
import graphics.renderer.VAOManager;
import material.Material;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIBone;
import serialization.Serializable;
import util.FileObject;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Model implements Serializable<Model> {

    //Metadata
    private String path;
    private int numIndicies = 0;
    private int id;

    private AABB aabb = new AABB();

    public LinkedList<Joint> rootJoints = new LinkedList<>();
    public LinkedHashMap<String, Joint> joints = new LinkedHashMap<>();
    public LinkedHashMap<String, Animation> animations = new LinkedHashMap<>();

    public LinkedHashMap<String, Joint> tPose = new LinkedHashMap<>();

    //VAO
    private Handshake handshake;
    private VAO vao;

    //Default Material
    private Material defaultMaterial;

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

        this.animations.clear();
        this.animations = animations;

        this.joints = joints;
        recalculateTPose();

        vao = new VAO(this);
    }

    public void setDefaultMaterial(Material passMaterial){
        this.defaultMaterial = passMaterial;
    }

    public Material getDefaultMaterial(){
        return defaultMaterial;
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

//        if( StringUtils.exists("models/" + this.path)) {
//            saveData.addProperty("file", this.path);
//        }else{
//            saveData.add("handshake", handshake.serialize());
//            saveData.addProperty("indices", this.numIndicies);
//            saveData.add("aabb", aabb.serialize());
//        }

        saveData.addProperty("file", this.path);

        return saveData;
    }

    @Override
    public Model deserialize(JsonObject data) {
        if(data.has("file")) {
            return ModelManager.getInstance().loadModel(data.get("file").getAsString()).getFirst();
        }
//        else {
//            this.numIndicies = data.get("indices").getAsInt();
//            this.handshake = new Handshake().deserialize(data.get("handshake").getAsJsonObject());
//            if(data.has("aabb")) {
//                this.aabb = new AABB().deserialize(data.get("aabb").getAsJsonObject());
//            }
//        }

        return this;
    }

    public void setJoints(LinkedHashMap<String, Joint> joints) {
        this.joints = joints;
        recalculateTPose();
    }

    private void recalculateTPose(){
        tPose.clear();
        for(Joint joint : this.joints.values()){
            Joint clone = new Joint(joint);
            clone.setAnimationTransform(ModelManager.getInstance().getIdentityMatrix());
            tPose.put(joint.getName(), clone);
            if(!joint.hasParent()){
                this.rootJoints.add(joint);
            }
        }
    }

    //todo refactor
    public void update(double delta){

    }

    public LinkedList<Joint> getRootJoints() {
        return rootJoints;
    }

    public LinkedHashMap<String, Joint> getAnimatedBoneTransforms(String animation, double deltaTime) {
        if(animations.containsKey(animation)) {
            return animations.get(animation).getBoneTransformsForTime(rootJoints, deltaTime);
        }else{
            return tPose;
        }
    }

    public Joint getAnimatedBoneTransform(String animation, String joint, double time) {
        LinkedHashMap<String, Joint> mapping = getAnimatedBoneTransforms(animation, time);
        if(mapping.containsKey(joint)){
            return mapping.get(joint);
        }else{
            return tPose.get(joint);
        }
    }

//    public Matrix4f getAnimatedBoneTransform(String animation, String bone, double deltaTime){
//        if(animations.containsKey(animation)) {
//            return drawBonesHelper(getRootJoint(), bone, new Matrix4f().identity(), deltaTime);
//        }
//        return new Matrix4f().identity();
//    }
//
//    private Matrix4f drawBonesHelper(Joint root, String search, Matrix4f parentTransform, HashMap<String, Matrix4f> frames) {
//        if(frames.containsKey(root.getName())) {
//            Matrix4f animationOffset = frames.get(root.getName());
//            Matrix4f currentTransform = new Matrix4f(parentTransform).mul(animationOffset);
//            for (Joint childJoint : root.getChildren()) {
//                drawBonesHelper(childJoint, currentTransform, frames);
//            }
//            Vector4f parentPos = new Vector4f(0, 0, 0, 1).mul(parentTransform);
//            Vector4f childPos = new Vector4f(0, 0, 0, 1).mul(currentTransform);
//
//            if(){
//
//            }
//        }
//        return null;
//    }

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

    public int getNumBones(){
        return this.joints.size();
    }

    public Collection<String> getBoneNames(){
        return this.joints.keySet();
    }
}
