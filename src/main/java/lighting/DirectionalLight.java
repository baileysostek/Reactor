package lighting;

import com.google.gson.JsonObject;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import graphics.renderer.DirectDraw;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import particle.ParticleSystem;
import util.Callback;

public class DirectionalLight extends Light {

    private FBO depthBuffer;
    private Matrix4f viewMatrix = new Matrix4f();

    private Callback resize;

    public DirectionalLight(){
        super();
        depthBuffer = new FBO();
        addAttribute(new Attribute<Vector3f>("targetPoint", new Vector3f(0)));

        this.getAttribute("castsShadows").setShouldBeSerialized(false).setData(true);

        resize = new Callback() {
            @Override
            public Object callback(Object... objects) {
                depthBuffer.resize((int)objects[0], (int)objects[1]);
                return null;
            }
        };

        updateViewMatrix();
    }

    @Override
    public void onAdd(){
        Renderer.getInstance().addResizeCallback(resize);
        super.onAdd();
        super.setTexture(depthBuffer.getDepthTexture());
    }

    @Override
    public void onRemove(){
        Renderer.getInstance().removeResizeCallback(resize);
        super.onRemove();
    }

    @Override
    public void update(double delta){
        LightingManager.getInstance().drawFromMyPerspective(this);
        updateViewMatrix();
    }

    public void updateViewMatrix(){
        viewMatrix = new Matrix4f().lookAt(new Vector3f(this.getPosition()), new Vector3f((Vector3f) this.getAttribute("targetPoint").getData()), new Vector3f(0, 1, 0));
    }

    public void setFrustum(float x, float y, float z){
        this.setFrustum(new Vector3f(x, y, z));
    }

    public void setFrustum(Vector3f frustum){
        if(this.hasAttribute("frustum")) {
            this.getAttribute("frustum").setData(frustum);
        }
    }

    @Override
    public void renderInEditor(boolean selected){
        DirectDraw.getInstance().Draw3D.drawArrow(new Vector3f(this.getPosition()), new Vector3f(0, 0, 0).sub(this.getPosition()).normalize().add(this.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, (Vector3f) this.getAttribute("color").getData());

        DirectDraw.getInstance().Draw3D.drawBillboard(new Vector3f(this.getPosition()), new Vector2f(1), (Vector3f) this.getAttribute("color").getData(), LightingManager.getInstance().getSunSVG());

        if(selected) {

            DirectDraw.getInstance().Draw3D.drawArrow(new Vector3f(this.getPosition()), new Vector3f((Vector3fc) this.getAttribute("targetPoint").getData()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, (Vector3f) this.getAttribute("color").getData());

            Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();

            Vector3f CYAN = new Vector3f(0, 1, 1);

            //Rays into space
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

            //Front Face
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

            //Back Face
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().Draw3D.drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        }
    }

    public Matrix4f getLightspaceTransformMatrix() {
        Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();
        return new Matrix4f().ortho(-frustum.x, frustum.x, -frustum.x,frustum.x, frustum.y, frustum.z).mul(viewMatrix);
    }

    public float[] getLightspaceTransform() {
        return getLightspaceTransformMatrix().get(new float[16]);
    }

    public FBO getDepthBuffer() {
        return this.depthBuffer;
    }
}
