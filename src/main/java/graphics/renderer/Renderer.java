package graphics.renderer;

import camera.CameraManager;
import engine.Engine;
import engine.FraudTek;
import entity.Entity;
import entity.EntityManager;
import lighting.DirectionalLight;
import lighting.Light;
import lighting.LightingManager;
import math.MatrixUtils;
import models.AABB;
import models.Joint;
import models.Model;
import org.joml.*;
import org.lwjgl.opengl.*;
import platform.EnumDevelopment;
import platform.PlatformManager;
import util.Callback;
import util.StopwatchManager;

import java.lang.Math;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Renderer extends Engine {

    private static Renderer renderer;
    private static int WIDTH;
    private static int HEIGHT;

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 1024.0f;
    //Reserve the first 7 textures for PBR
    public static final int TEXTURE_OFFSET = 7;

    private float aspectRatio = 1.0f;

    public static int PIXELS_PER_METER = 16;

    private static int RENDER_TYPE = GL46.GL_TRIANGLES;

    private int shaderID = 0;

    private FBO frameBuffer;

    private static float[] projectionMatrix = new float[16];

    //Skybox
    private SkyboxRenderer skyboxRenderer;

    //ImmediateDraw
    private ImmediateDrawLine     drawerLine;
    private ImmediateDrawTriangle drawTriangle;
    private ImmediateDrawSprite   drawSprite;

    private LinkedList<Callback> resizeCallbacks = new LinkedList<>();

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
        GL46.glEnable(GL46.GL_CULL_FACE);
        GL46.glCullFace(GL46.GL_BACK);

        System.out.println("Shader ID:"+shaderID);

        frameBuffer = new FBO(width, height);

        skyboxRenderer = new SkyboxRenderer();

        drawerLine = new ImmediateDrawLine();
        drawTriangle = new ImmediateDrawTriangle();
        drawSprite = new ImmediateDrawSprite();

    }

    public static void initialize(int width, int height){
        if(renderer == null){
            renderer = new Renderer(width, height);
            projectionMatrix = MatrixUtils.createProjectionMatrix();
            GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(renderer.shaderID, "perspective"),false, projectionMatrix);
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

        GL46.glViewport(0, 0, width, height);

        //Now itterate through callbacks
        for(Callback c : resizeCallbacks){
            c.callback(width, height);
        }
    }

    public void render(){
        //TODO Cleanup, This code should live in the Shader class, and also the  PRojection Matrix should be beuffered on resize and not regenerated per frame, only regen on screen resize.
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.DEVELOPMENT)) {
            frameBuffer.bindFrameBuffer();
        }

        GL46.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(shaderID);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

//        StopwatchManager.getInstance().getTimer("uploadUniforms").start();

        ShaderManager.getInstance().loadUniformIntoActiveShader("cameraPos", CameraManager.getInstance().getActiveCamera().getPosition());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "perspective"),false, projectionMatrix);

        //Pos -2 is skybox
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + 5);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, skyboxRenderer.getTextureID());
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "skybox"), 5);

        //-1 is closest probe to entity

        //Compute per frame, refactor to per entity eventually
        LinkedList<DirectionalLight> shadowCasters = LightingManager.getInstance().getClosestLights(5, new Vector3f(0));
        for(int directionalLightIndex = 0; directionalLightIndex < Math.min(shadowCasters.size(), GL46.glGetInteger(GL46.GL_MAX_TEXTURE_IMAGE_UNITS) - TEXTURE_OFFSET); directionalLightIndex++){
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

        int index = 0;
        for(Light l : LightingManager.getInstance().getClosestPointLights(4, new Vector3f(0))){
            ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightPosition", index, l.getPosition());
            ShaderManager.getInstance().loadUniformIntoActiveShaderArray("lightColor", index, l.getColor());
            index++;
        }
//        StopwatchManager.getInstance().getTimer("uploadUniforms").stop();

//        StopwatchManager.getInstance().getTimer("sort").start();
        EntityManager.getInstance().resort();
//        StopwatchManager.getInstance().getTimer("sort").stop();


        //Render all entities
//        StopwatchManager.getInstance().getTimer("drawCalls").start();
        int lastID = -1;
        int lastTexture = -1;
        int loads = 0;
        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {
//                StopwatchManager.getInstance().getTimer("uploadUniforms").lapStart();
                if (entity.getModel().getID() != lastID) {
                    ShaderManager.getInstance().loadHandshakeIntoShader(shaderID, entity.getModel().getHandshake());

                    drawBones(entity);

                    loads++;
                }

                if (lastTexture != entity.getTextureID()) {
                    GL46.glActiveTexture(GL46.GL_TEXTURE0);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, entity.getTextureID());
                    GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "textureID"), 0);
                    lastTexture = entity.getTextureID();
                }

                if(entity.hasAttribute("normalID")){
                    GL46.glActiveTexture(GL46.GL_TEXTURE0 + 1);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, (Integer) entity.getAttribute("normalID").getData());
                    GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "normalID"), 1);
                }

                if(entity.hasAttribute("metallicID")){
                    GL46.glActiveTexture(GL46.GL_TEXTURE0 + 2);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, (Integer) entity.getAttribute("metallicID").getData());
                    GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "metallicID"), 2);
                }

                if(entity.hasAttribute("roughnessID")){
                    GL46.glActiveTexture(GL46.GL_TEXTURE0 + 3);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, (Integer) entity.getAttribute("roughnessID").getData());
                    GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "roughnessID"), 3);
                }

                if(entity.hasAttribute("aoID")){
                    GL46.glActiveTexture(GL46.GL_TEXTURE0 + 4);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, (Integer) entity.getAttribute("aoID").getData());
                    GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "aoID"), 4);
                }

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

                lastID = entity.getModel().getID();
            }
        }
    }

    //Quads and billboard
    public void drawBillboard(Vector3f pos, Vector2f scale, int texture){
        drawSprite.drawSprite(pos, texture);
    }

    public void drawBones(Entity entity) {
        if(entity.getModel() != null) {
            if(entity.getModel().rootJoint != null) {
                HashMap<String, Matrix4f> frames = entity.getModel().getAnimatedBoneTransforms();
                drawBonesHelper(entity.getModel().getRootJoint(), entity.getTransform(), frames);
            }
        }
    }

    private void drawBonesHelper(Joint root, Matrix4f parentTransform, HashMap<String, Matrix4f> frames) {
        if(frames.containsKey(root.getName())) {
            Matrix4f animationOffset = frames.get(root.getName());
            Matrix4f currentTransform = new Matrix4f(parentTransform).mul(animationOffset);
            for (Joint childJoint : root.getChildren()) {
                drawBonesHelper(childJoint, currentTransform, frames);
            }
            Vector4f parentPos = new Vector4f(0, 0, 0, 1).mul(parentTransform);
            Vector4f childPos = new Vector4f(0, 0, 0, 1).mul(currentTransform);
//            currentTransform = currentTransform.mul(root.getInverseBindTransform());
            drawLine(new Vector3f(parentPos.x, parentPos.y, parentPos.z), new Vector3f(childPos.x, childPos.y, childPos.z), new Vector3f(1));
        }
    }

//    private void drawBonesHelper(Joint root, Matrix4f entityTransform, HashMap<String, Matrix4f> frames) {
//        Matrix4f animationOffset = frames.get(root.getName());
////        .mul(new Matrix4f(animationOffset).invert())
//        Vector4f parentPos = new Vector4f(0, 0, 0, 1).mul(root.getInverseBindTransform()).mul(entityTransform);
//        for(Joint childJoint : root.getChildren()){
//            Vector4f childPos = new Vector4f(0, 0, 0, 1).mul(childJoint.getInverseBindTransform()).mul(entityTransform);
////            drawCone(new Vector3f(parentPos.x, parentPos.y, parentPos.z), new Vector3f(childPos.x, childPos.y, childPos.z), new Vector3f(0.125f), 13, new Vector3f(1));
//            drawLine(new Vector3f(parentPos.x, parentPos.y, parentPos.z), new Vector3f(childPos.x, childPos.y, childPos.z), new Vector3f(1));
//            drawBonesHelper(childJoint, entityTransform, frames);
//        }
//    }


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
//            GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT);
            drawerLine.render();
            drawTriangle.render();
            drawSprite.render();
            frameBuffer.unbindFrameBuffer();
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
}
