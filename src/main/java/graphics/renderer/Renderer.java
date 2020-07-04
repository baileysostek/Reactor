package graphics.renderer;

import camera.CameraManager;
import engine.Engine;
import entity.Entity;
import entity.EntityManager;
import math.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import platform.EnumDevelopment;
import platform.PlatformManager;
import scene.SceneManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer extends Engine {

    private static Renderer renderer;
    private static int WIDTH;
    private static int HEIGHT;

    private static final float FOV = 70.0f;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1024.0f;
    private static Matrix4f projectionMatrix;

    private float aspectRatio = 1.0f;

    public static int PIXELS_PER_METER = 16;

    private static int RENDER_TYPE = GL11.GL_TRIANGLES;

    private int shaderID = 0;

    private FBO frameBuffer;

    private Renderer(int width, int height){
        //init
        // Set the clear color
        GL.createCapabilities();

        //TODO isdebug
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
//            GLUtil.setupDebugMessageCallback();
        }

        shaderID = ShaderManager.getInstance().loadShader("main");
        ShaderManager.getInstance().useShader(shaderID);

        WIDTH = width;
        HEIGHT = height;

        //Overall GL config
        GL20.glEnable(GL20.GL_CULL_FACE);
        GL20.glCullFace(GL20.GL_BACK);

        System.out.println("Shader ID:"+shaderID);

        frameBuffer = new FBO(width, height);

    }

    public void setRenderType(int type){
        RENDER_TYPE = type;
    }

    public void resize(int width, int height){
        WIDTH = width;
        HEIGHT = height;
        aspectRatio = (float)width / (float)height;
        projectionMatrix = Maths.createProjectionMatrix(FOV, NEAR_PLANE, FAR_PLANE);
        frameBuffer.resize(width, height);
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "perspective"),false, MatrixUtils.createProjectionMatrix());
    }

    public int getWIDTH(){
        return WIDTH;
    }

    public int getHEIGHT(){
        return HEIGHT;
    }

    public static void initialize(int width, int height){
        if(renderer == null){
            renderer = new Renderer(width, height);
            projectionMatrix = Maths.createProjectionMatrix(FOV, NEAR_PLANE, FAR_PLANE);
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(renderer.shaderID, "perspective"),false, MatrixUtils.createProjectionMatrix());
//            GL20.glEnable(GL20.GL_DEPTH_TEST);
        }
    }

    public static Renderer getInstance(){
        return renderer;
    }

    public void render(){
        //Cleanup
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.bindFrameBuffer();
        }

        GL20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(shaderID);
//        GL20.glEnable(GL20.GL_DEPTH_TEST);
        GL20.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        GL20.glEnable(GL20.GL_BLEND);
        GL20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        float[] out = new float[3];

        out[0] = CameraManager.getInstance().getActiveCamera().getForwardDir().x() * -1.0f;
        out[1] = CameraManager.getInstance().getActiveCamera().getForwardDir().y() * -1.0f;
        out[2] = CameraManager.getInstance().getActiveCamera().getForwardDir().z() * -1.0f;

        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "inverseCamera"), out);

        GL20.glActiveTexture(GL20.GL_TEXTURE0);



        //Render calls from the loaded scene.
        SceneManager.getInstance().render();
//        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "sunAngle"), new float[]{1, 0, 0});


        //Render all entities
        int lastID = -1;
        int lastTexture = -1;
        int loads = 0;
        for(Entity entity : EntityManager.getInstance().getEntities()){

            if(entity.getModel().getID() != lastID) {
                ShaderManager.getInstance().loadHandshakeIntoShader(shaderID, entity.getModel().getHandshake());
                loads++;
            }

            if(lastTexture != entity.getTextureID()) {
                GL20.glBindTexture(GL20.GL_TEXTURE_2D, entity.getTextureID());
                GL20.glUniform1i(GL20.glGetUniformLocation(shaderID, "textureID"), GL20.GL_TEXTURE0);
                lastTexture = entity.getTextureID();
            }

            if(entity.hasAttribute("t_scale")){
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_scale", entity.getAttribute("t_scale").getData());
            }else{
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_scale", new org.joml.Vector2f(1.0f));
            }

            if(entity.hasAttribute("t_offset")){
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_offset", entity.getAttribute("t_offset").getData());
            }else{
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_offset", new org.joml.Vector2f(0.0f));
            }

            //Mess with uniforms
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "transformation"), false, entity.getTransform().get(new float[16]));
            GL20.glDrawArrays(RENDER_TYPE, 0, entity.getModel().getNumIndicies());

            lastID = entity.getModel().getID();
        }

        //Cleanup
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.unbindFrameBuffer();
        }
    }

    public void postpare(){
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL20.glDisable(GL20.GL_BLEND);
    }

    public FBO getFrameBuffer(){
        return this.frameBuffer;
    }

    @Override
    public void onShutdown() {
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }
}
