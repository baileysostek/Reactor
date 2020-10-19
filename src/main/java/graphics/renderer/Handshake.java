package graphics.renderer;

//A Handshake is a collection of data to be rendered by a shader.
//When a shader context is bound, a Handshake can be passed to it
//If the Handshake contains references to the attributes needed by
//the shader. The handshake is successful and the object renders.

//This class was specifically designed to operate similar to a VAO since OpenGL ES 2.0 does not have vaos

import com.google.gson.JsonObject;
import serialization.Serializable;
import serialization.SerializationHelper;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.LinkedList;

public class Handshake implements Serializable<Handshake> {

    //Stored attributes
    private HashMap<String, SerializableBuffer> bufferedAttributes = new HashMap<>();
    private HashMap<String, EnumGLDatatype>     bufferedSizes      = new HashMap<>();
    private LinkedList<String> bufferNameIndexes = new LinkedList<>();

    public Handshake(){

    }

    public void addAttributeList(String name, float[] data, EnumGLDatatype datatype){

        //Buffer into our buffers
        bufferedAttributes.put(name, new SerializableBuffer(data, datatype));
        bufferedSizes.put(name, datatype);
        bufferNameIndexes.addLast(name);

    }

    public void addAttributeList(String name, SerializableBuffer buffer){
        //Buffer into our buffers
        bufferedAttributes.put(name, buffer);
        bufferedSizes.put(name, buffer.getType());
        bufferNameIndexes.addLast(name);
    }

    public Buffer getAttribute(String attribute) {
        return bufferedAttributes.get(attribute).getBuffer();
    }

    public int getAttributeDataLength(String attribute) {
        return bufferedAttributes.get(attribute).getRaw().length;
    }

    public int getAttributeDataLengthNormalized(String attribute) {
        return bufferedAttributes.get(attribute).getRaw().length / bufferedAttributes.get(attribute).getType().getSize();
    }


    public boolean hasAttribute(String attribute) {
        return this.bufferedAttributes.containsKey(attribute);
    }

    public int getAttributeSize(String attribute) {
        return bufferedSizes.get(attribute).sizePerVertex;
    }

    public int getAttributeLength(){
        return this.bufferedAttributes.size();
    }

    @Override
    public JsonObject serialize() {
        JsonObject saveData = new JsonObject();

        saveData.add("attributes", SerializationHelper.serializeHashMap(bufferedAttributes, bufferNameIndexes));
        saveData.add("sizes", SerializationHelper.serializeHashMap(bufferedSizes));

        return saveData;
    }

    @Override
    public Handshake deserialize(JsonObject data) {
        for(String key : data.get("attributes").getAsJsonObject().keySet()){
            SerializableBuffer buffer = new SerializableBuffer().deserialize(data.get("attributes").getAsJsonObject().get(key).getAsJsonObject());
            this.addAttributeList(key, buffer);
        }
        for(String key : data.get("sizes").getAsJsonObject().keySet()){
            bufferedSizes.put(key, EnumGLDatatype.valueOf(data.get("sizes").getAsJsonObject().get(key).getAsJsonObject().get("index").getAsString()));
        }

        return this;
    }

    public void clear() {
        bufferNameIndexes.clear();
        bufferedSizes.clear();
        bufferedAttributes.clear();
    }
}