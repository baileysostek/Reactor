package particle;

import camera.CameraManager;
import input.MousePicker;
import org.joml.Vector3f;

public class Particle {

    private Vector3f startPosition;
    public Vector3f pos;

    public Vector3f scale;
    public Vector3f col;
    private Vector3f startColor;
    private Vector3f endColor;
    public Vector3f velocity;
    public Vector3f acceleration;
    public float life = 2.0f;
    public float lifetime = 2.0f;

    public ParticleSystem system;

    private boolean awaitingRespawn = false;

    public Particle(ParticleSystem system){
        startPosition = system.determineStartPosition();
        pos      = new Vector3f(startPosition);
        scale    = new Vector3f(1);

        velocity = system.getVelocity();
        acceleration = new Vector3f(0, 0, 0);
        startColor = system.determineStartColor();
        endColor = system.determineEndColor();
        col = new Vector3f(startColor);
        this.system = system;
    }

    public void reset(){
        lifetime = (float) (this.system.getLifetime() * Math.random());
        life = lifetime;
        pos = new Vector3f(startPosition);
        scale.set(1);
        velocity.set(this.system.getVelocity());
    }

    public void update(double delta){
        if(awaitingRespawn){
            if(system.canRespawn()) {
                reset();
                awaitingRespawn = false;
            }
            return;
        }
        //Lifespan
        life -= delta;


        float lifePercent = (life / lifetime);

//        Vector3f orbitPos = MousePicker.rayHitsPlane(CameraManager.getInstance().getActiveCamera().getPosition(), MousePicker.getInstance().getRay(), new Vector3f(0), new Vector3f(0, 1, 0));
//        if (orbitPos != null) {
//            orbit(orbitPos);
//        }
        pos.add(new Vector3f(velocity.add(acceleration)).mul((float) delta));
        col = new Vector3f(endColor).lerp(startColor, lifePercent);

        scale = new Vector3f(0).lerp(new Vector3f(1), lifePercent);


        if(life <= 0){
            awaitingRespawn = true;
            if(system.canRespawn()) {
                reset();
                awaitingRespawn = false;
            }
        }
    }

    public boolean isLive(){
        return !this.awaitingRespawn;
    }

    private void orbit(Vector3f point){

        Vector3f worldPos = new Vector3f(system.getPosition()).add(pos);

        Vector3f unitDirection = new Vector3f(point).sub(worldPos).normalize();
//        Vector3f unitVelocity  = new Vector3f(velocity).normalize();

        if(new Vector3f(worldPos).distance(point) <= 5){
            velocity = velocity.add(unitDirection);
        }
    }

    public void setLive(boolean live) {
        this.awaitingRespawn = live;
    }
}
