package graphics.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class VAOManager {

    private static VAOManager vaoManager;

    private Queue<Integer> vaoIDS = new LinkedList<>();

    private VAOManager(){

    }

    public int genVertexArrays(){
        int vaoID = GL46.glGenVertexArrays();
        vaoIDS.add(vaoID);
        return vaoID;
    }

    public float[] attributeToFloat(Object uniform, int size) {

        float[] data = new float[size];

        loop :{
            if(uniform instanceof Integer){
                data[0] = (int) uniform;
                break loop;
            }
            if(uniform instanceof Float){
                data[0] = (float) uniform;
                break loop;
            }
            if(uniform instanceof Vector2f){
                Vector2f vector2f = (Vector2f) uniform;
                data[0] = vector2f.x();
                data[1] = vector2f.y();
                break loop;
            }
            if(uniform instanceof Vector3f){
                Vector3f vector3f = (Vector3f) uniform;
                data[0] = vector3f.x();
                data[1] = vector3f.y();
                data[2] = vector3f.z();
                break loop;
            }
            if(uniform instanceof Matrix3f){
                ((Matrix3f) uniform).get(data);
                break loop;
            }
            if(uniform instanceof Matrix4f){
                ((Matrix4f) uniform).get(data);
                break loop;
            }
        }
        return data;
    }

    //Cleanup
    public void onShutdown() {
        for(int i : vaoIDS){
            GL46.glDeleteVertexArrays(i);
        }
    }

    // Singleton instance getter / generator.
    public static void initialize(){
        if(vaoManager == null){
            vaoManager = new VAOManager();
        }
    }

    public static VAOManager getInstance(){
        return vaoManager;
    }
}
