package models;

import org.joml.Vector3f;

public class AABBUtils {

    public static Vector3f sizeOf(Vector3f[] aabb){
        return new Vector3f(aabb[1]).sub(aabb[0]).absolute();
    }

    public static Vector3f sizeOf(AABB aabb){
        return sizeOf(aabb.getVerteces());
    }

}