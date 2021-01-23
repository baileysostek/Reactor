package graphics.renderer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import serialization.Serializable;

import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class SerializableBuffer implements Serializable<SerializableBuffer> {

    private float[] raw;
    private Buffer buffer;
    private EnumGLDatatype type;

    public SerializableBuffer(float[] data, EnumGLDatatype type) {
        this.raw = data;
        this.buffer = BufferUtils.bufferData(data, type);
        this.type = type;
    }

    public SerializableBuffer(Buffer data, EnumGLDatatype type) {
        this.buffer = data;
        this.type = type;
    }

    //Used Exclusively for deserialization
    public SerializableBuffer() {}

    public Buffer getBuffer() {
        return this.buffer;
    }

    public EnumGLDatatype getType(){
        return this.type;
    }

    public float[] getRaw() {
        return this.raw;
    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();

        //Our data is stored in here
        JsonArray array = new JsonArray();

        //define our float array
        float[] arr = new float[buffer.remaining()];
        ((FloatBuffer)buffer).get(arr);

        //Write in backwards because add appends to the front, this will unflip our array
        for (int i = 0; i < arr.length; i++){
            //inverse
            if(false) {
                array.add(arr[arr.length - 1 - i]);
            }else{
                array.add(arr[i]);
            }
        }

        //NOW REMEMBER TO SET THE BUFFER INDEX BACK TO THE BEGINNING!
        buffer.rewind();

        out.add("bytes", array);
        out.add("type", type.serialize());
        return out;
    }

    @Override
    public SerializableBuffer deserialize(JsonObject data) {
        type   = EnumGLDatatype.valueOf(data.get("type").getAsJsonObject().get("index").getAsString());
        raw    = new Gson().fromJson(data.get("bytes"), (Type) float[].class);
        buffer = BufferUtils.bufferData(this.raw, this.type);
        return this;
    }
}
