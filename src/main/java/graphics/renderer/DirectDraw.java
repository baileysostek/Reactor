package graphics.renderer;

import editor.Editor;
import engine.Reactor;
import entity.Entity;
import entity.EntityManager;
import graphics.ui.UIManager;
import math.VectorUtils;
import models.AABB;
import models.Joint;
import org.joml.*;
import org.lwjgl.opengl.GL46;
import platform.EnumDevelopment;
import platform.PlatformManager;

import java.lang.Math;
import java.util.HashMap;
import java.util.LinkedList;

public class DirectDraw {
    private static DirectDraw directDraw;

    //ImmediateDraw
    private ImmediateDrawLine     drawerLine;
    private ImmediateDrawTriangle drawTriangle;
    private ImmediateDrawSprite   drawSprite;

    private DirectDraw(){
        //Setup our draw instances.

        drawerLine = new ImmediateDrawLine();
        drawTriangle = new ImmediateDrawTriangle();
        drawSprite = new ImmediateDrawSprite();

    }

    /*
        Billboard / Sprite
     */
    public void drawBillboard(Vector3f pos, Vector2f scale, int texture){
        drawSprite.drawBillboard(pos, scale, texture);
    }

    public void drawBillboard(Vector3f pos, Vector2f scale, Vector3f color, int texture){
        drawSprite.drawBillboard(pos, scale, color, texture);
    }

    /*
        Draw Bones
     */
    public void drawBones(Entity entity) {
        if(entity.hasModel()) {
            if(entity.getModel().rootJoint != null) {
                //TODO animation timeline
                for(float i = 0; i < 1; i += 0.1f){
                    HashMap<String, Joint> frames = entity.getModel().getAnimatedBoneTransforms("animation", i);
                    drawBonesHelper(entity.getModel().getRootJoint(), entity.getTransform(), frames);
                }
            }
        }
    }

    private void drawBonesHelper(Joint root, Matrix4f parentTransform, HashMap<String, Joint> frames) {
        if(frames.containsKey(root.getName())) {
            Matrix4f animationOffset = frames.get(root.getName()).getAnimationTransform();
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

    /*
        Draw AABB
     */

    public void drawAABB(AABB aabb) {
        this.drawAABB(aabb.getMIN(), aabb.getMAX(), new Vector3f(1));
    }

    public void drawAABB(Entity entity, Vector3f color) {
        org.joml.Vector3f[] aabb = entity.getAABB();
        org.joml.Vector3f min = aabb[0];
        org.joml.Vector3f max = aabb[1];

        //Check for children.
        if(EntityManager.getInstance().entityHasChildren(entity)) {
            LinkedList<Entity> children = EntityManager.getInstance().getEntitiesChildren(entity);
            for (Entity child : children) {
                this.drawAABB(child, new Vector3f(0, 1, 1));
            }
        }

        this.drawAABB(min, max, color);
    }

    public void drawAABB(Vector3f min, Vector3f max, Vector3f color) {
        //6 draw calls...
        this.drawLine(min, new Vector3f(max.x, min.y, min.z) , color);
        this.drawLine(min, new Vector3f(min.x, max.y, min.z) , color);
        this.drawLine(min, new Vector3f(min.x, min.y, max.z) , color);

        this.drawLine(max, new Vector3f(min.x, max.y, max.z) , color);
        this.drawLine(max, new Vector3f(max.x, min.y, max.z) , color);
        this.drawLine(max, new Vector3f(max.x, max.y, min.z) , color);

        //Bottom
        this.drawLine(new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, max.y, min.z) , color);
        this.drawLine(new Vector3f(min.x, max.y, min.z), new Vector3f(min.x, max.y, max.z) , color);

        this.drawLine(new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, min.y, min.z) , color);
        this.drawLine(new Vector3f(max.x, min.y, max.z), new Vector3f(min.x, min.y, max.z) , color);

        this.drawLine(new Vector3f(max.x, min.y, min.z), new Vector3f(max.x, max.y, min.z) , color);
        this.drawLine(new Vector3f(min.x, min.y, max.z), new Vector3f(min.x, max.y, max.z) , color);

    }

    /*
        Draw Ring
     */

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

    public DirectDrawData drawRing(Vector3f origin, Vector3f scale, Vector3f normal, Vector2i resolution, float arc, float offset, Vector3f color) {
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

//        rotationMatrix.rotate(new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), offset));

        Vector3f lastPoint = null;
        Vector3f[] lastRing = new Vector3f[resolution.y];
        Vector3f firstPoint = null;
        Vector3f[] firstRing = new Vector3f[resolution.y];
        for(int i = 0; i < resolution.x; i++){
            float angleDegrees = (i * angle_spacing) + offset;
            float angle = (float) Math.toRadians(angleDegrees);

            float x = (float) (Math.cos(angle));
            float y = (float) (Math.sin(angle));

            Vector3f thisPoint = new Vector3f(x * scale.x, y * scale.y, 0);
            thisPoint = thisPoint.mul(rotationMatrix);
            thisPoint.add(origin);

            Vector3f[] thisRing = calculateRingPoints(thisPoint, new Vector2f(scale.z), new Vector3f(biTangent).rotate(new Quaternionf().fromAxisAngleDeg(normal, angleDegrees)), resolution.y, new Vector3f(0, 0, 1));

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

        //Connect arc.
        if(arc >= 360) {
            if (lastPoint != null && firstPoint != null) {
                //We can tessellate between the last ring and this ring.
                for (int j = 0; j < resolution.y; j++) {
                    int negativeIndex = j - 1;
                    if (negativeIndex < 0) {
                        negativeIndex = resolution.y - 1;
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

    /*
        Draw Cone
     */

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

    /*
        Draw Cylender
     */

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

    /*
        Draw Arrow
     */

    public DirectDrawData drawArrow(Vector3f origin, Vector3f ray, Vector3f scale, int points, Vector3f color) {
        float ratio = scale.z / ray.distance(origin);

        DirectDrawData ddd_cylinder  = this.drawCylinder(origin, new Vector3f(ray).sub(origin).mul(1f - ratio), new Vector3f(scale).mul(0.5f, 0.5f, 1), points, color);
        DirectDrawData ddd_arrowhead = this.drawCone(new Vector3f(ray).mul(1f - ratio).add(new Vector3f(origin).mul(ratio)), new Vector3f(ray).sub(origin).mul(ratio), scale, points, color);

        return ddd_cylinder.merge(ddd_arrowhead);

    }

    /*
        Draw Line
     */

    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        drawerLine.drawLine(from, to, color);
    }

    public void drawLine(Vector3f from, Vector3f to, Vector4f color) {
        drawerLine.drawLine(from, to, new Vector3f(color.x, color.y, color.z));
    }


    /*
        Helpers, Redraw triangle.
     */

    //Color overrides
    public void redrawTriangleColor(DirectDrawData directDrawData, Vector3f color) {
        DrawIndex[] indices = directDrawData.getDrawIndices().toArray(new DrawIndex[]{});
        for(DrawIndex index : indices){
            drawTriangle.recolor(index.colorStart, index.colorLength, color);
        }
    }


    //Draw
    protected void render(){
        drawerLine.render();
        if(Reactor.isDev()) {
            GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT);
        }
        drawTriangle.render();
        drawSprite.render();
    }

    //Singleton stuff.
    public static void initialize(){
        if(directDraw == null){
            directDraw = new DirectDraw();
        }
    }

    public static DirectDraw getInstance(){
        return directDraw;
    }

}
