package graphics.renderer;

import camera.CameraManager;
import entity.Entity;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import java.util.LinkedList;

public class ImmediateDrawTriangle {
    private int lineShaderID = 0;

    //Represents the items in world.
    private LinkedList<Float> positions = new LinkedList<>();
    private LinkedList<Float> colors    = new LinkedList<>();

    protected ImmediateDrawTriangle(){
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
    }


    public void drawTriangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f color) {
        positions.addLast(p1.x());
        positions.addLast(p1.y());
        positions.addLast(p1.z());
        positions.addLast(p2.x());
        positions.addLast(p2.y());
        positions.addLast(p2.z());
        positions.addLast(p3.x());
        positions.addLast(p3.y());
        positions.addLast(p3.z());

        colors.addLast(color.x());
        colors.addLast(color.y());
        colors.addLast(color.z());
        colors.addLast(color.x());
        colors.addLast(color.y());
        colors.addLast(color.z());
        colors.addLast(color.x());
        colors.addLast(color.y());
        colors.addLast(color.z());
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(lineShaderID);

        //Overall GL config
        GL20.glDisable(GL20.GL_CULL_FACE);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(lineShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());

        Handshake handshake = new Handshake();
        float[] positionsF = new float[positions.size()];
        int index = 0;
        for(float p : positions){
            positionsF[index] = p;
            index++;
        }

        float[] colorsF = new float[colors.size()];
        index = 0;
        for(float c : colors){
            colorsF[index] = c;
            index++;
        }
        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("color", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, positions.size() / EnumGLDatatype.VEC3.sizePerVertex);

        GL20.glUseProgram(0);

        //Clear what was rendered this frame, for next frame.
        positions.clear();
        colors.clear();

        //Overall GL config
        GL20.glEnable(GL20.GL_CULL_FACE);
    }
}
