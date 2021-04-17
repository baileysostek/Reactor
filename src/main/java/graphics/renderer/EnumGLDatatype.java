package graphics.renderer;

import com.google.gson.JsonObject;
import serialization.Serializable;

public enum EnumGLDatatype implements Serializable<EnumGLDatatype> {

    //Different data types that we can use in our shader.
    INT(Integer.BYTES, 1 ),

    FLOAT(Float.BYTES, 1 ),
    VEC2 (Float.BYTES, 2 ),
    VEC3 (Float.BYTES, 3 ),
    VEC4 (Float.BYTES, 4 ),

    SAMPLER2D(Integer.BYTES, 1 ),
    SAMPLER3D(Integer.BYTES, 1 ),

    //Matrix
    MAT3(Float.BYTES, 9 ),
    MAT4(Float.BYTES, 16),

    //Bool
    BOOL(1, 1)
    ;

    //Size in bytes of one pice of data
    protected int instanceSize; //Size of a single piece of data in bytes. IE a float
    protected int sizePerVertex;

    //used EXCLUSIVLY for Serialization
    EnumGLDatatype(){}

    EnumGLDatatype(int instanceSize, int sizePerVertex){
        this.instanceSize  = instanceSize;
        this.sizePerVertex = sizePerVertex;
    }

    public static EnumGLDatatype findSuitable(int size) {
        for(EnumGLDatatype value : EnumGLDatatype.values()){
            if(value.sizePerVertex == size){
                return value;
            }
        }

        System.out.println("[EnumGLDataType] Error: could not find suitable data type to store data of unit size:" + size);

        return null;
    }

    protected int getSize(){
        return this.instanceSize;
    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.addProperty("index", this.name());
        return out;
    }

    @Override
    public EnumGLDatatype deserialize(JsonObject data) {
        return EnumGLDatatype.valueOf(data.get("index").getAsString());
    }

    public int getSizePerVertex() {
        return sizePerVertex;
    }
}
