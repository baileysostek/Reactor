package math;

import camera.CameraManager;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.text.DecimalFormat;
import java.util.Collection;

public class VectorUtils {

    private static DecimalFormat df = new DecimalFormat("#.####");

    public static String format(float f){
        return df.format(f);
    }

    public static float maxComponent(Vector3f size){
        float biggestDimension = Math.max(Math.max(size.x, size.y), size.z) / 2f;

        return biggestDimension;
    }

    public static Vector3f transform(Vector3f vec, Matrix4f transform){
        Vector4f vec4 = new Vector4f(vec, 1);
        vec4 = transform.transform(vec4);
        return new Vector3f(vec4.x, vec4.y, vec4.z).div(vec4.w);
    }

    public static Vector2f worldToScreen(Vector3f worldPosition) {
        Vector4f worldspace = new Vector4f(worldPosition, 1.0f);
        Vector4f postProjection = new Matrix4f(CameraManager.getInstance().getActiveCamera().getTransformationMatrix()).mul(new Matrix4f().set(Renderer.getInstance().getProjectionMatrix())).transform(worldspace);
        return new Vector2f(postProjection.x / postProjection.w, postProjection.y / postProjection.w);
    }

//    public static float[] streamToFloatArray(Collection<Vector2f> input){
//        return streamToFloatArray((Vector2f[]) input.toArray());
//    }

    public static float[] streamToFloatArray(Vector2f[] input){
        float[] out = new float[input.length * 2];
        int index = 0;
        for(Vector2f vec : input){
            out[index * 2 + 0] = vec.x;
            out[index * 2 + 1] = vec.y;
            index++;
        }
        return out;
    }

//    public static float[] streamToFloatArray(Collection<Vector3f> input){
//        return streamToFloatArray((Vector3f[]) input.toArray());
//    }

    public static float[] streamToFloatArray(Vector3f[] input){
        float[] out = new float[input.length * 3];
        int index = 0;
        for(Vector3f vec : input){
            out[index * 3 + 0] = vec.x;
            out[index * 3 + 1] = vec.y;
            out[index * 3 + 2] = vec.z;
            index++;
        }
        return out;
    }

}
