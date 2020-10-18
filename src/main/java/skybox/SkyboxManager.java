package skybox;

import camera.CameraManager;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;

import java.util.LinkedList;

public class SkyboxManager {
    private int skyShaderID = 0;
    private final Handshake handshake;

    private final float SIZE = 500f;

    private final float[] positionsF;

    private LinkedList<Skybox> skyboxes = new LinkedList<>();
    private Skybox selected;

    private int defaultSkyboxTexture;

    private static SkyboxManager skyboxManager;

    private SkyboxManager(){
        skyShaderID = ShaderManager.getInstance().loadShader("sky");

        defaultSkyboxTexture = SpriteBinder.getInstance().generateCubeMap(new Vector4f(0, 0, 0, 1));

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
        GL46.glDisable(GL46.GL_CULL_FACE);


        ShaderManager.getInstance().loadHandshakeIntoShader(skyShaderID, handshake);

        //Mess with uniforms
        float[] viewMatrix = CameraManager.getInstance().getActiveCamera().getTransform();
        viewMatrix[12] = 0;
        viewMatrix[13] = 0;
        viewMatrix[14] = 0;

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(skyShaderID, "viewMatrix"), false, viewMatrix);
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(skyShaderID, "projectionMatrix"), false, Renderer.getInstance().getProjectionMatrix());

        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, getSkyboxTexture());

        //Render
        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, positionsF.length / EnumGLDatatype.VEC3.getSizePerVertex());

        GL46.glUseProgram(0);

        //Overall GL config
        GL46.glEnable(GL46.GL_CULL_FACE);
    }

    public void addSkybox(Skybox skybox){
        if(skybox != null){
            skyboxes.addLast(skybox);
            if(this.selected == null){
                this.selected = skybox;
            }
        }
    }

    public void remove(Skybox skybox){
        if(this.skyboxes.contains(skybox)){
            this.skyboxes.remove(skybox);
            if(this.selected == skybox){
                this.selected = null;
            }
        }
    }

    public void setSelected(Skybox skybox){
        this.selected = skybox;
    }

    public static void initialize(){
        if(skyboxManager == null){
            skyboxManager = new SkyboxManager();
        }
    }

    public static SkyboxManager getInstance(){
        return skyboxManager;
    }

    public Skybox getSkybox() {
        return this.selected;
    }

    public int getSkyboxTexture(){
        if(this.selected != null){
            return selected.getTextureID();
        }else {
            return defaultSkyboxTexture;
        }
    }

    public void update(double delta) {
        
    }

    public boolean hasSkybox() {
        return this.selected != null;
    }
}
