package graphics.renderer;

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
