package lighting;

import camera.CameraManager;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.renderer.VAO;
import graphics.sprite.SpriteBinder;
import material.Material;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;
import skybox.SkyboxManager;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LightingManager {
    private static LightingManager lightingManager;
    private int lightDepth;

    private LinkedList<Light> lights        = new LinkedList<>();
    private LinkedList<DirectionalLight> shadowCasters = new LinkedList<>();
    private LinkedList<PointLight> pointLights = new LinkedList<>();

    private final int LIGHT_BULB_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/lightbulb.svg", 1, 1f, 96f);
    private final int SUN_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/sun.svg", 1, 1, 96f);

    //Lock for locking our entity set
    private Lock lock;

    private LightingManager(){
        lightDepth  = ShaderManager.getInstance().loadShader("depth");
        lock        = new ReentrantLock();
    }

    //Here we prepare everything we need this frame
    public void update(double delta){

    }

    public void drawFromMyPerspective(DirectionalLight directionalLight) {
        directionalLight.getDepthBuffer().bindFrameBuffer();
        GL46.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        ShaderManager.getInstance().useShader(lightDepth);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(ShaderManager.getInstance().getActiveShader(), "view"), false, directionalLight.getLightspaceTransform());
//        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(ShaderManager.getInstance().getActiveShader(), "perspective"), false, Renderer.getInstance().getProjectionMatrix());

        //Render all entities
        EntityManager.getInstance().resort();

        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {

                ShaderManager.getInstance().loadHandshakeIntoShader(lightDepth, entity.getModel().getHandshake());

                //Mess with uniforms
                GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lightDepth, "model"), false, entity.getTransform().get(new float[16]));
                GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, entity.getModel().getNumIndicies());
            }
        }

//        LinkedHashMap<VAO, LinkedHashMap<Material, LinkedList<Entity>>> batches = EntityManager.getInstance().getBatches();
//        for(VAO vao : batches.keySet()) {
//            if(vao == null){
//                continue;
//            }
//            LinkedList<Entity> rendered = new LinkedList<>();
//            LinkedHashMap<Material, LinkedList<Entity>> materialEntities = batches.get(vao);
//            for(LinkedList<Entity> toRender : materialEntities.values()) {
//                for(Entity e : toRender){
//                    if(e.hasAttribute("Casts Shadows")){
//                        if((boolean)e.getAttribute("Casts Shadows").getData()){
//                            rendered.add(e);
//                        }
//                    }else{
//                        rendered.add(e);
//                    }
//                }
//            }
//            if(rendered.size() > 0) {
//                vao.render(rendered);
//            }
//            rendered.clear();
//        }

        directionalLight.getDepthBuffer().unbindFrameBuffer();
    }

    public static void initialize(){
        if(lightingManager == null){
            lightingManager = new LightingManager();
        }
    }

    public static LightingManager getInstance(){
        return lightingManager;
    }

    // returns the closest n lights to point
    public LinkedList<DirectionalLight> getClosestLights(int n, Vector3f point) {
        return shadowCasters;
    }

    public LinkedList<PointLight> getClosestPointLights(int n, Vector3f point) {
        return pointLights;
    }

    public void add(Light light){
        //Disallow adding a null light.
        if(light == null){
            return;
        }

        //if not null, lock the array and add a light, then unlock.
        lock.lock();
        try {
            //Add the entity
            this.lights.add(light);
            if(light instanceof DirectionalLight){
                shadowCasters.add((DirectionalLight) light);
            }
            if(light instanceof PointLight){
                pointLights.add((PointLight) light);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getLightBulbSVG(){
        return LIGHT_BULB_SVG;
    }

    public int getSunSVG() {
        return SUN_SVG;
    }

    public void remove(Light light) {
        //Disallow adding a null light.
        if(light == null){
            return;
        }

        //if not null, lock the array and add a light, then unlock.
        lock.lock();
        try {
            //Add the entity
            this.lights.remove(light);
            if(light instanceof DirectionalLight){
                shadowCasters.remove(light);
            }
            if(light instanceof PointLight){
                pointLights.remove(light);
            }
        } finally {
            lock.unlock();
        }
    }
}
