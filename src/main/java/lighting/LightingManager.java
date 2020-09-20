package lighting;

import camera.CameraManager;
import engine.FraudTek;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.Renderer;
import graphics.renderer.Shader;
import graphics.renderer.ShaderManager;
import input.MousePicker;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class LightingManager {
    private static LightingManager lightingManager;
    private int lightDepth;

    private LightingManager(){
        lightDepth = ShaderManager.getInstance().loadShader("depth");
    }

    //Here we prepare everything we need this frame
    public void update(double delta){

    }

    public void drawFromMyPerspective(DirectionalLight directionalLight) {
        directionalLight.getDepthBuffer().bindFrameBuffer();
        GL20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(lightDepth);
        GL20.glEnable(GL20.GL_DEPTH_TEST);
        GL20.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        GL20.glEnable(GL20.GL_BLEND);
        GL20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ShaderManager.getInstance().loadUniformIntoActiveShader("lightSpaceMatrix", FraudTek.sun.getLightspaceTransform());

        //Render all entities
        EntityManager.getInstance().resort();
        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {

                ShaderManager.getInstance().loadHandshakeIntoShader(lightDepth, entity.getModel().getHandshake());

                //Mess with uniforms
                GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(lightDepth, "model"), false, entity.getTransform().get(new float[16]));
                GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, entity.getModel().getNumIndicies());
            }
        }
        directionalLight.getDepthBuffer().unbindFrameBuffer();
    }

    public static void initialize(){
        if(lightingManager == null){
            lightingManager = new LightingManager();
        }
    }

    public static LightingManager getInstance(){
        return lightingManager;
    }
}
