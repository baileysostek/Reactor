package reflection;

import camera.Camera;
import camera.CameraManager;
import camera.DynamicCamera;
import entity.Entity;
import entity.EntityManager;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import graphics.renderer.DirectDraw;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import lighting.LightingManager;
import math.MatrixUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;
import skybox.SkyboxManager;
import util.Callback;

public class Probe extends Entity {
    int resolution = 512;
    int cubeMapTextureID;
    int cubeFBO;
    int cubeRBO;

    float[] projectionMatrix = MatrixUtils.createProjectionMatrix(90, 1, 0.1f, 1024f);

    Attribute<Float> rotation = new Attribute<Float>("Rotation scale", 0f).setType(EnumAttributeType.SLIDERS);

    private Attribute<Callback> redraw   = new Attribute("redraw", new Callback() {
        @Override
        public Object callback(Object... objects) {
            Probe.this.updateProbe();
            return null;
        }
    });

    public Probe(){
        cubeMapTextureID = SpriteBinder.getInstance().generateCubeMap(resolution);

        //PBR setup framebuffer used to blur our images
        cubeFBO = GL46.glGenFramebuffers();
        cubeRBO = GL46.glGenRenderbuffers();
        EntityManager.getInstance();
    }

    @Override
    public void onAdd(){
        updateProbe();
        this.addAttribute(redraw);
        this.addAttribute(rotation);
        ProbeManager.getInstance().addProbe(this);
    }

    @Override
    public void onRemove(){
        ProbeManager.getInstance().removeProbe(this);
}

    public void updateProbe(){
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, cubeFBO);
        GL46.glDrawBuffer(GL46.GL_COLOR_ATTACHMENT0);
        GL46.glBindRenderbuffer(GL46.GL_RENDERBUFFER, cubeRBO);
        GL46.glRenderbufferStorage(GL46.GL_RENDERBUFFER, GL46.GL_DEPTH_COMPONENT24, resolution, resolution);
        GL46.glFramebufferRenderbuffer(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_ATTACHMENT, GL46.GL_RENDERBUFFER, cubeRBO);
        GL46.glViewport(0, 0, resolution, resolution);
        for(int i = 0; i < 6; i++){
            DynamicCamera camera = new DynamicCamera();
            GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubeMapTextureID, 0);
            camera.setPosition(new Vector3f(0));
            setCameraViewFromIndex(camera, i);
            camera.setPosition(new Vector3f(this.getPosition()));
            Renderer.getInstance().renderWorld(camera, projectionMatrix);
        }
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
        GL46.glViewport(0, 0, Renderer.getWIDTH(), Renderer.getHEIGHT());
    }

    private void setCameraViewFromIndex(Camera camera, int index){
//        String[] names = new String[]{"RIGHT", "LEFT", "TOP", "BOTTOM", "BACK", "FRONT"};
        switch (index){
            case 0:{ //RIGHT
                camera.setRotation(new Vector3f(0, rotation.getData() - 90, 0));
                break;
            }
            case 1:{ //LEFT
                camera.setRotation(new Vector3f(0, rotation.getData() + 90, 0));
                break;
            }
            case 2:{ //TOP
                camera.setRotation(new Vector3f(90, 0, 0));
                camera.rotate(new Quaternionf(0, 1, 0, (float) Math.toRadians(-rotation.getData())));
                break;
            }
            case 3:{ //BOTTOM
                camera.setRotation(new Vector3f(-90, rotation.getData() + 180, 0));
                break;
            }
            case 4:{ //BACK
                camera.setRotation(new Vector3f(0, rotation.getData() - 180, 0));
                break;
            }
            case 5:{
                camera.setRotation(new Vector3f(0, rotation.getData(), 0));
                break;
            }
        }
    }

    @Override
    public void renderInEditor(boolean selected){
        DirectDraw.getInstance().drawBillboard(new Vector3f(this.getPosition()), new Vector2f(1), ProbeManager.getInstance().getReflectionProbeSVG());
    }

    public int getReflectionProbeTextureID(){
        return cubeMapTextureID;
    }
}
