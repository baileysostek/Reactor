package graphics.renderer;

import camera.CameraManager;
import entity.Entity;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.HashMap;
import java.util.LinkedList;

public class ImmediateDrawTriangle {
    private int lineShaderID = 0;

    //Represents the items in world.
    private LinkedList<Float> positions = new LinkedList<>();
    private HashMap<Integer, Float> colors    = new HashMap<Integer, Float>();

    protected ImmediateDrawTriangle(){
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
    }


    public DrawIndex drawTriangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f color) {
        int start_size = positions.size();
        positions.addLast(p1.x());
        positions.addLast(p1.y());
        positions.addLast(p1.z());
        positions.addLast(p2.x());
        positions.addLast(p2.y());
        positions.addLast(p2.z());
        positions.addLast(p3.x());
        positions.addLast(p3.y());
        positions.addLast(p3.z());

        int start_color = colors.size();
        colors.put(colors.size(), color.x());
        colors.put(colors.size(), color.y());
        colors.put(colors.size(), color.z());
        colors.put(colors.size(), color.x());
        colors.put(colors.size(), color.y());
        colors.put(colors.size(), color.z());
        colors.put(colors.size(), color.x());
        colors.put(colors.size(), color.y());
        colors.put(colors.size(), color.z());

        return new DrawIndex(start_size, 9, start_color,  9);
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(lineShaderID);

        //Overall GL config
        GL46.glDisable(GL46.GL_CULL_FACE);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());

        Handshake handshake = new Handshake();
        float[] positionsF = new float[positions.size()];
        int index = 0;
        for(float p : positions){
            positionsF[index] = p;
            index++;
        }

        float[] colorsF = new float[colors.size()];
        index = 0;
        for(float c : colors.values()){
            colorsF[index] = c;
            index++;
        }
        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("color", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, positions.size() / EnumGLDatatype.VEC3.sizePerVertex);

        GL46.glUseProgram(0);

        //Clear what was rendered this frame, for next frame.
        positions.clear();
        colors.clear();

        //Overall GL config
        GL46.glEnable(GL46.GL_CULL_FACE);
    }

    public void recolor(int start, int length, Vector3f color){
        for(int i = 0; i < length / 3; i++){
            colors.put(((i * 3) + 0) + start, color.x);
            colors.put(((i * 3) + 1) + start, color.y);
            colors.put(((i * 3) + 2) + start, color.z);
        }
    }
}
