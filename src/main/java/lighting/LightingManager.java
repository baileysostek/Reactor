package lighting;

import engine.FraudTek;
import entity.Entity;
import entity.EntityManager;
import graphics.renderer.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;
import particle.ParticleManager;
import util.StopwatchManager;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LightingManager {
    private static LightingManager lightingManager;
    private int lightDepth;

    private LinkedList<Light> lights        = new LinkedList<>();
    private LinkedList<CastsShadows> shadowCasters = new LinkedList<>();
    private LinkedList<PointLight> pointLights = new LinkedList<>();

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
        GL46.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        ShaderManager.getInstance().useShader(lightDepth);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        ShaderManager.getInstance().loadUniformIntoActiveShader("lightSpaceMatrix", directionalLight.getLightspaceTransform());

        //Render all entities
        EntityManager.getInstance().resort();

        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {
                if(entity.isVisible()) {
                    ShaderManager.getInstance().loadHandshakeIntoShader(lightDepth, entity.getModel().getHandshake());

                    //Mess with uniforms
                    GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lightDepth, "model"), false, entity.getTransform().get(new float[16]));
                    GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, entity.getModel().getNumIndicies());
                }
            }
        }

        directionalLight.getDepthBuffer().unbindFrameBuffer();

    }

    public void drawFromMyPerspective(SpotLight directionalLight) {
        directionalLight.getDepthBuffer().bindFrameBuffer();
        GL46.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        ShaderManager.getInstance().useShader(lightDepth);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        ShaderManager.getInstance().loadUniformIntoActiveShader("lightSpaceMatrix", directionalLight.getLightspaceTransform());

        //Render all entities
        EntityManager.getInstance().resort();

        for(Entity entity : EntityManager.getInstance().getEntities()){
            if(entity.getModel() != null) {
                if(entity.isVisible()) {
                    ShaderManager.getInstance().loadHandshakeIntoShader(lightDepth, entity.getModel().getHandshake());

                    //Mess with uniforms
                    GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(lightDepth, "model"), false, entity.getTransform().get(new float[16]));
                    GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, entity.getModel().getNumIndicies());
                }
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
    public LinkedList<CastsShadows> getClosestLights(int n, Vector3f point) {
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
            if(light instanceof CastsShadows){
                shadowCasters.add((CastsShadows) light);
            }
            if(light instanceof PointLight){
                pointLights.add((PointLight) light);
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
            if(light instanceof CastsShadows){
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
