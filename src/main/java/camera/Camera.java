/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import engine.Constants;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Bailey
 */
public abstract class Camera {

    protected Matrix4f transform = new Matrix4f();

    private Vector3f rotationV = new Vector3f(0, 0, 0);
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f offset = new Vector3f(0f);

    protected float speed = 0.1f;

    public Camera(){

    }

    public Camera(Vector3f position, Vector3f rotation){
        this.position = position;
        Quaternionf qPitch   = new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), rotation.x);
        Quaternionf qRoll    = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), rotation.y);
        Quaternionf qYaw     = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), rotation.z);

        Quaternionf orientation = (qPitch.mul(qRoll.mul(qYaw))).normalize();
        transform.rotate(orientation);
    }

    //Callback for when the camera becomes the active camera
    public void onActive(){
        return;
    }

    //Callback for when this camera is Deactivated.
    public void onDeactivated(){
        return;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        this.position.mul(-1);
    }

    public Quaternionf getRotation() {
        Quaternionf out = new Quaternionf();
        this.transform.getUnnormalizedRotation(out);
        return out;
    }

    public void setRotation(Vector3f rotation){
        Matrix4f out = new Matrix4f();
        rotationV = rotation;
        Quaternionf qPitch   = new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), rotation.x);
        Quaternionf qRoll    = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), rotation.y);
        Quaternionf qYaw     = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), rotation.z);

        Quaternionf orientation = (qPitch.mul(qRoll.mul(qYaw).normalize())).normalize();
        out.rotate(orientation);

        this.transform = out;
    }

    public void setRotation(Quaternionf quat){
        Matrix4f out = new Matrix4f();
        quat.getEulerAnglesXYZ(rotationV);
        out.rotate(quat);
        this.transform = out;
    }

    //Get and set Rotation
    public void rotate(float dx, float dy, float dz){
        this.rotationV.add(new Vector3f(dx, dy, dz));

        Quaternionf qPitch   = new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), dx);
        Quaternionf qRoll    = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), dy);
        Quaternionf qYaw     = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), dz);

        Quaternionf orientation = (qPitch.mul(qRoll.mul(qYaw).normalize())).normalize();

        transform.rotate(orientation);
    }

    public void rotate(Quaternionf rot){
        transform.rotate(rot);
        this.rotationV.add(rot.getEulerAnglesXYZ(new Vector3f(0,0,0)));
    }

    public void translate(Vector3f translation){
        this.position.add(translation);
    }

    public void setOffset(Vector3f offset){
        this.offset = offset;
    }

    public Vector3f getOffset(){
        return this.offset;
    }

    public Vector3f getLookingDirection(){
        return new Vector3f(Constants.FUNDAMENTAL_FORWARD_VECTOR()).mulTransposeDirection(transform);
    }

    public abstract void update(double delta);

    public Vector3f getRotationV() {
        return rotationV;
    }

    public Matrix4f getTransformationMatrix(){
        return new Matrix4f().identity().mul(new Matrix4f(this.transform)).translate(new Vector3f(this.position));
    }

    public float[] getTransform() {
        Matrix4f transform = getTransformationMatrix();


        float[] modelMatrix = new float[]{
                transform.m00(), transform.m01(), transform.m02(), transform.m03(),
                transform.m10(), transform.m11(), transform.m12(), transform.m13(),
                transform.m20(), transform.m21(), transform.m22(), transform.m23(),
                transform.m30(), transform.m31(), transform.m32(), transform.m33()
        };

        return modelMatrix;
    }


}
