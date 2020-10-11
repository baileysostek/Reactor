package entity;

import entity.component.Attribute;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class AttributeUtils {

    private static final Vector2f DEFAULT_VEC2 = new Vector2f();
    private static final Vector3f DEFAULT_VEC3 = new Vector3f();

    public static boolean isEmpty(Attribute check){
        Object data = check.getData();

        if(data == null){
            return true;
        }

        if(data instanceof String){
            return ((String)data).isEmpty();
        }

        if(data instanceof Number){
            return ((Number)data) == (Number) 0;
        }

        if(data instanceof Vector2f){
            return ((Vector2f)data).equals(DEFAULT_VEC2);
        }

        if(data instanceof Vector3f){
            return ((Vector3f)data).equals(DEFAULT_VEC3);
        }

        return false;
    }
}
