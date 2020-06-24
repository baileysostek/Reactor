package math;

import camera.Camera;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Maths {
    public static Matrix4f createTransformationMatrix(Vector3f position, Vector3f rotation, float scale){
        Matrix4f matrix = new Matrix4f().identity();
        matrix.translate(position);
        matrix.rotate((float)Math.toRadians(rotation.x()), new Vector3f(1,0,0),matrix);
        matrix.rotate((float)Math.toRadians(rotation.y()), new Vector3f(0,1,0),matrix);
        matrix.rotate((float)Math.toRadians(rotation.z()), new Vector3f(0,0,1),matrix);
        matrix.scale(scale);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f position, Vector3f rotation, Vector2f scale){
        Matrix4f matrix = new Matrix4f().identity();
        matrix.translate(position);
        matrix.rotate((float)Math.toRadians(rotation.x()), new Vector3f(1,0,0),matrix);
        matrix.rotate((float)Math.toRadians(rotation.y()), new Vector3f(0,1,0),matrix);
        matrix.rotate((float)Math.toRadians(rotation.z()), new Vector3f(0,0,1),matrix);
        matrix.scale(scale.x, scale.y, 1);
        return matrix;
    }

    public static Matrix4f createProjectionMatrix(float FOV, float FAR_PLANE, float NEAR_PLANE) {
        float aspectRatio = (float) Renderer.getInstance().getWIDTH() / (float) Renderer.getInstance().getHEIGHT();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        Matrix4f projectionMatrix = new Matrix4f().set(new float[]{
            x_scale, 0, 0, 0,
            0, y_scale, 0, 0,
            0, 0, -((FAR_PLANE + NEAR_PLANE) / frustum_length), -1.0f,
            0, 0, -((2.0f * NEAR_PLANE * FAR_PLANE) / frustum_length), 0
        });

        return projectionMatrix;
    }

    public static Matrix4f createViewMatrix(Camera camera){
        Matrix4f matrix = new Matrix4f().identity();
//        matrix.rotate
        Vector3f offset = new Vector3f(camera.getPosition());
        offset.mul(new Vector3f(-1, -1, -1));
        matrix.translate(offset);
        return matrix;
    }
}
