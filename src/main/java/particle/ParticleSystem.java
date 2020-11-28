package particle;

import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import entity.component.EnumAttributeType;
import graphics.renderer.Renderer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import util.Callback;

import java.util.LinkedList;

public class ParticleSystem extends Entity {
    //Entity implementation for a collection of particles, contains information about what properties effect this particle system.

    //Attributes are variables controlled in editor.
    Attribute<Integer> numParticles;

    //Color
    Attribute<LinkedList<Vector3f>> startColors;
    Attribute<ColorInterpolation> deriveStartColor;
//    Attribute<LinkedList<Vector3f>> endColors;
//    Attribute<ColorInterpolation> deriveEndColor;

    //Lifespan
        //Curve of lifepsan values, Min, Max, Interpolation

    //Size
        //Curve of Size values, Start{min max}, End{min max}, Interpolation

    //Rotation
        //Curve of Rotation values, Start{min max}, End{min max}, Interpolation

    //Opacity
        //Curve of Opacity values, Start{min max}, End{min max}, Interpolation

    //Texture



    //Physics
        //Gravity
        //Collision Detection
        //

    //Burst Loop or Not
    Attribute<EmissionType> emissionType;

    //Emission
    Attribute<EmissionShape> emissionShape;
//    Attribute<ColorInterpolation> deriveEndColor;

    //Transition
    Attribute<ColorTransition> startToEndTransition;
    Attribute<Integer> startIndex;

    //Buttons
    Attribute<Callback> playButton;
    Attribute<Callback> pauseButton;



    public float lifetime = 8.0f;
    private float time = 0;
    private int burstCount = 0;
    private boolean canBurst = false;
    private boolean needsUpdate = true;
    private boolean paused = false;

    private Particle[] particles;

    private final LinkedList<Attribute> additionalAttributes = new LinkedList<Attribute>(){};

    public ParticleSystem(){
        //Attribute config
        LinkedList<Vector3f> startColorsList = new LinkedList<Vector3f>(){};
        startColorsList.add(new Vector3f(1, 0, 0));
        startColorsList.add(new Vector3f(0, 1, 0));
        startColorsList.add(new Vector3f(0, 0, 1));
        startColors = new Attribute<LinkedList<Vector3f>>("startColor", startColorsList).setType(EnumAttributeType.COLOR);
        startColors.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                return null;
            }
        });

        deriveStartColor = new Attribute<ColorInterpolation>("startInterpolationType" , ColorInterpolation.RANDOM);
        deriveStartColor.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                return null;
            }
        });

//        LinkedList<Vector3f> endColorsList = new LinkedList<Vector3f>(){};
//        endColorsList.add(new Vector3f(1, 0, 0));
//        endColorsList.add(new Vector3f(0, 1, 0));
//        endColorsList.add(new Vector3f(0, 0, 1));
//        endColors = new Attribute<LinkedList<Vector3f>>("endColors", endColorsList).setType(EnumAttributeType.COLOR);
//        endColors.subscribe(new Callback() {
//            @Override
//            public Object callback(Object... objects) {
//            updateSystem();
//            return null;
//            }
//        });

        startIndex = new Attribute<Integer>("startIndex", 0).setShouldBeSerialized(false);
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

        //Emission type
        emissionType  = new Attribute<EmissionType>( "Emission Type" , EmissionType.CONTINUOUS);
        emissionShape = new Attribute<EmissionShape>("Emission Shape", EmissionShape.CUBE);
        emissionShape.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                System.out.println("Test");
                return null;
            }
        });

        //Buttons
        playButton = new Attribute<Callback>("Play", new Callback() {
            @Override
            public Object callback(Object... objects) {
                start();
                return null;
            }
        }).setShouldBeSerialized(false);

        pauseButton = new Attribute<Callback>("Pause", new Callback() {
            @Override
            public Object callback(Object... objects) {
                pause();
                return null;
            }
        }).setShouldBeSerialized(false);

        // Add attributes
        this.addAttribute(numParticles);
        this.addAttribute(startColors);
        this.addAttribute(startIndex);
//        this.addAttribute(endColors);

        // Enums
        this.addAttribute(emissionType);
        this.addAttribute(emissionShape);

        // Buttons
        this.addAttribute(playButton);
        this.addAttribute(pauseButton);

        // Force update
        this.getAttribute("updateInEditor").setData(true);

        // Add subscription to scale
        super.getAttribute("scale").subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
            updateSystem();
            return null;
            }
        });

        // Set system based on initial params
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

    public void start(){
        paused = false;
        time = lifetime;
        canBurst = true;
        burstCount = 0;
    }

    public void pause(){
        paused = true;
    }

    public void stop(){
        this.paused = true;
        for (Particle p : particles) {
            p.reset();
        }
        canBurst = false;
        burstCount = 0;
    }

    public void update(double delta){
        if(!paused) {
            //Burst
            time += delta;
            if(time > lifetime){
                canBurst = true;
                burstCount++;
            }else{
                canBurst = false;
            }
            time %= lifetime;

            for (Particle p : particles) {
                p.update(delta);
            }
        }
    }

    protected boolean canRespawn(){
        switch (emissionType.getData()){
            case CONTINUOUS:{
                return true;
            }
            case BURST_LOOP:{
                return canBurst;
            }
            case BURST_SINGLE:{
                if(burstCount <= 1) {
                    return canBurst;
                }else{
                    return false;
                }
            }
            default:{
                return true;
            }
        }
    }

    protected Vector3f determineStartColor(){
        switch (deriveStartColor.getData()){
            case DISCRETE:{
                int index = (int) Math.floor(startColors.getData().size() * Math.random());
                return startColors.getData().get(index);
            }
            case RANDOM:{
                //If there are not enough colors to do a random, return the first.
                if(startColors.getData().size() < 2){
                    return startColors.getData().getFirst();
                }

                //Pick two random colors.
                int index_first  = (int) Math.floor(startColors.getData().size() * Math.random());
                int index_second = (int) Math.floor(startColors.getData().size() * Math.random());
                while(index_first == index_second){
                    index_second = (int) Math.floor(startColors.getData().size() * Math.random());
                }

                Vector3f color1 = new Vector3f(startColors.getData().get(index_first));
                Vector3f color2 = new Vector3f(startColors.getData().get(index_second));

                return new Vector3f(new Vector3f(color1).add(color2)).lerp(color1.lerp(color2, (float) Math.random()).normalize(), (float) Math.random());
            }
        }
        return new Vector3f(0);
    }

    protected Vector3f determineEndColor(){
        return new Vector3f(0);
    }

    protected Vector3f determineStartPosition(){
        switch (emissionShape.getData()){
            case POINT:{
                return new Vector3f(0);
            }
            case CUBE:{
                return new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
            }
            case PLANE:{
                return new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), 0).mul(2).mul(super.getScale());
            }
            case SPHERE:{
                while (true) {
                    Vector3f cube = new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
                    if ( cube.length() <= super.getScale().x ){
                        return cube;
                    }
                }
            }
            case CYLINDER:{
                while (true) {
                    Vector3f cube = new Vector3f((float)(Math.random() - 0.5f),0, (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
                    if ( cube.length() <= super.getScale().x ){
                        cube.y = (float)(Math.random() - 0.5f) * (2f * super.getScale().y);
                        return cube;
                    }
                }
            }
        }
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

    @Override
    public JsonObject serialize(){
        return super.serialize();
    }

    @Override
    public ParticleSystem deserialize(JsonObject data) {
        super.deserialize(data);

        startColors   = AttributeUtils.synchronizeWithParent(startColors  , this);
        emissionShape = AttributeUtils.synchronizeWithParent(emissionShape, this);
        emissionType  = AttributeUtils.synchronizeWithParent(emissionType , this);
        numParticles  = AttributeUtils.synchronizeWithParent(numParticles , this);

        this.updateSystem();
        return this;
    }
}
