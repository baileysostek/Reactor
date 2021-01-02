package graphics.renderer;

import camera.CameraManager;
import camera.DynamicCamera;
import engine.Engine;
import engine.Reactor;
import entity.Entity;
import entity.EntityManager;
import graphics.sprite.SpriteBinder;
import input.Keyboard;
import lighting.DirectionalLight;
import lighting.Light;
import lighting.LightingManager;
import material.Material;
import material.MaterialManager;
import math.MatrixUtils;
import models.AABB;
import models.Joint;
import models.ModelManager;
import org.joml.*;
import org.lwjgl.opengl.GL46;
import platform.EnumDevelopment;
import platform.PlatformManager;
import skybox.SkyboxManager;
import util.Callback;

import java.lang.Math;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer extends Engine {

    private static Renderer renderer;

    private static int WIDTH;
    private static int HEIGHT;
    //Screen size as vec2
    private static Vector2f screenSize = new Vector2f();

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 1024.0f;
    //Reserve the first 7 textures for PBR
    public static final int TEXTURE_OFFSET = 7;

    private float aspectRatio = 1.0f;

    public static int PIXELS_PER_METER = 16;

    private static int RENDER_TYPE = GL46.GL_TRIANGLES;

    private FBO frameBuffer;

    private static float[] projectionMatrix = new float[16];

    private LinkedList<Callback> resizeCallbacks = new LinkedList<>();

    //Used for entity Previews
    private HashMap<Entity, FBO> snapshotFBOS = new HashMap<>();
    private DirectionalLight light;

    private boolean fboBound = false;

    VAO test;

    private Renderer(int width, int height){
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
//            GLUtil.setupDebugMessageCallback();
        }

        ShaderManager.getInstance().useShader(ShaderManager.getInstance().getDefaultShader());

        WIDTH = width;
        HEIGHT = height;

        screenSize.x = WIDTH;
        screenSize.y = HEIGHT;

        //Overall GL config
        GL46.glEnable(GL46.GL_CULL_FACE);
        GL46.glCullFace(GL46.GL_BACK);

        frameBuffer = new FBO(width, height);

        //INIT our light.
        light = new DirectionalLight();
    }

    public static void initialize(int width, int height){
        if(renderer == null){
            renderer = new Renderer(width, height);
            projectionMatrix = MatrixUtils.createProjectionMatrix();
            GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(ShaderManager.getInstance().getDefaultShader(), "perspective"),false, projectionMatrix);
            renderer.resize(width, height);
        }
    }


    public void setRenderType(int type){
        RENDER_TYPE = type;
    }

    public void resize(int width, int height){

        WIDTH = width;
        HEIGHT = height;

        screenSize.x = WIDTH;
        screenSize.y = HEIGHT;

        aspectRatio = (float)width / (float)height;
        frameBuffer.resize(width, height);

        projectionMatrix = MatrixUtils.createProjectionMatrix();

        GL46.glViewport(0, 0, width, height);

        //Now itterate through callbacks
        for(Callback c : resizeCallbacks){
            c.callback(width, height);
        }
    }

    //Renders the supplied Entities with the
    public void render(){
        this.renderEntities(EntityManager.getInstance().getEntities());
    }

    public void renderEntities(Collection<Entity> entities){
        //TODO Cleanup, This code should live in the Shader class, and also the  Projection Matrix should be buffered on resize and not regenerated per frame, only regen on screen resize.
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.bindFrameBuffer();
            fboBound = true;
            if(test == null){
                test = new VAO(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
            }
        }else{
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
        }

        GL46.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

//        GL46.glEnable(GL46.GL_BLEND);
//        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

//        StopwatchManager.getInstance().getTimer("uploadUniforms").start();


//        StopwatchManager.getInstance().getTimer("uploadUniforms").stop();

//        StopwatchManager.getInstance().getTimer("sort").start();
        EntityManager.getInstance().resort();
//        StopwatchManager.getInstance().getTimer("sort").stop();


        //Render all entities
//        StopwatchManager.getInstance().getTimer("drawCalls").start();
//        int lastID = -1;
//        int lastTexture = -1;
//        int loads = 0;


                int shaderID = ShaderManager.getInstance().getDefaultShader();

                ShaderManager.getInstance().useShader(shaderID);

                ShaderManager.getInstance().loadUniformIntoActiveShader("cameraPos", CameraManager.getInstance().getActiveCamera().getPosition());
                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "perspective"),false, projectionMatrix);

                //Pos -2 is skybox
                GL46.glActiveTexture(GL46.GL_TEXTURE0 + 5);
                GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, SkyboxManager.getInstance().getSkyboxTexture());
                GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "skybox"), 5);

                //-1 is closest probe to entity

                //Compute per frame, refactor to per entity eventually
                LinkedList<DirectionalLight> shadowCasters = LightingManager.getInstance().getClosestLights(5, new Vector3f(0));
                int numDirectionalLights = Math.min(shadowCasters.size(), GL46.glGetInteger(GL46.GL_MAX_TEXTURE_IMAGE_UNITS) - TEXTURE_OFFSET);
                for(int directionalLightIndex = 0; directionalLightIndex < numDirectionalLights; directionalLightIndex++){

                    DirectionalLight shadowCaster = shadowCasters.get(directionalLightIndex);
                    //Bind and allocate this texture unit.
                    int textureUnit = TEXTURE_OFFSET + directionalLightIndex;
                    GL46.glActiveTexture(GL46.GL_TEXTURE0 + textureUnit);
                    int textureIndex = shadowCaster.getDepthBuffer().getDepthTexture();
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureIndex);

                    //Upload our uniforms.
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("sunAngle", directionalLightIndex, new Vector3f(0).sub(shadowCaster.getPosition()).normalize());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("sunColor", directionalLightIndex, shadowCaster.getColor());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightSpaceMatrix", directionalLightIndex, shadowCaster.getLightspaceTransform());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("shadowMap", directionalLightIndex, textureUnit);
                }
                ShaderManager.getInstance().loadUniformIntoActiveShader("numDirectionalLights", numDirectionalLights);


                int index = 0;
                for(Light l : LightingManager.getInstance().getClosestPointLights(4, new Vector3f(0))){
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightPosition", index, l.getPosition());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightColor", index, l.getColor());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightIntensity", index, l.getBrightness());
                    index++;
                }
                ShaderManager.getInstance().loadUniformIntoActiveShader("numPointLights", index);



                Material material = MaterialManager.getInstance().getDefaultMaterial();
                loadMaterialIntoShader(shaderID, material);

//                ShaderManager.getInstance().loadAttributesFromEntity(entity);
//                StopwatchManager.getInstance().getTimer("uploadUniforms").stop();

                //Mess with uniforms
//                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "transform"), false, new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
//                StopwatchManager.getInstance().getTimer("drawCalls").lapStart();
//                GL46.glDrawArrays(RENDER_TYPE, 0, entity.getModel().getNumIndicies());
//                StopwatchManager.getInstance().getTimer("drawCalls").stop();
                test.render(entities);

            }




    public void postpare(){
        //Render our Skybox
        SkyboxManager.getInstance().render();

        //Render our lines!
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT) || Reactor.canDirectDraw()) {
            DirectDraw.getInstance().render();
        }

        if(fboBound) {
            frameBuffer.unbindFrameBuffer();
            fboBound = false;
        }

        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glDisable(GL46.GL_DEPTH_TEST);
        GL46.glDisable(GL46.GL_BLEND);
    }

    public FBO getFrameBuffer(){
        return this.frameBuffer;
    }

    public void addResizeCallback(Callback resize) {
        resizeCallbacks.add(resize);
    }

    public void removeResizeCallback(Callback resize) {
        resizeCallbacks.remove(resize);
    }

    @Override
    public void onShutdown() {

    }

    public float[] getProjectionMatrix(){
        return projectionMatrix;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public static int getWIDTH(){
        return WIDTH;
    }

    public static int getHEIGHT(){
        return HEIGHT;
    }

    public static Renderer getInstance(){
        return renderer;
    }

    //Preview with default shader
    public int generateRenderedPreview(Entity entity) {
        return generateRenderedPreview(entity.getMaterial().getShaderID(), entity);
    }

    public int generateRenderedPreview(int shaderID, Entity entity) {

        if(entity == null){
            return SpriteBinder.getInstance().getFileNotFoundID();
        }

        //TODO lookup table for FBOS
        FBO preview;
        if(snapshotFBOS.containsKey(entity)){
            preview = snapshotFBOS.get(entity);
        }else{
            preview = new FBO(512, 512);
            snapshotFBOS.put(entity, preview);
        }

        DynamicCamera cam = new DynamicCamera();
        Vector3f[] aabb = entity.getAABB();
        Vector3f distance = new Vector3f(aabb[1]).sub(aabb[0]);

        float max = Math.max(Math.max(distance.x, distance.y), distance.z);
        distance = new Vector3f(max);

        cam.setPosition(distance);
        cam.setRotation(new Quaternionf().lookAlong(new Vector3f(distance).normalize(), new Vector3f(0, 1, 0)));

        preview.bindFrameBuffer();

        GL46.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(shaderID);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

        ShaderManager.getInstance().loadUniformIntoActiveShader("cameraPos", cam.getPosition());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, cam.getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "perspective"),false, new Matrix4f().ortho(-max, max, -max, max, 0.1f, 1024).get(new float[16]));

        //Pos -2 is skybox
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 5);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, SkyboxManager.getInstance().getSkyboxTexture());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "skybox"), 5);

        light.setPosition(new Vector3f(max/2f, max, max));

        //Bind and allocate this texture unit.
        int textureUnit = TEXTURE_OFFSET + 0;
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + textureUnit);
        int textureIndex = light.getDepthBuffer().getDepthTexture();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureIndex);

        //Upload our uniforms.
        ShaderManager.getInstance().loadUniformIntoActiveShaderArray("sunAngle", 0, new Vector3f(0).sub(light.getPosition()).normalize());
        ShaderManager.getInstance().loadUniformIntoActiveShaderArray("sunColor", 0, light.getColor());
        ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightSpaceMatrix", 0, light.getLightspaceTransform());
        ShaderManager.getInstance().loadUniformIntoActiveShaderArray("shadowMap", 0, textureUnit);

        if(entity.getModel() != null) {
//                StopwatchManager.getInstance().getTimer("uploadUniforms").lapStart();
            ShaderManager.getInstance().loadHandshakeIntoShader(shaderID, entity.getModel().getHandshake());

            Material material = entity.getMaterial();

            loadMaterialIntoShader(shaderID, material);

            if (entity.hasAttribute("t_scale")) {
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_scale", entity.getAttribute("t_scale").getData());
            } else {
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_scale", new org.joml.Vector2f(1.0f));
            }

            if (entity.hasAttribute("t_offset")) {
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_offset", entity.getAttribute("t_offset").getData());
            } else {
                ShaderManager.getInstance().loadUniformIntoActiveShader("t_offset", new org.joml.Vector2f(0.0f));
            }

            if (entity.hasAttribute("mat_m")) {
                ShaderManager.getInstance().loadUniformIntoActiveShader("mat_m", entity.getAttribute("mat_m").getData());
            } else {
                ShaderManager.getInstance().loadUniformIntoActiveShader("mat_m", 0.5f);
            }

            if (entity.hasAttribute("mat_r")) {
                ShaderManager.getInstance().loadUniformIntoActiveShader("mat_r", entity.getAttribute("mat_r").getData());
            } else {
                ShaderManager.getInstance().loadUniformIntoActiveShader("mat_r", 0.5f);
            }
//                StopwatchManager.getInstance().getTimer("uploadUniforms").stop();

            //Mess with uniforms
            GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "transformation"), false, entity.getTransform().get(new float[16]));
//                StopwatchManager.getInstance().getTimer("drawCalls").lapStart();
            GL46.glDrawArrays(RENDER_TYPE, 0, entity.getModel().getNumIndicies());
//                StopwatchManager.getInstance().getTimer("drawCalls").stop();

        }

        //Render our Skybox
        SkyboxManager.getInstance().render();

        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glDisable(GL46.GL_DEPTH_TEST);
        GL46.glDisable(GL46.GL_BLEND);

        preview.unbindFrameBuffer();

        return preview.getTextureID();
    }

    private void loadMaterialIntoShader(int shaderID, Material material){
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, material.getAlbedoID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "textureID"), 0);

        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 1);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, material.getNormalID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "normalID"), 1);

        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 2);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, material.getMetallicID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "metallicID"), 2);

        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 3);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, material.getRoughnessID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "roughnessID"), 3);

        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 4);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, material.getAmbientOcclusionID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "aoID"), 4);
    }

    public Vector2f getScreenSize(){
        return screenSize;
    }
}
