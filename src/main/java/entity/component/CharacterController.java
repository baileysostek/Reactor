package entity.component;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import entity.Entity;
import org.joml.Quaternionf;
import physics.Collider;
import physics.PhysicsEngine;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.LinkedList;

public class CharacterController extends Component implements Collider {

    //This Components attributes
    RigidBody body;

    //Attributes that we care about
    Attribute<org.joml.Vector3f> position    = new Attribute<org.joml.Vector3f> ("position", new org.joml.Vector3f(0));
    Attribute<org.joml.Vector3f> scale       = new Attribute<org.joml.Vector3f> ("scale", new org.joml.Vector3f(0));

    Attribute<Float>             friction    = new Attribute<Float> ("friction", 1.0f).setType(EnumAttributeType.SLIDERS);
    Attribute<Float>             mass        = new Attribute<Float> ("mass", 10.0f).setType(EnumAttributeType.SLIDERS);
    Attribute<Float>             restitution = new Attribute<Float> ("restitution", 0.35f).setType(EnumAttributeType.SLIDERS);

    private KinematicCharacterController character;
    private PairCachingGhostObject ghostObject;

    public CharacterController(){

    }

    @Override
    public void onRemove(){
        PhysicsEngine.getInstance().removeRigidBody(this);
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        //from the source src\com\bulletphysics\demos\character\CharacterDemo.java
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        org.joml.Vector3f parentPos = super.getParent().getPosition();
        startTransform.origin.set(parentPos.x, parentPos.y, parentPos.z);

        //TODO fix this? maybe??
        Vector3f worldMin = new Vector3f(-1000f,-1000f,-1000f);
        Vector3f worldMax = new Vector3f(1000f,1000f,1000f);
        AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);

        ghostObject = new PairCachingGhostObject();
        ghostObject.setWorldTransform(startTransform);
        sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        float characterHeight = parent.getScale().y;
        float characterWidth  = parent.getScale().x;

        ConvexShape capsule = new CapsuleShape(characterWidth, characterHeight);
        ghostObject.setCollisionShape(capsule);
        ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

        float stepHeight = 0.35f * characterHeight;
        character = new KinematicCharacterController(ghostObject, capsule, stepHeight);

        character.debugDraw(PhysicsEngine.getInstance().getDrawer());

        PhysicsEngine.getInstance().addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
        PhysicsEngine.getInstance().addAction(character);


        MotionState ballMotionState = new DefaultMotionState(startTransform);
        RigidBodyConstructionInfo ballConstructionInfo = new RigidBodyConstructionInfo(0f, ballMotionState, capsule, new Vector3f(0, 0, 0));
        ballConstructionInfo.restitution = restitution.getData();
        ballConstructionInfo.angularDamping = 0.1f;
        ballConstructionInfo.friction = 0f;

        body = new RigidBody(ballConstructionInfo);

        body.setCollisionFlags(body.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

        //Add ourself to the physics engine
        PhysicsEngine.getInstance().addRigidBody(this);

        //Attribute stuff
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        out.addLast(position);
        out.addLast(scale);
        out.addLast(friction);
        out.addLast(mass);
        out.addLast(restitution);

        //Return out
        return out;
    }

    @Override
    public void update(double delta) {
        org.joml.Vector3f pos = (org.joml.Vector3f) super.parent.getPosition();
        Transform t = new Transform();
        t.setIdentity();
        t.origin.x = pos.x;
        t.origin.y = pos.y;
        t.origin.z = pos.z;

        body.applyCentralImpulse(PhysicsEngine.getInstance().getGravity());

        body.getMotionState().setWorldTransform(t);
        ghostObject.setWorldTransform(t);
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {
        //If we get a position update
        switch (observed.getName()){
            case "scale":{
                CollisionShape fallShape = new CapsuleShape(parent.getScale().x(), parent.getScale().y());
                body.setCollisionShape(fallShape);
                break;
            }

            case "position":{
                org.joml.Vector3f pos = (org.joml.Vector3f) observed.getData();
                Transform t = new Transform();
                t.setIdentity();
                t.origin.x = pos.x;
                t.origin.y = pos.y;
                t.origin.z = pos.z;

                body.setWorldTransform(t);
                ghostObject.setWorldTransform(t);
                break;
            }
        }
    }

    //Callback function triggered on hit
    //Truths: other MUST have a collision component
    public void onCollide(Entity other, ManifoldPoint contactPoint) {
        invoke("onCollide", other, contactPoint);
        System.out.println("HIT");
    }

    @Override
    public RigidBody getRigidBody() {
        return body;
    }

    //This components name
    @Override
    public String getName() {
        return "CharacterController";
    }


    public void setMass(float mass){
        this.mass.setData(mass);
    }
}
