package graphics.renderer;

import camera.CameraManager;
import entity.Entity;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.HashMap;
import java.util.LinkedList;

public class ImmediateDrawTriangle {
    private int lineShaderID = 0;

    private int MAX_LINES = 2048;

    float[] positionsF;
    float[] colorsF;

    private int drawIndex = 0;

    private boolean expand = false;

    private Handshake handshake;

    protected ImmediateDrawTriangle(){
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
        handshake = new Handshake();

        resize();
    }

    private void resize(){
        positionsF = new float[MAX_LINES * 9];
        colorsF    = new float[MAX_LINES * 9];

        for(int i = 0; i < (MAX_LINES * 9); i++){
            positionsF[i] = 0f;
            colorsF[i]    = 0f;
        }
    }


    public DrawIndex drawTriangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f color) {
        if(drawIndex >= MAX_LINES){
            expand = true;
            return new DrawIndex(0, 0, 0,  0);
        }

        positionsF[drawIndex * 9 + 0] = (p1.x());
        positionsF[drawIndex * 9 + 1] = (p1.y());
        positionsF[drawIndex * 9 + 2] = (p1.z());
        positionsF[drawIndex * 9 + 3] = (p2.x());
        positionsF[drawIndex * 9 + 4] = (p2.y());
        positionsF[drawIndex * 9 + 5] = (p2.z());
        positionsF[drawIndex * 9 + 6] = (p3.x());
        positionsF[drawIndex * 9 + 7] = (p3.y());
        positionsF[drawIndex * 9 + 8] = (p3.z());

        colorsF[drawIndex * 9 + 0] = (color.x());
        colorsF[drawIndex * 9 + 1] = (color.y());
        colorsF[drawIndex * 9 + 2] = (color.z());
        colorsF[drawIndex * 9 + 3] = (color.x());
        colorsF[drawIndex * 9 + 4] = (color.y());
        colorsF[drawIndex * 9 + 5] = (color.z());
        colorsF[drawIndex * 9 + 6] = (color.x());
        colorsF[drawIndex * 9 + 7] = (color.y());
        colorsF[drawIndex * 9 + 8] = (color.z());

        DrawIndex data = new DrawIndex(drawIndex, 9, drawIndex,  9);
        drawIndex++;

        return data;
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(lineShaderID);

        ShaderManager.getInstance().loadUniformIntoActiveShader("screenSize", Renderer.getInstance().getScreenSize());

        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, Renderer.getInstance().getFrameBuffer().getTextureID());
        GL46.glUniform1i(GL46.glGetUniformLocation(lineShaderID, "depthTexture"), 0);

        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 1);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, Renderer.getInstance().getFrameBuffer().getDepthTexture());
        GL46.glUniform1i(GL46.glGetUniformLocation(lineShaderID, "sceneTexture"), 1);

        //Overall GL config
        GL46.glDisable(GL46.GL_CULL_FACE);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());

        Handshake handshake = new Handshake();

        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("color", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, drawIndex * 3);

        GL46.glUseProgram(0);

        //Clear what was rendered this frame, for next frame.

        //Overall GL config
        GL46.glEnable(GL46.GL_CULL_FACE);

        //Clear what was rendered this frame, for next frame.
        handshake.clear();

        drawIndex = 0;

        //Check about expand
        if(expand){
            MAX_LINES *= 2;
            resize();
            expand = false;
        }
    }

    public void recolor(int start, int length, Vector3f color){
        colorsF[((start * 9) + 0)] = color.x;
        colorsF[((start * 9) + 1)] = color.y;
        colorsF[((start * 9) + 2)] = color.z;
//
        colorsF[((start * 9) + 3)] = color.x;
        colorsF[((start * 9) + 4)] = color.y;
        colorsF[((start * 9) + 5)] = color.z;

        colorsF[((start * 9) + 6)] = color.x;
        colorsF[((start * 9) + 7)] = color.y;
        colorsF[((start * 9) + 8)] = color.z;
    }
}
