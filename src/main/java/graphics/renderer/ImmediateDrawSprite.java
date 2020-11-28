package graphics.renderer;

import camera.CameraManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.HashMap;
import java.util.LinkedList;

public class ImmediateDrawSprite {
    private int billboardShaderID = 0;

    //Represents the items in world.
    private LinkedList<Float> positions = new LinkedList<>();
    private LinkedList<Float> translations = new LinkedList<>();
    private HashMap<Integer, Float> colors    = new HashMap<Integer, Float>();

    protected ImmediateDrawSprite(){
        billboardShaderID = ShaderManager.getInstance().loadShader("billboard");
    }


    public void drawSprite(Vector3f position, int textureID) {
        Vector3f min = new Vector3f(-0.5f, -0.5f, 0);
        Vector3f max = new Vector3f( 0.5f,  0.5f, 0);

        positions.addLast(min.x);
        positions.addLast(min.y);
        positions.addLast(min.z);

        positions.addLast(max.x);
        positions.addLast(max.y);
        positions.addLast(max.z);

        positions.addLast(min.x);
        positions.addLast(max.y);
        positions.addLast(min.z);

        positions.addLast(max.x);
        positions.addLast(max.y);
        positions.addLast(max.z);

        positions.addLast(min.x);
        positions.addLast(min.y);
        positions.addLast(min.z);

        positions.addLast(max.x);
        positions.addLast(min.y);
        positions.addLast(max.z);

        //TODO fix
        translations.addLast(position.x);
        translations.addLast(position.y);
        translations.addLast(position.z);

        translations.addLast(position.x);
        translations.addLast(position.y);
        translations.addLast(position.z);

        translations.addLast(position.x);
        translations.addLast(position.y);
        translations.addLast(position.z);

        translations.addLast(position.x);
        translations.addLast(position.y);
        translations.addLast(position.z);
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(billboardShaderID);

        //Overall GL config
        GL46.glDisable(GL46.GL_CULL_FACE);

        Handshake handshake = new Handshake();
        float[] positionsF = new float[positions.size()];
        int index = 0;
        for(float p : positions){
            positionsF[index] = p;
            index++;
        }

        float[] colorsF = new float[positions.size()];
        index = 0;
        for(int i = 0; i < positions.size(); i++){
            colorsF[index] = (float) Math.random();
            index++;
        }

        float[] translationF = new float[translations.size()];
        index = 0;
        for(float p : translations){
            translationF[index] = p;
            index++;
        }


        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("translation", translationF, EnumGLDatatype.VEC3);
        handshake.addAttributeList("vTexture", colorsF, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(billboardShaderID, handshake);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(billboardShaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(billboardShaderID, "projection"),false, Renderer.getInstance().getProjectionMatrix());

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
