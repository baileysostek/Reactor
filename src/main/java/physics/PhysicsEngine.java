package physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import entity.component.Collision;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;


public class PhysicsEngine {
    BroadphaseInterface broadphase = new DbvtBroadphase();
    DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
    CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

    SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

    DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
    HashMap<RigidBody, Collision> bodies = new HashMap<RigidBody, Collision>();
    Collection<RigidBody> blackList = new LinkedList<RigidBody>();

    private static PhysicsEngine physicsManager;
    //This is the drawer that renders the wireframe meshes for the engine.
    private final BulletDebugDrawer drawer;

    private PhysicsEngine(){
        //Define our drawer
        drawer = new BulletDebugDrawer();

        // set the gravity of our world
        dynamicsWorld.setGravity(new Vector3f(0, -9.8f, 0));

        // setup our collision shapes
        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);

        // setup the motion state
        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1.0f)));

        RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0,-1f,0));
        RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

        groundRigidBody.setFriction(0.5f);
        groundRigidBody.setRestitution(0.5f);

        dynamicsWorld.addRigidBody(groundRigidBody); // add our ground to the dynamic world.

        blackList.add(groundRigidBody);

        dynamicsWorld.setDebugDrawer(drawer);

        //Collision detection
        dynamicsWorld.setInternalTickCallback(new InternalTickCallback() {
            @Override
            public void internalTick(DynamicsWorld dynamicsWorld, float timeStep) {
                Dispatcher dispatcher = dynamicsWorld.getDispatcher();
                int manifoldCount = dispatcher.getNumManifolds();
                for (int i = 0; i < manifoldCount; i++) {
                    PersistentManifold manifold = dispatcher.getManifoldByIndexInternal(i);
                    // The following two lines are optional.
                    RigidBody object1 = (RigidBody)manifold.getBody0();
                    RigidBody object2 = (RigidBody)manifold.getBody1();

                    //Not collision with anything on blacklist
                    if(!(blackList.contains(object1) || blackList.contains(object2))) {
                        for (int j = 0; j < manifold.getNumContacts(); j++) {
                            ManifoldPoint contactPoint = manifold.getContactPoint(j);
                            if (contactPoint.getDistance() < 0.0f) {
//                                normal = contactPoint.normalWorldOnB;

                                //Get Components of each entity
                                Collision entity1 = bodies.get(object1);
                                Collision entity2 = bodies.get(object2);

                                entity1.onCollide(entity2.getParent(), contactPoint);
                                entity2.onCollide(entity1.getParent(), contactPoint);

                                break;
                            }
                        }
                    }
                }
            }
        }, null);
    }

    public BulletDebugDrawer getDrawer(){
        return this.drawer;
    }

    public void addRigidBody(Collision body){
        //Backwards mapping
        bodies.put(body.getRigidBody(), body);
        dynamicsWorld.addRigidBody(body.getRigidBody());
    }

    public void removeRigidBody(Collision body) {
        if(bodies.containsKey(body.getRigidBody())){
            bodies.remove(body.getRigidBody());
        }
        dynamicsWorld.removeRigidBody(body.getRigidBody());
    }

    public void render(){
        dynamicsWorld.debugDrawWorld();
    }

    public void update(double delta){
        dynamicsWorld.stepSimulation((float) delta);
    }

    public static void initialize(){
        if(physicsManager == null){
            physicsManager  = new PhysicsEngine();
        }
    }

    public static PhysicsEngine getInstance(){
        return physicsManager;
    }

}
