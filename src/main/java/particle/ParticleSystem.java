package particle;

import entity.Entity;
import entity.component.Attribute;
import util.Callback;

public class ParticleSystem extends Entity {
    //Entity implementation for a collection of particles, contains information about what properties effect this particle system.

    //Attributes
    Attribute<Integer> numParticles;

    private Particle[] particles;

    public ParticleSystem(){
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
        this.addAttribute(numParticles);
        updateSystem();
    }

    public void onAdd(){
        ParticleManager.getInstance().add(this);
    }

    public void onRemove(){
        ParticleManager.getInstance().remove(this);
    }

    private void updateSystem(){
        this.particles = new Particle[numParticles.getData()];
        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle();
        }
    }

    public void update(double delta){
        for(Particle p : particles){
            p.update(delta);
        }
    }

}
