package graphics.renderer.reflection;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class ReflectionManager {

    //Projection Constants
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 256f;
    private static final float FOV = 90;
    private static final float ASPECT_RATIO = 1;

    private static ReflectionManager reflectionManager;

    private LinkedList<ReflectionProbe> reflectionProbes = new LinkedList<>();
    private Matrix4f projectionMatrix = new Matrix4f();

    int reflectionShader;

    private ReflectionManager(){
//        reflectionShader = ShaderManager.getInstance().loadShader("sky");
        setProjectionMatrix();
    }

    public void update(double delta){
        //Tell all probes to check what entities contained within themselves that they need to update.
    }

    public void addReflectionProbe(ReflectionProbe probe){
        this.reflectionProbes.add(probe);
    }

    public void removeReflectionProbe(ReflectionProbe probe){
        if(this.reflectionProbes.contains(probe)) {
            this.reflectionProbes.remove(probe);
        }
    }

    public ReflectionProbe getNearestProbe(Vector3f position){
        Collections.sort(reflectionProbes, new Comparator<ReflectionProbe>() {
            @Override
            public int compare(ReflectionProbe o1, ReflectionProbe o2) {
                return 0;
            }
        });
        return reflectionProbes.getFirst();
    }

    public void render(){

    }

    public Matrix4f getProjectionMatrix(){
        return this.projectionMatrix;
    }

    private void setProjectionMatrix() {
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / ASPECT_RATIO;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix.m00(x_scale);
        projectionMatrix.m11(y_scale);
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustum_length));
        projectionMatrix.m23(-1);
        projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustum_length));
        projectionMatrix.m33(0);
    }

    public static void initialize(){
        if(reflectionManager == null){
            reflectionManager = new ReflectionManager();
        }
    }

    public static ReflectionManager getInstance(){
        return reflectionManager;
    }

}
