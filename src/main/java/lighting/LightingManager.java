package lighting;

import engine.FraudTek;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LightingManager {
    private static LightingManager lightingManager;
    private int lightDepth;

    private LinkedList<Light> lights        = new LinkedList<>();
    private LinkedList<DirectionalLight> shadowCasters = new LinkedList<>();

    //Lock for locking our entity set
    private Lock lock;

    private LightingManager(){
        lightDepth = ShaderManager.getInstance().loadShader("depth");
        lock = new ReentrantLock();
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

        ShaderManager.getInstance().loadUniformIntoActiveShader("lightSpaceMatrix", directionalLight.getLightspaceTransform());

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
        } finally {
            lock.unlock();
        }
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
        } finally {
            lock.unlock();
        }
    }
}
