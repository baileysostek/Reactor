package graphics.renderer;

import camera.CameraManager;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.SpriteBinder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class SkyboxRenderer {
    private int skyShaderID = 0;
    private final Handshake handshake;

    private final float SIZE = 500f;

    private final float[] positionsF;

    private final int textureID;

    protected SkyboxRenderer(){
        skyShaderID = ShaderManager.getInstance().loadShader("sky");

        textureID = SpriteBinder.getInstance().loadCubeMap("sky1");

        handshake = new Handshake();
        positionsF = new float[]{
            -SIZE,  SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            -SIZE,  SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE,  SIZE
        };
        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
    }

    public void render(){
        // Vertex data
        ShaderManager.getInstance().useShader(skyShaderID);

        //Overall GL config
        GL20.glDisable(GL20.GL_CULL_FACE);


        ShaderManager.getInstance().loadHandshakeIntoShader(skyShaderID, handshake);

        //Mess with uniforms
        float[] viewMatrix = CameraManager.getInstance().getActiveCamera().getTransform();
        viewMatrix[12] = 0;
        viewMatrix[13] = 0;
        viewMatrix[14] = 0;

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(skyShaderID, "viewMatrix"), false, viewMatrix);
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(skyShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID);

        //Render
        GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, positionsF.length / EnumGLDatatype.VEC3.sizePerVertex);

        GL20.glUseProgram(0);

        //Overall GL config
        GL20.glEnable(GL20.GL_CULL_FACE);
    }
}
