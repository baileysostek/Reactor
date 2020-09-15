package entity.interfaces;


import org.joml.Vector3f;

public interface Transformable {
    Transformable setPosition(Vector3f pos);
    void setPosition(float x, float y, float z);
    Vector3f translate(float x, float y, float z);
    Vector3f translate(Vector3f offset);
    Vector3f getPosition();
    Vector3f getPositionSelf();
    Transformable setRotation(Vector3f rot);
    Vector3f getRotation();
    Transformable setScale(Vector3f scale);
    Transformable setScale(float scalar);
    Vector3f getScale();

}
