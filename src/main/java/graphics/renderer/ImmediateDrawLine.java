package graphics.renderer;

import camera.CameraManager;
import entity.Entity;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

public class ImmediateDrawLine {
    private int lineShaderID = 0;

    private Handshake handshake;

    private final int MAX_LINES = 64;

    float[] positionsF;
    float[] colorsF;

    private int drawIndex = 0;

    protected ImmediateDrawLine(){
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
        handshake = new Handshake();

        //Allocate our memory
        //Each line has 2 vec3 and 2 colors, meaning 6*numLines floats
        positionsF = new float[MAX_LINES * 6];
        colorsF    = new float[MAX_LINES * 6];

        for(int i = 0; i < (MAX_LINES * 6); i++){
            positionsF[i] = 0f;
            colorsF[i]    = 0f;
        }
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
        if(drawIndex >= MAX_LINES){
            return;
        }
        positionsF[drawIndex * 6 + 0] = (from.x());
        positionsF[drawIndex * 6 + 1] = (from.y());
        positionsF[drawIndex * 6 + 2] = (from.z());
        positionsF[drawIndex * 6 + 3] = (to.x());
        positionsF[drawIndex * 6 + 4] = (to.y());
        positionsF[drawIndex * 6 + 5] = (to.z());

        colorsF[drawIndex * 6 + 0] = (color.x());
        colorsF[drawIndex * 6 + 1] = (color.y());
        colorsF[drawIndex * 6 + 2] = (color.z());
        colorsF[drawIndex * 6 + 3] = (color.x());
        colorsF[drawIndex * 6 + 4] = (color.y());
        colorsF[drawIndex * 6 + 5] = (color.z());
        drawIndex++;
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(lineShaderID);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());

        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("color", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL46.glDrawArrays(GL46.GL_LINES, 0, MAX_LINES);

        GL46.glUseProgram(0);

        //Clear what was rendered this frame, for next frame.
        handshake.clear();

        drawIndex = 0;
    }
}
