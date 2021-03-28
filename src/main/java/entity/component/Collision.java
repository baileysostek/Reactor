package entity.component;

import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
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
import util.Callback;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.LinkedList;

public class Collision extends Component implements Collider {

    //This Components attributes
    RigidBody body;
    Transform trans = new Transform();

    //Attributes that we care about
    Attribute<org.joml.Vector3f> position    = new Attribute<org.joml.Vector3f> ("position", new org.joml.Vector3f(0));
    Attribute<org.joml.Vector3f> scale       = new Attribute<org.joml.Vector3f> ("scale", new org.joml.Vector3f(1));
    Attribute<Float>             friction    = new Attribute<Float> ("friction", 1.0f).setType(EnumAttributeType.SLIDERS);
    Attribute<Float>             mass        = new Attribute<Float> ("mass", 10.0f).setType(EnumAttributeType.SLIDERS);
    Attribute<Float>             restitution = new Attribute<Float> ("restitution", 0.35f).setType(EnumAttributeType.SLIDERS);

    private EnumCollisionShape shape = EnumCollisionShape.SPHERE;

    public Collision(){
        super("Collision");
    }

    @Override
    public void onRemove(){
        PhysicsEngine.getInstance().removeRigidBody(this);
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        //This we're going to give mass so it responds to gravity

//        CollisionShape fallShape = new SphereShape(1);
        CollisionShape fallShape = new BoxShape(new Vector3f(parent.getScale().x(), parent.getScale().y(), parent.getScale().z()));

        Vector3f fallInertia = new Vector3f(0,0,0);
        fallShape.calculateLocalInertia(mass.getData(), fallInertia);

        MotionState ballMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(parent.getPosition().x(), parent.getPosition().y(), parent.getPosition().z()), 1.0f)));
        RigidBodyConstructionInfo ballConstructionInfo = new RigidBodyConstructionInfo(mass.getData(), ballMotionState, fallShape, fallInertia);
        ballConstructionInfo.restitution = restitution.getData();
        ballConstructionInfo.angularDamping = 0.1f;
        body = new RigidBody(ballConstructionInfo);

        body.setMassProps(this.mass.getData(), fallInertia);

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
        //If this is controlled by an external source, we dont do this
        body.getWorldTransform(trans);
        body.updateInertiaTensor();

        //Update Pos
        position.getData().x = trans.origin.x;
        position.getData().y = trans.origin.y;
        position.getData().z = trans.origin.z;

        Quaternionf rotation = new Quaternionf();
        Quat4f bulletRot = new Quat4f();
        trans.getRotation(bulletRot);

        rotation.x = bulletRot.x;
        rotation.y = bulletRot.y;
        rotation.z = bulletRot.z;
        rotation.w = bulletRot.w;

        this.parent.setRotation(rotation);
        //Set Pos
        this.parent.setPosition(position.getData());
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {
        //If we get a position update
        switch (observed.getName()){
            case "friction":{
                body.setFriction((Float) observed.getData());
                break;
            }
            case "mass":{
                Vector3f fallInertia = new Vector3f(0,0,0);
                body.setMassProps((Float) observed.getData(), fallInertia);
                break;
            }
            case "position":{
                org.joml.Vector3f pos = (org.joml.Vector3f) observed.getData();
                Transform t = new Transform();
                body.getWorldTransform(t);
                t.origin.x = pos.x;
                t.origin.y = pos.y;
                t.origin.z = pos.z;
                body.setWorldTransform(t);
                break;
            }
            case "scale":{
                org.joml.Vector3f scale = (org.joml.Vector3f) observed.getData();
                body.getCollisionShape().setLocalScaling(new Vector3f(scale.x, scale.y, scale.z));
                break;
            }
        }
    }

    //Callback function triggered on hit
    //Truths: other MUST have a collision component
    @Override
    public void onCollide(Entity other, ManifoldPoint contactPoint) {
        invoke("onCollide", other, contactPoint);
    }

    @Override
    public RigidBody getRigidBody() {
        return this.body;
    }

    //This components name
    @Override
    public String getName() {
        return "Collision";
    }

    public void addAcceleration(org.joml.Vector3f acceleration){
        body.applyCentralImpulse(new Vector3f(acceleration.x, acceleration.y, acceleration.z));
    }

    public void setMass(float mass){
        this.mass.setData(mass);
    }

    private CollisionShapeInertia recalculateBounds(EnumCollisionShape shape){
        switch (shape){
            case SPHERE:{
                CollisionShape fallShape = new SphereShape(parent.getScale().x());
                Vector3f fallInertia = new Vector3f(0,0,0);
                fallShape.calculateLocalInertia(mass.getData(), fallInertia);
                body.setMassProps(this.mass.getData(), fallInertia);
                body.setCollisionShape(fallShape);
                return new CollisionShapeInertia(fallInertia, fallShape);
            }

            case CUBE:{
                CollisionShape fallShape = new BoxShape(new Vector3f(parent.getScale().x(), parent.getScale().y(), parent.getScale().z()));
                Vector3f fallInertia = new Vector3f(0,0,0);
                fallShape.calculateLocalInertia(mass.getData(), fallInertia);
                body.setMassProps(this.mass.getData(), fallInertia);
                body.setCollisionShape(fallShape);
                return new CollisionShapeInertia(fallInertia, fallShape);
            }

            case CAPSULE:{
                CollisionShape fallShape = new CapsuleShape(parent.getScale().x(), parent.getScale().y());
                Vector3f fallInertia = new Vector3f(0,0,0);
                fallShape.calculateLocalInertia(mass.getData(), fallInertia);
                body.setMassProps(this.mass.getData(), fallInertia);
                body.setCollisionShape(fallShape);
                return new CollisionShapeInertia(fallInertia, fallShape);
            }
        }
        return null;
    }

    public CollisionShapeInertia setCollisionShape(EnumCollisionShape shape){
        this.shape = shape;
        if(this.parent != null){
            return recalculateBounds(shape);
        }
        return null;
    }

}

class CollisionShapeInertia{
    public Vector3f fallInertia;
    public CollisionShape fallShape;

    public CollisionShapeInertia(Vector3f fallInertia, CollisionShape fallShape){
        this.fallInertia = fallInertia;
        this.fallShape = fallShape;
    }
}

