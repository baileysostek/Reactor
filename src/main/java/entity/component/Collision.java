package entity.component;

import com.bulletphysics.BulletGlobals;
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
import input.Keyboard;
import physics.PhysicsEngine;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.LinkedList;

public class Collision extends Component{

    //This Components attributes
    RigidBody body;
    Transform trans = new Transform();

    //Attributes that we care about
    Attribute<org.joml.Vector3f> position    = new Attribute<org.joml.Vector3f> ("position", new org.joml.Vector3f(0));
    Attribute<Boolean>           movable     = new Attribute<Boolean>           ("movable", true);
    Attribute<Float>             friction    = new Attribute<Float>             ("friction", 1.0f);
    Attribute<Float>             mass        = new Attribute<Float>             ("mass", 1.0f);
    Attribute<Float>             restitution = new Attribute<Float>             ("restitution", 0.5f);


    public Collision(){

    }


    @Override
    protected LinkedList<Attribute> initialize() {
        //This we're going to give mass so it responds to gravity

//        CollisionShape fallShape = new SphereShape(1);
        CollisionShape fallShape = new BoxShape(new Vector3f(parent.getScale().x(), parent.getScale().y(),parent.getScale().z() ));

        Vector3f fallInertia = new Vector3f(0,0,0);
        fallShape.calculateLocalInertia(mass.getData(),fallInertia);

        MotionState ballMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(parent.getPosition().x(), parent.getPosition().y(), parent.getPosition().z()), 1.0f)));
        RigidBodyConstructionInfo ballConstructionInfo = new RigidBodyConstructionInfo(mass.getData(), ballMotionState, fallShape, fallInertia);
        ballConstructionInfo.restitution = restitution.getData();
        ballConstructionInfo.angularDamping = 0.9f;
        body = new RigidBody(ballConstructionInfo);


        //Add ourself to the physics engine
        PhysicsEngine.getInstance().addRigidBody(this);

        //Attribute stuff
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        out.addLast(position);
        out.addLast(movable);
        out.addLast(friction);
        out.addLast(mass);
        out.addLast(restitution);

        //Return out
        return out;
    }

    @Override
    public void update(double delta) {
        //This is the frame update
        //If we are not controlled externally set this entities pos to the rigid body pos
        body.getMotionState().getWorldTransform(trans);

        //Update Pos
        position.getData().x = trans.origin.x;
        position.getData().y = trans.origin.y;
        position.getData().z = trans.origin.z;

        javax.vecmath.Vector3f inertia = new javax.vecmath.Vector3f();
        body.getCollisionShape().calculateLocalInertia(this.mass.getData(), inertia);
        body.setMassProps(this.mass.getData(), inertia);
        body.updateInertiaTensor();

        //Set Pos
        this.parent.setPosition(position.getData());

//        CollisionShape fallShape = new SphereShape(1);
//        CollisionShape fallShape = new BoxShape(new Vector3f(parent.getScale().x(), parent.getScale().y() ,parent.getScale().z()));
//        body.setCollisionShape(fallShape);

//        Transform newTransform = new Transform();
//        body.getWorldTransform(newTransform).setRotation(new Quat4f());
//        body.setWorldTransform(newTransform);
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {
        //If we get a position update
        switch (observed.getName()){
            case "friction":{
                body.setFriction((Float) observed.getData());
            }
            case "mass":{
                Vector3f fallInertia = new Vector3f(0,0,0);
                body.setMassProps((Float) observed.getData(), fallInertia);
            }
        }

    }

    //Callback function triggered on hit
    //Truths: other MUST have a collision component
    public void onCollide(Entity other, ManifoldPoint contactPoint) {
        invoke("onCollide", other, contactPoint);
    }

    public RigidBody getRigidBody() {
        return this.body;
    }

    //This components name
    @Override
    public String getName() {
        return "Collision";
    }

}
