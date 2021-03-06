package graphics.renderer;

import camera.CameraManager;
import engine.Engine;
import entity.Entity;
import entity.EntityManager;
import math.MatrixUtils;
import models.AABB;
import org.joml.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import platform.EnumDevelopment;
import platform.PlatformManager;

import java.lang.Math;

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

    //Skybox
    private SkyboxRenderer skyboxRenderer;

    //ImmediateDraw
    private ImmediateDrawLine     drawerLine;
    private ImmediateDrawTriangle drawTriangle;

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

        skyboxRenderer = new SkyboxRenderer();

        drawerLine = new ImmediateDrawLine();
        drawTriangle = new ImmediateDrawTriangle();

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

        GL20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
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

    public void drawAABB(AABB aabb) {
        this.drawAABB(aabb.getMIN(), aabb.getMAX());
    }

    public void drawAABB(Entity entity) {
        org.joml.Vector3f[] aabb = entity.getAABB();
        org.joml.Vector3f min = aabb[0];
        org.joml.Vector3f max = aabb[1];

        this.drawAABB(min, max);
    }

    public void drawAABB(Vector3f min, Vector3f max) {
        //6 draw calls...
        this.drawLine(min, new Vector3f(max.x, min.y, min.z) , new Vector3f(1));
        this.drawLine(min, new Vector3f(min.x, max.y, min.z) , new Vector3f(1));
        this.drawLine(min, new Vector3f(min.x, min.y, max.z) , new Vector3f(1));

        this.drawLine(max, new Vector3f(min.x, max.y, max.z) , new Vector3f(1));
        this.drawLine(max, new Vector3f(max.x, min.y, max.z) , new Vector3f(1));
        this.drawLine(max, new Vector3f(max.x, max.y, min.z) , new Vector3f(1));

        //Bottom
        this.drawLine(new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, max.y, min.z) , new Vector3f(1));
        this.drawLine(new Vector3f(min.x, max.y, min.z), new Vector3f(min.x, max.y, max.z) , new Vector3f(1));

        this.drawLine(new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, min.y, min.z) , new Vector3f(1));
        this.drawLine(new Vector3f(max.x, min.y, max.z), new Vector3f(min.x, min.y, max.z) , new Vector3f(1));

        this.drawLine(new Vector3f(max.x, min.y, min.z), new Vector3f(max.x, max.y, min.z) , new Vector3f(1));
        this.drawLine(new Vector3f(min.x, min.y, max.z), new Vector3f(min.x, max.y, max.z) , new Vector3f(1));

    }

    public void drawRing(Vector3f origin, Vector2f radius, int points, Vector3f color) {
        float angle_spacing = 360.0f / (float)points;
        Vector3f lastPoint = null;
        Vector3f firstPoint = null;
        for(int i = 0; i < points; i++){
            float angle = (float) Math.toRadians((i * angle_spacing));

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(origin).add(x * radius.x, y * radius.y, 0);

            if(lastPoint != null) {
                drawLine(lastPoint, thisPoint, color);
            }else{
                firstPoint = thisPoint;
            }

            lastPoint = thisPoint;
        }

        if(lastPoint != null && firstPoint != null){
            drawLine(lastPoint, firstPoint, color);
        }
    }

    public DirectDrawData drawCone(Vector3f origin, Vector3f ray, Vector3f scale, int points, Vector3f color) {
        //Direct draw info
        DirectDrawData out = new DirectDrawData();
        AABB aabb = new AABB();


        float angle_spacing = 360.0f / (float)points;

        Vector3f usableRay = new Vector3f(ray).mul(Math.max(scale.x, 1), Math.max(scale.y, 1), Math.max(scale.x, 1));

        Vector3f lastPoint = null;
        Vector3f firstPoint = null;

        Vector3f tip = new Vector3f(usableRay).mul(1, 1, 1).add(origin);
        aabb.recalculateFromPoint(tip);

        Vector3f up = new Vector3f(0, (float) Math.cos(usableRay.z * Math.PI), (float) Math.cos(usableRay.y * Math.PI));

        Vector3f xAxis = new Vector3f(up).cross(usableRay).normalize();
        Vector3f yAxis = new Vector3f(usableRay).cross(xAxis).normalize();

        Matrix3f rotationMatrix = new Matrix3f().set(new float[]{
            xAxis.x, xAxis.y, xAxis.z,
            yAxis.x, yAxis.y, yAxis.z,
            usableRay.x, usableRay.y, usableRay.z,
        });

        for(int i = 0; i < points; i++){
            float angle = (float) Math.toRadians((i * angle_spacing));

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(x * scale.x, y * scale.y, 0).mul(rotationMatrix).add(origin);
            aabb.recalculateFromPoint(thisPoint);

            if(lastPoint != null) {
                out.addDrawData(drawTriangle.drawTriangle(lastPoint, thisPoint, tip, color));
            }else{
                firstPoint = thisPoint;
            }

            lastPoint = thisPoint;
        }

        if(lastPoint != null && firstPoint != null){
            out.addDrawData(drawTriangle.drawTriangle(lastPoint, firstPoint, tip, color));
        }

        out.setAABB(aabb);

        return out;
    }

    public DirectDrawData drawCylinder(Vector3f origin, Vector3f ray, Vector3f scale, int points, Vector3f color) {
        DirectDrawData out = new DirectDrawData();
        AABB aabb = new AABB();

        float angle_spacing = 360.0f / (float)points;

        Vector3f usableRay = new Vector3f(ray);

        Vector3f lastPoint = null;
        Vector3f lastThatPoint = null;
        Vector3f firstPoint = null;
        Vector3f firstThatPoint = null;

        Vector3f up = new Vector3f(0, (float) Math.cos(usableRay.z * Math.PI), (float) Math.cos(usableRay.y * Math.PI));

        Vector3f xAxis = new Vector3f(up).cross(usableRay).normalize();
        Vector3f yAxis = new Vector3f(usableRay).cross(xAxis).normalize();

        Matrix3f rotationMatrix = new Matrix3f().set(new float[]{
                xAxis.x, xAxis.y, xAxis.z,
                yAxis.x, yAxis.y, yAxis.z,
                usableRay.x, usableRay.y, usableRay.z,
        });

        for(int i = 0; i < points; i++){
            float angle = (float) Math.toRadians((i * angle_spacing));

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(x * scale.x, y * scale.y, 0).mul(rotationMatrix).add(origin);
            aabb.recalculateFromPoint(thisPoint);
            Vector3f thatPoint = new Vector3f(x * scale.x, y * scale.y, 0).mul(rotationMatrix).add(origin).add(usableRay);
            aabb.recalculateFromPoint(thatPoint);

            if(lastPoint != null) {
                out.addDrawData(drawTriangle.drawTriangle(lastPoint, thisPoint, thatPoint, color));
                out.addDrawData(drawTriangle.drawTriangle(lastPoint, lastThatPoint, thatPoint, color));
            }else{
                firstPoint = thisPoint;
                firstThatPoint = thatPoint;
            }

            lastPoint = thisPoint;
            lastThatPoint = thatPoint;
        }

        if(lastPoint != null && firstPoint != null){
            out.addDrawData(drawTriangle.drawTriangle(lastPoint, firstPoint, lastThatPoint, color));
            out.addDrawData(drawTriangle.drawTriangle(firstPoint, lastThatPoint, firstThatPoint, color));
        }

        out.setAABB(aabb);

        return out;
    }

    public DirectDrawData drawArrow(Vector3f origin, Vector3f ray, Vector3f scale, int points, Vector3f color) {
        float ratio = scale.z / ray.distance(origin);

        DirectDrawData ddd_cylinder  = this.drawCylinder(origin, new Vector3f(ray).sub(origin).mul(1f - ratio), new Vector3f(scale).mul(0.5f, 0.5f, 1), points, color);
        DirectDrawData ddd_arrowhead = this.drawCone(new Vector3f(ray).mul(1f - ratio).add(new Vector3f(origin).mul(ratio)), new Vector3f(ray).sub(origin).mul(ratio), scale, points, color);

        return ddd_cylinder.merge(ddd_arrowhead);

    }

    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        drawerLine.drawLine(from, to, color);
    }

    public void drawLine(Vector3f from, Vector3f to, Vector4f color) {
        drawerLine.drawLine(from, to, new Vector3f(color.x, color.y, color.z));
    }

    //Color overrides
    public void redrawTriangleColor(DirectDrawData directDrawData, Vector3f color) {
        for(DrawIndex index : directDrawData.getDrawIndices()){
            drawTriangle.recolor(index.colorStart, index.colorLength, color);
        }
    }


    public void postpare(){
        //Render our Skybox
        skyboxRenderer.render();

        //Render our lines!
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            drawerLine.render();
            drawTriangle.render();
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
