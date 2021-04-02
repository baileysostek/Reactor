package physics;

import com.bulletphysics.linearmath.IDebugDraw;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import org.joml.Vector4f;

import javax.vecmath.Vector3f;

public class BulletDebugDrawer extends IDebugDraw {
    int debugMode = 0;

    private static final int DEBUG_DRAW_NONE        = 0;
    private static final int DEBUG_DRAW_ORIGIN      = 1;
    private static final int DEBUG_DRAW_AABB        = 2;
    private static final int DEBUG_DRAW_TEST3       = 4;
    private static final int DEBUG_DRAW_TEST4       = 8;
    private static final int DEBUG_DRAW_TEST5       = 16;

    public BulletDebugDrawer(){
        debugMode = DEBUG_DRAW_ORIGIN | DEBUG_DRAW_AABB | DEBUG_DRAW_TEST3 | DEBUG_DRAW_TEST4 | DEBUG_DRAW_TEST5;
    }

    @Override
    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        Vector4f perspectiveFrom    = new Vector4f(from.x, from.y, from.z, 1);
        Vector4f perspectiveTo      = new Vector4f(to.x, to.y, to.z, 1);
//
        DirectDraw.getInstance().Draw3D.drawLine(new org.joml.Vector3f(perspectiveFrom.x, perspectiveFrom.y, perspectiveFrom.z), new org.joml.Vector3f(perspectiveTo.x, perspectiveTo.y, perspectiveTo.z), new org.joml.Vector3f(color.x, color.y, color.z));
    }

    @Override
    public void drawContactPoint(Vector3f vector3f, Vector3f vector3f1, float v, int i, Vector3f vector3f2) {

    }

    @Override
    public void reportErrorWarning(String s) {
        System.out.println("[Bullet]"+s);
    }

    @Override
    public void draw3dText(Vector3f vector3f, String s) {

    }

    @Override
    public void setDebugMode(int i) {
        debugMode = i;
    }

    @Override
    public int getDebugMode() {
        return debugMode;
    }
}
