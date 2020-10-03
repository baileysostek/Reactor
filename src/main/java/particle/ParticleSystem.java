package particle;

import entity.Entity;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import org.joml.Vector3f;
import util.Callback;

public class ParticleSystem extends Entity {
    //Entity implementation for a collection of particles, contains information about what properties effect this particle system.

    //Attributes are variables controlled in editor.
    Attribute<Integer>  numParticles;
    Attribute<Vector3f> color;

    private int start = 0;
    private boolean needsUpdate = true;

    private Particle[] particles;

    public ParticleSystem(){
        //Number of particles in use.
        start = ParticleManager.getInstance().getAllocatedParticles();

        //Attribute config
        color = new Attribute<Vector3f>("color", new Vector3f(1, 0, 0)).setType(EnumAttributeType.COLOR);

        //How many we want
        numParticles = new Attribute<Integer>("numParticles", 100);
        ParticleSystem that = this;
        numParticles.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
            int remaining = ParticleManager.getInstance().getUnallocatedParticles(that);
            if(numParticles.getData() > remaining){
                numParticles.setData(remaining);
            }
            updateSystem();
            return null;
            }
        });
        //Add attributes
        this.addAttribute(numParticles);
        this.addAttribute(color);

        //Force update
        this.getAttribute("updateInEditor").setData(true);
        updateSystem();
    }

    public void onAdd(){
        ParticleManager.getInstance().add(this);
    }

    public void onRemove(){
        ParticleManager.getInstance().remove(this);
    }

    private void updateSystem(){
        needsUpdate = true;
        this.particles = new Particle[numParticles.getData()];
        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle();
        }
    }

    public Vector3f getColor(){
        return this.color.getData();
    }

    public void update(double delta){
        for(Particle p : particles){
            p.update(delta);
        }
    }

    public boolean needsUpdate(){
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate){
        this.needsUpdate = needsUpdate;
    }

    public Particle getParticle(int index){
        return particles[index];
    }

    public int getStartIndex() {
        return start;
    }
}
