package particle;

import camera.CameraManager;
import entity.EntityManager;
import input.MousePicker;
import org.joml.Vector3f;

public class Particle {

    public Vector3f pos;
    public Vector3f col;
    private Vector3f startColor;
    private Vector3f endColor;
    public Vector3f velocity;
    public Vector3f acceleration;
    public float life = 2.0f;
    public float lifetime = 2.0f;

    public ParticleSystem system;

    public Particle(ParticleSystem system){
        pos      = new Vector3f(0);
        velocity = new Vector3f(0.5f, 30, 0);
        acceleration = new Vector3f(0, -0.1f, 0);
        startColor = system.determineStartColor();
        endColor = system.determineEndColor();
        col = new Vector3f(startColor);
        this.system = system;
    }

    public void reset(Vector3f initalPos, Vector3f initialVelocity){
        pos      = new Vector3f(initalPos);
        velocity = new Vector3f(initialVelocity);
    }

    public void update(double delta){
        //Lifespan
        life -= delta;
        Vector3f orbitPos = MousePicker.rayHitsPlane(CameraManager.getInstance().getActiveCamera().getPosition(), MousePicker.getInstance().getRay(), new Vector3f(0), new Vector3f(0, 1, 0));
        if(orbitPos != null) {
            orbit(orbitPos);
        }
        pos.add(new Vector3f(velocity.add(acceleration)).mul((float) delta));
        col = new Vector3f(endColor).lerp(startColor, (life / lifetime));
        if(life <= 0){
            lifetime = (float) (8f * Math.random());
            life = lifetime;
            pos.set(0);
            velocity.set((float) (Math.random() * 8f - 4f), (float) (Math.random() * 30f), (float) (Math.random() * 8f - 4f));
        }
    }

    private void orbit(Vector3f point){

        Vector3f worldPos = new Vector3f(system.getPosition()).add(pos);

        Vector3f unitDirection = new Vector3f(point).sub(worldPos).normalize();
//        Vector3f unitVelocity  = new Vector3f(velocity).normalize();

        if(new Vector3f(worldPos).distance(point) <= 5){
            velocity = velocity.add(unitDirection);
        }
    }
}
