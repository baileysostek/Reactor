package lighting;

import entity.component.Attribute;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import util.Callback;

public class SpotLight extends Light implements CastsShadows{

    private FBO depthBuffer;
    private Matrix4f viewMatrix = new Matrix4f();

    private Callback resize;

    public SpotLight(){
        super();
        depthBuffer = new FBO();
        setTexture(depthBuffer.getDepthTexture());
        addAttribute(new Attribute<Vector3f>("targetPoint", new Vector3f(0)));

        this.getAttribute("castsShadows").setShouldBeSerialized(false).setData(true);
        this.getAttribute("textureID").setShouldBeSerialized(false);

        resize = new Callback() {
            @Override
            public Object callback(Object... objects) {
                depthBuffer.resize(Renderer.getWIDTH(), Renderer.getHEIGHT());
                return null;
            }
        };

        Renderer.getInstance().addResizeCallback(resize);

    }

    @Override
    public void onRemove(){
        Renderer.getInstance().removeResizeCallback(resize);
        super.onRemove();
    }

    @Override
    public void update(double delta){
        LightingManager.getInstance().drawFromMyPerspective(this);
        viewMatrix = new Matrix4f().lookAt(new Vector3f(this.getPosition()), new Vector3f((Vector3f) this.getAttribute("targetPoint").getData()), new Vector3f(0, 1, 0));
    }

    @Override
    public void renderInEditor(boolean selected){
        Renderer.getInstance().drawArrow(new Vector3f(this.getPosition()), new Vector3f(0, 0, 0).sub(this.getPosition()).normalize().add(this.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, (Vector3f) this.getAttribute("color").getData());
        Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();

        Vector3f CYAN = new Vector3f(0, 1, 1);

        //Rays into space
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f( frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f( frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

        //Front Face
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f( frustum.x,  frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x, -frustum.x, -frustum.y).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);

        //Back Face
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f(-frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f(-frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
        Renderer.getInstance().drawLine(new Vector3f( frustum.x,  frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), new Vector3f( frustum.x, -frustum.x, -frustum.y -frustum.z).mulTransposeDirection(viewMatrix).add(this.getPosition()), CYAN);
    }

    public float[] getLightspaceTransform() {
        Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();
        return new Matrix4f().perspective((float) Math.toRadians(70), 1, 0.1f,200).mul(viewMatrix).get(new float[16]);
    }

    public FBO getDepthBuffer() {
        return this.depthBuffer;
    }

    @Override
    public Light getLight() {
        return this;
    }
}