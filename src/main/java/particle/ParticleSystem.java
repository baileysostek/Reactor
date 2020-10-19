package particle;

import entity.Entity;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import graphics.renderer.Renderer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import util.Callback;

import java.util.LinkedList;

public class ParticleSystem extends Entity {
    //Entity implementation for a collection of particles, contains information about what properties effect this particle system.

    //Attributes are variables controlled in editor.
    Attribute<Integer>  numParticles;

    //Color
    Attribute<LinkedList<Vector3f>> startColors;
    Attribute<ColorInterpolation> deriveStartColor;
    Attribute<LinkedList<Vector3f>> endColors;
    Attribute<ColorInterpolation> deriveEndColor;

    //Lifespan
        //Curve of lifepsan values, Min, Max, Interpolation

    //Size
        //Curve of lifepsan values, Start{min max}, End{}, Interpolation

    //Rotation
        //Curve of lifepsan values, Start{min max}, End{min max}, Interpolation

    //Opacity
        //Curve of lifepsan values, Start{min max}, End{min max}, Interpolation

    //Texture



    //Physics
        //Gravity
        //Collision Detection
        //

    //Burst Loop or Not

    //Emission
//    Attribute<ColorInterpolation> deriveEndColor;

    //Transition
    Attribute<ColorTransition> startToEndTransition;
    Attribute<Integer> startIndex;



    public float lifetime = 2.0f;
    private boolean needsUpdate = true;

    private Particle[] particles;

    public ParticleSystem(){
        //Attribute config
        LinkedList<Vector3f> startColorsList = new LinkedList<Vector3f>(){};
        startColorsList.add(new Vector3f(1, 0, 0));
        startColorsList.add(new Vector3f(0, 1, 0));
        startColorsList.add(new Vector3f(0, 0, 1));
        startColors = new Attribute<LinkedList<Vector3f>>("startColor", startColorsList).setType(EnumAttributeType.COLOR);

        deriveStartColor = new Attribute<ColorInterpolation>("startInterpolationType" , ColorInterpolation.DISCRETE);

        LinkedList<Vector3f> endColorsList = new LinkedList<Vector3f>(){};
        endColorsList.add(new Vector3f(1, 0, 0));
        endColorsList.add(new Vector3f(0, 1, 0));
        endColorsList.add(new Vector3f(0, 0, 1));
        endColors = new Attribute<LinkedList<Vector3f>>("endColor", startColorsList).setType(EnumAttributeType.COLOR);

        startIndex = new Attribute<Integer>("startIndex", 0);
        startIndex.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                ParticleManager.getInstance().markIndicesDirty();
                return null;
            }
        });

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
            if(numParticles.getData() < 0){
                numParticles.setData(0);
            }
            updateSystem();
            return null;
            }
        });
        //Add attributes
        this.addAttribute(numParticles);
        this.addAttribute(startColors);
        this.addAttribute(endColors);

        this.addAttribute(startIndex);

        //Force update
        this.getAttribute("updateInEditor").setData(true);
        updateSystem();
    }

    public void onAdd(){
        //Number of particles in use.
        calculateStartIndex();
        ParticleManager.getInstance().add(this);
    }

    private void calculateStartIndex(){
        startIndex.setData(ParticleManager.getInstance().getAllocatedParticles());
    }

    protected void overrideStartIndex(int allocated){
        startIndex.setData(allocated);
    }

    public void onRemove(){
        ParticleManager.getInstance().remove(this);
    }

    private void updateSystem(){
        needsUpdate = true;
        this.particles = new Particle[numParticles.getData()];
        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle(this);
        }
    }

    public void update(double delta){
        for(Particle p : particles){
            p.update(delta);
        }
    }

    protected Vector3f determineStartColor(){
        switch (deriveStartColor.getData()){
            case DISCRETE:{
                int index = (int) Math.floor(startColors.getData().size() * Math.random());
                return startColors.getData().get(index);
            }
        }
        return new Vector3f(0);
    }

    protected Vector3f determineEndColor(){
        return new Vector3f(0);
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
        return startIndex.getData();
    }

    @Override
    public void renderInEditor(boolean selected){
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f(1), new Vector3f(1, 0, 0), 32, new Vector3f(1));
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f(1), new Vector3f(0, 1, 0), 32, new Vector3f(1));
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f(1), new Vector3f(0, 0, 1), 32, new Vector3f(1));
    }
}
