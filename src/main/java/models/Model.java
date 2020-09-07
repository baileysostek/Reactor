package models;

import com.google.gson.JsonObject;
import graphics.renderer.Handshake;
import org.joml.Vector3f;
import serialization.Serializable;

public class Model implements Serializable<Model> {

    //Metadata
    private String name;
    private int numIndicies = 0;
    private int id;

    private Vector3f[] aabb = new Vector3f[]{new Vector3f(-1), new Vector3f(1)};

    //VAO
    private Handshake handshake;

    //Just used to create a new pointer so deserilize can be called
    public Model(int id){
        this.id = id;
    }

    public Model(int id, Handshake handshake, int numIndicies, Vector3f[] AABB){
//        this.name      = name;
        this.id = id;
        this.handshake = handshake;
        this.numIndicies = numIndicies;
        this.aabb = AABB;
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

    public Vector3f[] getAABB(){
        return new Vector3f[]{new Vector3f(this.aabb[0]), new Vector3f(this.aabb[1])};
    }

    @Override
    public JsonObject serialize() {
        JsonObject saveData = new JsonObject();
        saveData.add("handshake", handshake.serialize());
        saveData.addProperty("indices", this.numIndicies);
        return saveData;
    }

    @Override
    public Model deserialize(JsonObject data) {
        this.numIndicies = data.get("indices").getAsInt();
        this.handshake = new Handshake().deserialize(data.get("handshake").getAsJsonObject());
        return this;
    }

}