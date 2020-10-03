package particle;

import org.joml.Vector3f;

public class Particle {

    public Vector3f pos;
    public Vector3f velocity;
    public Vector3f acceleration;
    public float lifetime = 2.0f;

    public Particle(){
        pos      = new Vector3f(0);
        velocity = new Vector3f(0.5f, 30, 0);
        acceleration = new Vector3f(0, -0.1f, 0);
    }

    public void reset(Vector3f initalPos, Vector3f initialVelocity){
        pos      = new Vector3f(initalPos);
        velocity = new Vector3f(initialVelocity);
    }

    public void update(double delta){
        lifetime -= delta;
        pos.add(new Vector3f(velocity.add(acceleration)).mul((float) delta));
        if(lifetime <= 0){
            lifetime = (float) (Math.random() * 8f);
            pos.set(0);
            velocity.set((float) (Math.random() * 8f - 4f), (float) (Math.random() * 30f), 0);
        }
    }


}
