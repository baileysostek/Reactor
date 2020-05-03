package math;

import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class MatrixUtils {

    public static final float FOV = 70.0f;
    private static final float  NEAR_PLANE = 0.1f;
    private static final float  FAR_PLANE = 1024.0f;

    public static float[] getIdentityMatrix(){
        return new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
    }

    //Translate matrix by an ammount.
    public static float[] translate(float[] mat4,  Vector3f offset){
        mat4[3]  += offset.x();
        mat4[7]  += offset.y();
        mat4[11] += offset.z();
        return mat4;
    }

    //Set translation in space
    public static float[] setTranslation(float[] mat4,  Vector3f offset){
        mat4[3]  = offset.x();
        mat4[7]  = offset.y();
        mat4[11] = offset.z();
        return mat4;
    }

    public static float[] rotateM(float[] floatMatrix, int offset, float rotation, float xAxis, float yAxis, float zAxis){
        Matrix4f matrix4f = new Matrix4f().set(floatMatrix, offset);
        matrix4f.rotate(rotation, xAxis, yAxis, zAxis, matrix4f);
        return matrix4f.get(floatMatrix);
    }

    public static float[] createProjectionMatrix() {
        float aspectRatio = (float) Renderer.getInstance().getWIDTH() / (float) Renderer.getInstance().getHEIGHT();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        return new float[]{
            x_scale, 0, 0, 0,
            0, y_scale, 0, 0,
            0, 0, -((FAR_PLANE + NEAR_PLANE) / frustum_length), -1.0f,
            0, 0, -((2.0f * NEAR_PLANE * FAR_PLANE) / frustum_length), 0
        };
    }

    public static float[] scaleM(float[] matrix, int offset, float scaleX, float scaleY, float scaleZ){
        Matrix4f matrix4f = new Matrix4f().set(matrix, offset);
        matrix4f = matrix4f.scale(scaleX, scaleY, scaleZ);
        return matrix4f.get(matrix);
    }

    public static float[] multiplyMV(float[] out, float[] in, int inOffset, float[] vector, int vectorOffset){
        Matrix4f matrix4f = new Matrix4f().set(in, inOffset);
        Vector4f outVector = new Vector4f();
        matrix4f.transform(vector[0 + vectorOffset], vector[1 + vectorOffset], vector[2 + vectorOffset], vector[3 + vectorOffset], outVector);
        matrix4f.transform(vector[0 + vectorOffset], vector[1 + vectorOffset], vector[2 + vectorOffset], vector[3 + vectorOffset], outVector);
        out = new float[]{outVector.x, outVector.y, outVector.z, outVector.w};
        return out;
    }

    public static float[] multiplyMM(float[] out, float[] in, int inOffset, float[] in2, int inOffset2){
        Matrix4f matrix4f = new Matrix4f().set(in, inOffset);
        Matrix4f matrix4f2 = new Matrix4f().set(in2, inOffset2);

        Matrix4f outMat = new Matrix4f(matrix4f).mul(matrix4f2);
        out = outMat.get(out);
        return out;
    }

}
