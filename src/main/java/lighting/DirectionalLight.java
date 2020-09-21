package lighting;

import entity.component.Attribute;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class DirectionalLight extends Light {

    private FBO depthBuffer;
    private Matrix4f viewMatrix = new Matrix4f();

    public DirectionalLight(){
        depthBuffer = new FBO();
        setTexture(depthBuffer.getDepthTexture());
        addAttribute(new Attribute<Vector3f>("targetPoint", new Vector3f(0)));
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
        return new Matrix4f().ortho(-frustum.x, frustum.x, -frustum.x,frustum.x, frustum.y, frustum.z).mul(viewMatrix).get(new float[16]);
    }

    public FBO getDepthBuffer() {
        return this.depthBuffer;
    }
}
