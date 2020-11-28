package entity.component;

import entity.Entity;
import entity.component.Attribute;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Callback;

import java.util.Collection;
import java.util.LinkedList;

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

    public static boolean differ(Object newData, Object existingData){
        if(newData instanceof String){
            return !existingData.equals(newData);
        }if(newData instanceof Vector3f){
            Vector3f newVec = ((Vector3f) newData);
            Vector3f oldData = ((Vector3f) existingData);

            return (newVec.x != oldData.x) || (newVec.y != oldData.y) || (newVec.z != oldData.z);
        }else if(newData instanceof Vector4f){
            Vector4f newVec = ((Vector4f) newData);
            Vector4f oldData = ((Vector4f) existingData);

            return (newVec.x != oldData.x) || (newVec.y != oldData.y) || (newVec.z != oldData.z) || (newVec.w != oldData.w);
        }else{
            if(newData instanceof Collection){
                Collection newCollection = ((Collection)newData);
                Collection currentData   = ((Collection) existingData);
                Object[] currentDataBuffer = currentData.toArray();
                //Quick Compare if size of elements are different return true.
                if(currentData.size() != newCollection.size()){
                    return true;
                }

                //Deep compare. Element size did not change, lets do a deep compare on each object.
                int index = 0;
                for(Object obj : newCollection){
                    if(differ(obj, currentDataBuffer[index])){
                        System.out.println("Changed!");
                        return true;
                    }
                    index++;
                }
                return false;
            }

            return existingData != newData;
        }
    }

    public static Attribute synchronizeWithParent(Attribute check, Entity parent){
        LinkedList<Callback> subscribers = check.subscribers;
        Attribute out = parent.getAttribute(check.getName());
        for(Callback subscriber : subscribers){
            out.subscribe(subscriber);
        }
        return out;
    }
}

























