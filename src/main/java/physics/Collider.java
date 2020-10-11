package physics;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;
import entity.Entity;

public interface Collider {
    void onCollide(Entity other, ManifoldPoint contactPoint);
    RigidBody getRigidBody();
    Entity getParent();
}
