package graphics.renderer;

import camera.CameraManager;
import engine.Engine;
import engine.FraudTek;
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
import java.util.LinkedList;

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

        ShaderManager.getInstance().loadUniformIntoActiveShader("sunAngle", new Vector3f(0).sub(FraudTek.sun.getPosition()).normalize());
        ShaderManager.getInstance().loadUniformIntoActiveShader("lightSpaceMatrix", FraudTek.sun.getLightspaceTransform());

        float[] out = new float[3];

        out[0] = CameraManager.getInstance().getActiveCamera().getLookingDirection().x() * -1.0f;
        out[1] = CameraManager.getInstance().getActiveCamera().getLookingDirection().y() * -1.0f;
        out[2] = CameraManager.getInstance().getActiveCamera().getLookingDirection().z() * -1.0f;

        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "inverseCamera"), out);

        //Render calls from the loaded scene.
//        GL20.glUniform3fv(GL20.glGetUniformLocation(shaderID, "sunAngle"), new float[]{1, 0, 0});


        //Render all entities
        int lastID = -1;
        int lastTexture = -1;
        int loads = 0;
        EntityManager.getInstance().resort();
        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {
                if (entity.getModel().getID() != lastID) {
                    ShaderManager.getInstance().loadHandshakeIntoShader(shaderID, entity.getModel().getHandshake());
                    loads++;
                }

                if (lastTexture != entity.getTextureID()) {
                    GL20.glActiveTexture(GL20.GL_TEXTURE0);
                    GL20.glBindTexture(GL20.GL_TEXTURE_2D, entity.getTextureID());
                    GL20.glUniform1i(GL20.glGetUniformLocation(shaderID, "textureID"), 0);
                    lastTexture = entity.getTextureID();
                }
                GL20.glActiveTexture(GL20.GL_TEXTURE0 + 2);
                GL20.glBindTexture(GL20.GL_TEXTURE_2D, FraudTek.sun.getDepthBuffer().getDepthTexture());
                GL20.glUniform1i(GL20.glGetUniformLocation(shaderID, "shadowMap"), 2);

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

                //Mess with uniforms
                GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderID, "transformation"), false, entity.getTransform().get(new float[16]));
                GL20.glDrawArrays(RENDER_TYPE, 0, entity.getModel().getNumIndicies());

                lastID = entity.getModel().getID();
            }
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

    public void drawRing(Vector3f origin, Vector2f radius, Vector3f normal, int points, Vector3f color) {
        float angle_spacing = 360.0f / (float)points;

        Vector3f usableRay = new Vector3f(normal);

        Vector3f up = new Vector3f(0, (float) Math.cos(usableRay.z * Math.PI), (float) Math.cos(usableRay.y * Math.PI));

        Vector3f xAxis = new Vector3f(up).cross(usableRay).normalize();
        Vector3f yAxis = new Vector3f(usableRay).cross(xAxis).normalize();

        Matrix3f rotationMatrix = new Matrix3f().set(new float[]{
            xAxis.x, xAxis.y, xAxis.z,
            yAxis.x, yAxis.y, yAxis.z,
            usableRay.x, usableRay.y, usableRay.z,
        });

        Vector3f lastPoint = null;
        Vector3f firstPoint = null;
        for(int i = 0; i < points; i++){
            float angle = (float) Math.toRadians((i * angle_spacing));

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(x * radius.x, y * radius.y, 0);
            thisPoint = thisPoint.mul(rotationMatrix);
            thisPoint.add(origin);

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

    public DirectDrawData drawRing(Vector3f origin, Vector3f scale, Vector3f normal, Vector2i resolution, float arc, Vector3f color) {
        DirectDrawData out = new DirectDrawData();
        AABB aabb = new AABB();

        float angle_spacing = arc / (float)resolution.x;

        Vector3f usableRay = new Vector3f(normal);

        Vector3f up = new Vector3f(0, (float) Math.cos(usableRay.z * Math.PI), (float) Math.cos(usableRay.y * Math.PI));

        Vector3f xAxis = new Vector3f(up).cross(usableRay).normalize();
        Vector3f yAxis = new Vector3f(usableRay).cross(xAxis).normalize();

        Matrix3f rotationMatrix = new Matrix3f().set(new float[]{
            xAxis.x, xAxis.y, xAxis.z,
            yAxis.x, yAxis.y, yAxis.z,
            usableRay.x, usableRay.y, usableRay.z,
        });

        Vector3f tangent = new Vector3f((float) (Math.cos(0)) * scale.x, (float) (Math.sin(0)) * scale.y, 0).mul(rotationMatrix).normalize();
        Vector3f biTangent = new Vector3f(tangent).cross(new Vector3f(normal));

        Vector3f lastPoint = null;
        Vector3f[] lastRing = new Vector3f[resolution.y];
        Vector3f firstPoint = null;
        Vector3f[] firstRing = new Vector3f[resolution.y];
        for(int i = 0; i < resolution.x; i++){
            float angle = (float) Math.toRadians((i * angle_spacing));

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(x * scale.x, y * scale.y, 0);
            thisPoint = thisPoint.mul(rotationMatrix);
            thisPoint.add(origin);

            Vector3f[] thisRing = calculateRingPoints(thisPoint, new Vector2f(scale.z), new Vector3f(biTangent).rotate(new Quaternionf().fromAxisAngleDeg(normal, i * angle_spacing)), resolution.y, new Vector3f(0, 0, 1));

            if(lastPoint == null) {
                firstRing = thisRing;
                firstPoint = thisPoint;
            }else{
                //We can tessellate between the last ring and this ring.
                for(int j = 0; j < resolution.y; j++){
                    int negativeIndex = j - 1;
                    if(negativeIndex < 0){
                        negativeIndex = resolution.y -1;
                    }
                    Vector3f p1 = thisRing[negativeIndex];
                    Vector3f p2 = thisRing[j];
                    Vector3f p3 = lastRing[negativeIndex];
                    Vector3f p4 = lastRing[j];

                    aabb.recalculateFromPoint(p1);
                    aabb.recalculateFromPoint(p2);
                    aabb.recalculateFromPoint(p3);
                    aabb.recalculateFromPoint(p4);

                    out.addDrawData(drawTriangle.drawTriangle(p1, p2, p3, color));
                    out.addDrawData(drawTriangle.drawTriangle(p3, p2, p4, color));

                }
            }

            lastPoint = thisPoint;
            lastRing = thisRing;
        }

        if(lastPoint != null && firstPoint != null){
            //We can tessellate between the last ring and this ring.
            for(int j = 0; j < resolution.y; j++){
                int negativeIndex = j - 1;
                if(negativeIndex < 0){
                    negativeIndex = resolution.y -1;
                }
                Vector3f p1 = firstRing[negativeIndex];
                Vector3f p2 = firstRing[j];
                Vector3f p3 = lastRing[negativeIndex];
                Vector3f p4 = lastRing[j];

                aabb.recalculateFromPoint(p1);
                aabb.recalculateFromPoint(p2);
                aabb.recalculateFromPoint(p3);
                aabb.recalculateFromPoint(p4);

                out.addDrawData(drawTriangle.drawTriangle(p1, p2, p3, color));
                out.addDrawData(drawTriangle.drawTriangle(p3, p2, p4, color));
            }
        }

        out.setAABB(aabb);

        return out;
    }

    private Vector3f[] calculateRingPoints(Vector3f origin, Vector2f radius, Vector3f normal, int points, Vector3f color) {
        Vector3f[] out = new Vector3f[points];
        float angle_spacing = 360.0f / (float)points;

        Vector3f usableRay = new Vector3f(normal);

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

            Vector3f thisPoint = new Vector3f(x * radius.x, y * radius.y, 0);
            thisPoint = thisPoint.mul(rotationMatrix);
            thisPoint.add(origin);

            out[i] = thisPoint;
        }

        return out;
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
        DrawIndex[] indices = directDrawData.getDrawIndices().toArray(new DrawIndex[]{});
        for(DrawIndex index : indices){
            drawTriangle.recolor(index.colorStart, index.colorLength, color);
        }
    }


    public void postpare(){
        //Render our Skybox
        skyboxRenderer.render();

        //Render our lines!
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            drawerLine.render();
//            GL32.glClear(GL32.GL_DEPTH_BUFFER_BIT);
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
