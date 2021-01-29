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
import particle.ParticleSystem;
import util.Callback;

public class DirectionalLight extends Light {

    private FBO depthBuffer;
    private Matrix4f viewMatrix = new Matrix4f();

    private Callback resize;

    public DirectionalLight(){
        super();
        depthBuffer = new FBO();
        setTexture(depthBuffer.getDepthTexture());
        addAttribute(new Attribute<Vector3f>("targetPoint", new Vector3f(0)));

        this.getAttribute("castsShadows").setShouldBeSerialized(false).setData(true);
        super.setTexture(SpriteBinder.getInstance().loadSVG("engine/svg/sun.svg", 1, 1, 96f));

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
        DirectDraw.getInstance().drawArrow(new Vector3f(this.getPosition()), new Vector3f(0, 0, 0).sub(this.getPosition()).normalize().add(this.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, (Vector3f) this.getAttribute("color").getData());

        DirectDraw.getInstance().drawBillboard(new Vector3f(this.getPosition()), new Vector2f(1), 0);

        if(selected) {
            Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();

            Vector3f CYAN = new Vector3f(0, 1, 1);

            //Rays into space
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

            //Front Face
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

            //Back Face
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(-frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
            DirectDraw.getInstance().drawLine(new Vector3f(frustum.x, frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(frustum.x, -frustum.x, -frustum.y - frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        }
    }

    public float[] getLightspaceTransform() {
        Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();
        return new Matrix4f().ortho(-frustum.x, frustum.x, -frustum.x,frustum.x, frustum.y, frustum.z).mul(viewMatrix).get(new float[16]);
    }

    public FBO getDepthBuffer() {
        return this.depthBuffer;
    }
}
