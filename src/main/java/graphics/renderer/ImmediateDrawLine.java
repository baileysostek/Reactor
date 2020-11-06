package graphics.renderer;

import camera.CameraManager;
import entity.Entity;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.Collection;
import java.util.LinkedList;

public class ImmediateDrawLine {
    private int lineShaderID = 0;

    //Represents the items in world.
    private LinkedList<Float> positions = new LinkedList<>();
    private LinkedList<Float> colors    = new LinkedList<>();

    protected ImmediateDrawLine(){
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
    }

    public void drawAABB(Entity entity) {
        org.joml.Vector3f[] aabb = entity.getAABB();
        org.joml.Vector3f min = aabb[0];
        org.joml.Vector3f max = aabb[1];

        //6 draw calls...
        drawLine(min, new Vector3f(min).add(0, 0, entity.getScale().z()) , new Vector3f(1));
        drawLine(min, new Vector3f(min).add(0, entity.getScale().y(), 0) , new Vector3f(1));
        drawLine(min, new Vector3f(min).add(entity.getScale().x(), 0, 0) , new Vector3f(1));
        drawLine(max, new Vector3f(max).add(0, 0, -entity.getScale().z()), new Vector3f(1));
        drawLine(max, new Vector3f(max).add(0, -entity.getScale().y(), 0), new Vector3f(1));
        drawLine(max, new Vector3f(max).add(-entity.getScale().x(), 0, 0), new Vector3f(1));
    }

    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        positions.addLast(from.x());
        positions.addLast(from.y());
        positions.addLast(from.z());
        positions.addLast(to.x());
        positions.addLast(to.y());
        positions.addLast(to.z());

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
//        GL40.glUseProgram(lineShaderID);
//        GL46.glEnable(GL46.GL_DEPTH_TEST);
//        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT);

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
        for(float c : colors){
            colorsF[index] = c;
            index++;
        }
        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("color", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL46.glDrawArrays(GL46.GL_LINES, 0, positions.size() / EnumGLDatatype.VEC3.sizePerVertex);

        GL46.glUseProgram(0);

        //Clear what was rendered this frame, for next frame.
        positions.clear();
        colors.clear();
    }
}
