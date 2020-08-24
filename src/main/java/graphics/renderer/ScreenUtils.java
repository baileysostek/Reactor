package graphics.renderer;
import camera.Camera;
import math.MatrixUtils;

public class ScreenUtils {
    public static float[] screenToGL(float x, float y){
        float percentX = x / (float) Renderer.getInstance().getWIDTH();
        float percentY = y / (float) Renderer.getInstance().getHEIGHT();
        percentX *= 2f;
        percentY *= 2f;
        percentX -= 1f;
        percentY -= 1f;
        return new float[]{percentX, percentY};
    }

    public static float[] glToWorld(float[] screen, Camera camera){
        float[] forward = {0, 0, -1, 1}; //Worldspace direction

        float aspectRatio = Renderer.getInstance().getAspectRatio();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(Renderer.FOV / 2f))));
        float x_scale = y_scale / aspectRatio;

        float[] touchMatrix = MatrixUtils.getIdentityMatrix();
        System.out.println("xScale:" + x_scale + " yScale:"+y_scale + " ar:"+aspectRatio);
        touchMatrix = MatrixUtils.rotateM(touchMatrix, 0, (screen[0] - (0.16f)) * -1f * (Renderer.FOV / 0.95f), 0f,0f, 1f); //X
        touchMatrix = MatrixUtils.rotateM(touchMatrix, 0, screen[1] * -1f * (Renderer.FOV / aspectRatio), 1f,0f, 0f); //Y

        float[] out     = {0, 0, 0, 0};
        out = MatrixUtils.multiplyMV(out, camera.getTransform(), 0, forward, 0);
        out = MatrixUtils.multiplyMV(out, touchMatrix, 0, out, 0);

        //Invert X axis
        out[0] *= -1.0f;

        return out;
    }
}
