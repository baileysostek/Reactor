package editor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.EntityManager;
import util.StringUtils;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;

public class DocumentationGenerator {

    public static JsonObject generateDocumentation(){
        JsonObject documentation = new JsonObject();

        LinkedList<Object> documentedClasses = new LinkedList<>();
        documentedClasses.add(EntityManager.getInstance());

        for(Object object : documentedClasses){
            Class objectClass = object.getClass();
            JsonObject classObject = new JsonObject();
            String className = objectClass.getSimpleName();
            for(Method m : objectClass.getMethods()){
                String methodName = m.getName();
                JsonObject methodJson = new JsonObject();
                methodJson.addProperty("name", methodName);
                JsonObject parameters = new JsonObject();
                for(Parameter p : m.getParameters()){
                    parameters.addProperty(p.getName(), p.getType().getSimpleName());
                }
                methodJson.add("parameters", parameters);
                methodJson.addProperty("returns", m.getGenericReturnType().getTypeName());

                classObject.add(methodName, methodJson);
            }
            documentation.add(className, classObject);
        }

        StringUtils.write(documentation.toString(), "/documentation.json");

        return documentation;
    }
}
