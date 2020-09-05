package graphics.renderer;

import camera.CameraManager;
import engine.Engine;
import entity.Entity;
import entity.EntityManager;
import math.MatrixUtils;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import platform.EnumDevelopment;
import platform.PlatformManager;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer extends Engine {

    private static Renderer renderer;
    private static int WIDTH;
    private static int HEIGHT;

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 1024.0f;

    private float aspectRatio = 1.0f;

    public static int PIXELS_PER_METER = 16;

    private static int RENDER_TYPE = GL11.GL_TRIANGLES;

    private int shaderID = 0;
    private int lineShaderID = 0;

    private FBO frameBuffer;

    private static float[] projectionMatrix = new float[16];

    private Renderer(int width, int height){
        //init
        // Set the clear color
        GL.createCapabilities();

        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
//            GLUtil.setupDebugMessageCallback();
        }

        shaderID = ShaderManager.getInstance().loadShader("main");
        lineShaderID = ShaderManager.getInstance().loadShader("vector");
        ShaderManager.getInstance().useShader(shaderID);

        WIDTH = width;
        HEIGHT = height;

        //Overall GL config
        GL20.glEnable(GL20.GL_CULL_FACE);
        GL20.glCullFace(GL20.GL_BACK);

        System.out.println("Shader ID:"+shaderID);

        frameBuffer = new FBO(width, height);

    }

    public static void initialize(int width, int height){
        if(renderer == null){
            renderer = new Renderer(width, height);
            projectionMatrix = MatrixUtils.createProjectionMatrix();
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(renderer.shaderID, "perspective"),false, projectionMatrix);
            renderer.resize(width, height);
        }
    }

    public void setRenderType(int type){
        RENDER_TYPE = type;
    }

    public void resize(int width, int height){

        WIDTH = width;
        HEIGHT = height;
        aspectRatio = (float)width / (float)height;
        frameBuffer.resize(width, height);

        projectionMatrix = MatrixUtils.createProjectionMatrix();

        GL20.glViewport(0, 0, width, height);
    }

    public void render(){
        //TODO Cleanup, This code should live in the Shader class, and also the  PRojection Matrix should be beuffered on resize and not regenerated per frame, only regen on screen resize.
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.bindFrameBuffer();
        }

        GL20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(shaderID);
        GL20.glEnable(GL20.GL_DEPTH_TEST);
        GL20.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        GL20.glEnable(GL20.GL_BLEND);
        GL20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "perspective"),false, projectionMatrix);

        float[] out = new float[3];

        out[0] = CameraManager.getInstance().getActiveCamera().getLookingDirection().x() * -1.0f;
        out[1] = CameraManager.getInstance().getActiveCamera().getLookingDirection().y() * -1.0f;
        out[2] = CameraManager.getInstance().getActiveCamera().getLookingDirection().z() * -1.0f;

        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "inverseCamera"), out);

        GL20.glActiveTexture(GL20.GL_TEXTURE0);



        //Render calls from the loaded scene.
//        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "sunAngle"), new float[]{1, 0, 0});


        //Render all entities
        int lastID = -1;
        int lastTexture = -1;
        int loads = 0;
        EntityManager.getInstance().resort();
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
    }

    //TODO currently this is an immediate draw, it would be more efficient to create a buffer that is written into as the Vector draw calls come in, then do one single draw.

    public void drawAABB(Entity entity) {
        org.joml.Vector3f[] aabb = entity.getAABB();
        org.joml.Vector3f min = aabb[0];
        org.joml.Vector3f max = aabb[1];

        //6 draw calls...
        this.drawLine(min, new Vector3f(min).add(0, 0, 1), new Vector3f(1));
        this.drawLine(min, new Vector3f(min).add(0, 1, 0), new Vector3f(1));
        this.drawLine(min, new Vector3f(min).add(1, 0, 0), new Vector3f(1));
        this.drawLine(max, new Vector3f(min).add(0, 0, -1), new Vector3f(1));
        this.drawLine(max, new Vector3f(min).add(0, -1, 0), new Vector3f(1));
        this.drawLine(max, new Vector3f(min).add(-1, 0, 0), new Vector3f(1));
    }

    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        // Vertex data
        ShaderManager.getInstance().useShader(lineShaderID);
//        GL40.glUseProgram(lineShaderID);
//        GL20.glEnable(GL20.GL_DEPTH_TEST);
//        GL20.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(lineShaderID, "projectionMatrix"),false, projectionMatrix);

        Handshake handshake = new Handshake();
        handshake.addAttributeList("position", new float[]{
            from.x(),
            from.y(),
            from.z(),
            to.x(),
            to.y(),
            to.z()
        }, EnumGLDatatype.VEC3);

        //Mess with uniforms
        ShaderManager.getInstance().loadHandshakeIntoShader(lineShaderID, handshake);

        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(lineShaderID, "viewMatrix"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        ShaderManager.getInstance().loadUniformIntoActiveShader("color", new org.joml.Vector3f(color.x(), color.y(), color.z()));

        GL20.glDrawArrays(GL20.GL_LINES, 0, 2);

        GL20.glUseProgram(0);
    }

    public void postpare(){

        //Cleanup
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.unbindFrameBuffer();
        }

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
}
