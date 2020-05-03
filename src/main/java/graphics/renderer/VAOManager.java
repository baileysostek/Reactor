package graphics.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import javax.script.ScriptEngine;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * Created by Bailey on 1/30/2018.
 */
public class VAOManager{

    private ArrayList<Integer> vaos = new ArrayList<Integer>();
    private  ArrayList<Integer> vbos = new ArrayList<Integer>();

    private static VAOManager manager;

    private VAOManager() {

    }

    public static void initialize(){
        if(manager == null){
            manager = new VAOManager();
        }
    }

    public static VAOManager getInstance() {
        return manager;
    }

    public int createVAO(){
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    public void bindIndiciesBuffer(VAO vao, int[] indicies){
        GL30.glBindVertexArray(vao.getID());
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = org.lwjgl.BufferUtils.createIntBuffer(indicies.length);
        buffer.put(indicies);
        buffer.flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    public void addVBO(VAO vao, int coordinateSize, float[] data){
        GL30.glBindVertexArray(vao.getID());
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(vao.getSize(), coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        vao.setSize(vao.getSize()+1);
    }

    public void unbindVAO(){
        GL30.glBindVertexArray(0);
    }


    private FloatBuffer storeDataInFloatBuffer(float[] data){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private IntBuffer storeDataInIntBuffer(int[] data){
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }


    public void onShutdown() {
        for(Integer id : vaos){
            GL30.glDeleteVertexArrays(id);
        }
        for(Integer id : vbos){
            GL15.glDeleteBuffers(id);
        }
    }
}