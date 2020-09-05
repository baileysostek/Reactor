package entity.interfaces;


import org.joml.Vector3f;

public interface Transformable {
    Transformable setPosition(Vector3f pos);
    Vector3f getPosition();
    Transformable setRotation(Vector3f rot);
    Vector3f getRotation();
    Transformable setScale(Vector3f scale);
    Transformable setScale(float scalar);
    Vector3f getScale();
}
