package math;

import camera.Camera;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Maths {

    public static Matrix4f createViewMatrix(Camera camera){
        Matrix4f matrix = new Matrix4f().identity();
//        matrix.rotate
        Vector3f offset = new Vector3f(camera.getPosition());
        offset.mul(new Vector3f(-1, -1, -1));
        matrix.translate(offset);
        return matrix;
    }
}
