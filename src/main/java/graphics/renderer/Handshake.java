package graphics.renderer;

//A Handshake is a collection of data to be rendered by a shader.
//When a shader context is bound, a Handshake can be passed to it
//If the Handshake contains references to the attributes needed by
//the shader. The handshake is successful and the object renders.

//This class was specifically designed to operate similar to a VAO since OpenGL ES 2.0 does not have vaos

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.lwjgl.opengl.GL15;
import serialization.Serializable;
import serialization.SerializationHelper;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;

public class Handshake implements Serializable<Handshake> {

    //VAO
    private VAO vao = new VAO();

    //Stored attributes
    private HashMap<String, SerializableBuffer> bufferedAttributes = new HashMap<>();
    private HashMap<String, EnumGLDatatype>     bufferedSizes      = new HashMap<>();
    private LinkedList<String> bufferNameIndexes = new LinkedList<>();

    //Indicies
    private int[] indicies;

    public Handshake(){

    }

    public void addIndicies(int[] indicies) {
        VAOManager.getInstance().bindIndiciesBuffer(vao, indicies);
        this.indicies = indicies;
    }

    public void addAttributeList(String name, float[] data, EnumGLDatatype datatype){
        //Add to VAO as well
        VAOManager.getInstance().addVBO(vao, datatype.getSize(), data);

        //Buffer into our buffers
        bufferedAttributes.put(name, new SerializableBuffer(data, datatype));
        bufferedSizes.put(name, datatype);
        bufferNameIndexes.addLast(name);

        System.out.println("[.obj]["+name+"]"+vao.getSize());
    }

    public void addAttributeList(String name, SerializableBuffer buffer){
        //Add to VAO as well
        VAOManager.getInstance().addVBO(vao, buffer.getType().getSize(), buffer.getRaw());

        //Buffer into our buffers
        bufferedAttributes.put(name, buffer);
        bufferedSizes.put(name, buffer.getType());
        bufferNameIndexes.addLast(name);

        System.out.println("[.tek]["+name+"]"+vao.getSize());
    }

    public Buffer getAttribute(String attribute) {
        return bufferedAttributes.get(attribute).getBuffer();
    }


    public boolean hasAttribute(String attribute) {
        return this.bufferedAttributes.containsKey(attribute);
    }

    public int getAttributeSize(String attribute) {
        return bufferedSizes.get(attribute).sizePerVertex;
    }

    public VAO getVAO() {
        return this.vao;
    }

    public int getAttributeLength(){
        return this.bufferedAttributes.size();
    }

    @Override
    public JsonObject serialize() {
        JsonObject saveData = new JsonObject();

        saveData.add("attributes", SerializationHelper.serializeHashMap(bufferedAttributes, bufferNameIndexes));
        saveData.add("sizes", SerializationHelper.serializeHashMap(bufferedSizes));
        saveData.add("indices", SerializationHelper.serializeArray(indicies));

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
        //indices
        this.indicies = new int[data.get("indices").getAsJsonArray().size()];
        int index =0;
        for(Object obj : data.get("indices").getAsJsonArray()){
            int value = ((JsonPrimitive)obj).getAsInt();
            indicies[index] = value;
            index++;
        }
        this.addIndicies(indicies);
        return this;
    }

}