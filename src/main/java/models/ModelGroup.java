package models;

import entity.Entity;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import math.Vector3f;

import java.util.Collection;

public class ModelGroup {
    private Model model;
    private Collection<Entity> entities;

    public ModelGroup(Collection<Entity> entities){
        this.entities = entities;

    }

    public void regenerate(){
//        this.model = new Model();

        int vPositions_size = 0;
        int vNormals_size = 0;
        int vTextures_size = 0;

        int faceCount = 0;

        for(Entity entity : this.entities){
            Model model = entity.getModel();
            vPositions_size += model.getHandshake().getAttributeDataLength("vPosition");
            vNormals_size   += model.getHandshake().getAttributeDataLength("vNormal");
            vTextures_size  += model.getHandshake().getAttributeDataLength("vTexture");
            faceCount += (model.getNumIndicies() / 3);
        }

        //Arrays here
        float[] vPositions = new float[vPositions_size];
        float[] vNormals = new float[vPositions_size];
        float[] vTextures = new float[vPositions_size];

        for(Entity entity : this.entities){
            Model model = entity.getModel();
            //Apply Relative Transform to Pos and Normals
//            for(float value : model.getHandshake().attribute){
//
//            }

        }

        Handshake modelHandshake = new Handshake();
        modelHandshake.addAttributeList("vPosition", vPositions, EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vNormal", vNormals, EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vTexture", vTextures, EnumGLDatatype.VEC2);

//        model = new Model(Math.random(), modelHandshake, faceCount, ModelManager.getInstance().getAABB(verteciesList));
    }

    public Model getModel(){
        return this.model;
    }
}
