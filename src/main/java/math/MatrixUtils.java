package math;

import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class MatrixUtils {

    public static float[] getIdentityMatrix(){
        return new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
    }

    public static float[] rotateM(float[] floatMatrix, int offset, float rotation, float xAxis, float yAxis, float zAxis){
        Matrix4f matrix4f = new Matrix4f().set(floatMatrix, offset);
        matrix4f.rotate(rotation, xAxis, yAxis, zAxis, matrix4f);
        return matrix4f.get(floatMatrix);
    }

    public static float[] createProjectionMatrix() {
        return new Matrix4f().perspective(Renderer.FOV, Renderer.getInstance().getAspectRatio(), Renderer.NEAR_PLANE, Renderer.FAR_PLANE).get(new float[16]);
    }

    public static float[] createProjectionMatrix(float FOV, float aspectRatio, float near, float far) {
        Matrix4f out = new Matrix4f();

        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / aspectRatio;
        float frustumLength = far - near;

        out.m00(x_scale);
        out.m11(y_scale);
        out.m22(-((far + near) / frustumLength));
        out.m23(-1);
        out.m32(-((2 * near * far) / frustumLength));
        out.m33(0);

        return out.get(new float[16]);
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
