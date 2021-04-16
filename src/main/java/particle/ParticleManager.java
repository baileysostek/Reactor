package particle;

import camera.CameraManager;
import graphics.renderer.Handshake;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.SpriteBinder;
import models.ModelManager;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

import java.nio.FloatBuffer;
import java.util.LinkedList;

public class ParticleManager {
    //Handles Rendering, sorting, and updating particle systems.
    //Also will batch together textures into a texture atlas
    private static ParticleManager particleManager;

    private final int MAX_PARTICLES = 100000;

    private final LinkedList<ParticleSystem> systems = new LinkedList<ParticleSystem>();

    private boolean startIndicesDirty = false;

    private final int PARTICLE_SYSTEM_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/star.svg", 1, 1f, 96f);

    private ParticleManager(){

    }

    public void update(double delta){
        // Check if indices are dirty, this happens when a ParticleSystem changes size.
        if(startIndicesDirty){
            recalculateSystemsStartIndices();
            startIndicesDirty = false;
        }
    }

    public void render(){
        for(ParticleSystem system : systems){
            system.render();
        }
    }

    public int getMaxParticles(){
        return MAX_PARTICLES;
    }

    public int getAllocatedParticles(){
        int count = 0;
        for(ParticleSystem p : systems){
           count += p.numParticles.getData();
        }
        return count;
    }

    //Returns the number of particles we can still render
    public int getUnallocatedParticles(ParticleSystem self){
        int remaining = MAX_PARTICLES;
        for(ParticleSystem p : systems){
            if(!p.equals(self)) {
                remaining -= p.numParticles.getData();
            }
        }
        return remaining;
    }

    public void markIndicesDirty(){
        this.startIndicesDirty = true;
    }

    public void add(ParticleSystem particleSystem) {
        if(systems.add(particleSystem)){
            updateSystem(particleSystem);
        }
    }

    public void remove(ParticleSystem particleSystem){
        systems.remove(particleSystem);
        //recalculate indices
        recalculateSystemsStartIndices();
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private void updateSystem(ParticleSystem system){

    }

    private void recalculateSystemsStartIndices(){
        int count = 0;
        for(ParticleSystem s : systems){
            s.overrideStartIndex(count);
            count += s.numParticles.getData();
        }
    }

    public int getParticleSystemSVG(){
        return PARTICLE_SYSTEM_SVG;
    }

    public static void initialize(){
        if(particleManager == null){
            particleManager = new ParticleManager();
        }
    }

    public static ParticleManager getInstance(){
        return particleManager;
    }
}
