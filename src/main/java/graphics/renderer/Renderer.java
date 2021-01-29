package graphics.renderer;

import camera.CameraManager;
import camera.DynamicCamera;
import engine.Reactor;
import entity.Entity;
import entity.EntityManager;
import graphics.sprite.SpriteBinder;
import graphics.ui.UIManager;
import lighting.DirectionalLight;
import lighting.Light;
import lighting.LightingManager;
import material.Material;
import math.MatrixUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46;
import skybox.SkyboxManager;
import util.Callback;
import util.StopwatchManager;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer{

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

    private Handshake cube;

    private Renderer(int width, int height){
        if(Reactor.isDev()) {
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

        cube = new Handshake();
        float[] defaultCubeMap = new float[]{
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };
        cube.addAttributeList("aPos", defaultCubeMap, EnumGLDatatype.VEC3);
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
        if(Reactor.isDev()) {
            frameBuffer.bindFrameBuffer();
            fboBound = true;
        }else{
            GL46.glViewport(0, 0, WIDTH, HEIGHT);
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
        }

        GL46.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glEnable(GL46.GL_STENCIL_TEST);
        GL46.glEnable(GL46.GL_CULL_FACE);
        GL46.glCullFace(GL46.GL_BACK);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT | GL46.GL_STENCIL_BUFFER_BIT);

        EntityManager.getInstance().resort();
//        StopwatchManager.getInstance().getTimer("sort").stop();


        //Render all entities
//        StopwatchManager.getInstance().getTimer("drawCalls").start();
//        int lastID = -1;
//        int lastTexture = -1;
//        int loads = 0;


        LinkedHashMap<VAO, LinkedHashMap<Material, LinkedList<Entity>>> batches = EntityManager.getInstance().getBatches();

        int numDrawCalls = 0;

        for(VAO vao : batches.keySet()) {
            if(vao == null){
                continue;
            }
            LinkedHashMap<Material, LinkedList<Entity>> materialEntities = batches.get(vao);
            for(Material mat : materialEntities.keySet()){
                int shaderID = mat.getShaderID();

                ShaderManager.getInstance().useShader(shaderID);

                ShaderManager.getInstance().loadUniformIntoActiveShader("cameraPos", CameraManager.getInstance().getActiveCamera().getPosition());
                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "perspective"), false, projectionMatrix);

                //Pos -2 is skybox
                GL46.glActiveTexture(GL46.GL_TEXTURE0 + 5);
                GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, SkyboxManager.getInstance().getSkyboxTexture());
                GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "skybox"), 5);

                //-1 is closest probe to entity

                //Compute per frame, refactor to per entity eventually
                LinkedList<DirectionalLight> shadowCasters = LightingManager.getInstance().getClosestLights(5, new Vector3f(0));
                int numDirectionalLights = Math.min(shadowCasters.size(), GL46.glGetInteger(GL46.GL_MAX_TEXTURE_IMAGE_UNITS) - TEXTURE_OFFSET);
                for (int directionalLightIndex = 0; directionalLightIndex < numDirectionalLights; directionalLightIndex++) {

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
                for (Light l : LightingManager.getInstance().getClosestPointLights(4, new Vector3f(0))) {
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightPosition", index, l.getPosition());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightColor", index, l.getColor());
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightIntensity", index, l.getBrightness());
                    index++;
                }
                ShaderManager.getInstance().loadUniformIntoActiveShader("numPointLights", index);

                loadMaterialIntoShader(shaderID, mat);

                //Draw calls
                vao.render(materialEntities.get(mat));
                numDrawCalls++;
            }
        }

        UIManager.getInstance().drawString(128, 4, "Batches:" + numDrawCalls);

    }

    public void postpare(){
        //Render our lines!
        StopwatchManager.getInstance().getTimer("render_direct").start();
        if(Reactor.isDev() || Reactor.canDirectDraw()) {
            DirectDraw.getInstance().render();
        }
        StopwatchManager.getInstance().getTimer("render_direct").stop();

        //Start HUD timer
        StopwatchManager.getInstance().getTimer("render_hud").start();
        //Generate our HudDrawCalls and then draw them.
        UIManager.getInstance().render();
        //Return GL to a known state
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);
        GL46.glEnable(GL46.GL_CULL_FACE);
        GL46.glCullFace(GL46.GL_BACK);
        GL46.glFrontFace(GL46.GL_CCW);
        GL46.glEnable(GL46.GL_BLEND);
        GL46.glDisable(GL46.GL_DEPTH_TEST);
        GL46.glDisable(GL46.GL_SCISSOR_TEST);
        GL46.glColorMask(true, true, true, true);
        GL46.glStencilMask(0xffffffff);
        GL46.glStencilOp(GL46.GL_KEEP, GL46.GL_KEEP, GL46.GL_KEEP);
        GL46.glStencilFunc(GL46.GL_ALWAYS, 0, 0xffffffff);
        //Stop the timer
        StopwatchManager.getInstance().getTimer("render_hud").stop();

        if(fboBound) {
            frameBuffer.unbindFrameBuffer();
            fboBound = false;
        }
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

        if(entity == null){
            return SpriteBinder.getInstance().getFileNotFoundID();
        }

        if(entity.getModel() == null){
            return entity.getTextureID();
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
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

        if(entity.getModel() != null) {
            Material material = entity.getMaterial();
            int shaderID = material.getShaderID();
            VAO vao = entity.getModel().getVAO();

            ShaderManager.getInstance().useShader(shaderID);

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

            loadMaterialIntoShader(shaderID, material);

            LinkedList<Entity> entities = new LinkedList<>();
            entities.add(entity);
            vao.render(entities);
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
