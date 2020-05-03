package scripting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

public class ExpandedClass {

    String name;
    HashMap<String, LinkedList<Method>>  methods     = new HashMap<>();
    HashMap<String, Field>   fields      = new HashMap<>();
    public Class instance;

    public ExpandedClass(Class className){
        Class cls = className;
        this.name = cls.getName();
        instance = cls;
//        System.out.println(reference);

        Method[] class_methods = cls.getMethods();

        // Printing method names
        for (Method method:class_methods){
            String append = "";
            for(Class clazz : method.getParameterTypes()){
                if (clazz.isPrimitive()){
                    if(clazz.equals(int.class)){
                        clazz = Integer.class;
                    }
                }
                if(append.isEmpty()){
                    append += "(" + clazz.getName();
                }else {
                    append += "," + clazz.getName();
                }
            }
            append+=")";
            System.out.println(ConsoleColors.BLUE+method.getName()+append+ConsoleColors.RESET);
            //Check if methods at this point has an entry
            if(!methods.containsKey(method.getName())){
                methods.put(method.getName(), new LinkedList<Method>());
            }
            this.methods.get(method.getName()).push(method);
        }

        Field[] fields = cls.getFields();
        for (Field field : fields) {
            System.out.println(ConsoleColors.BLUE+field.getName()+ConsoleColors.RESET);
            this.fields.put(field.getName(), field);
        }

    }

    public Method getMethod(String name, Class<?> ... params){
        try {
            String append = "";
            for(Class clazz : params){
                if(append.isEmpty()){
                    append += "(" + clazz.getName();
                }else {
                    append += "," + clazz.getName();
                }
            }
            if(methods.containsKey(name+append+")")){
                //TODO
                return instance.getDeclaredMethod(name+append+")", params);
            }
        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
            for(int i = 0; i < params.length; i++){
                params[i] = Object.class;
            }
            String append = "";
            for(Class clazz : params){
                if(append.isEmpty()){
                    append += "(" + clazz.getName();
                }else {
                    append += "," + clazz.getName();
                }
            }
            try {
                //Try as objects
                return instance.getDeclaredMethod(name+append+")", params);
            } catch (NoSuchMethodException ex) {
//                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public Object getInstance(){
        return System.out;
    }



    //Maybe make this an object
    public Object invoke(String methodName, Object instance, Object[] params) throws NoSuchMethodException {
        if(methods.containsKey(methodName)){
            for(Method method : methods.get(methodName)){
                if(this.signaturesMatch(method.getParameterTypes(), params)) {
                    try {
                        System.out.println(method);
                        System.out.println(params);
                        return method.invoke(instance, params);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            throw new NoSuchMethodException();
        }
        return null;
    }

    private boolean signaturesMatch(Class<?>[] parameterTypes, Object[] params) {
        if(parameterTypes.length != params.length){
            return false;
        }
        for(int i = 0; i < parameterTypes.length; i++){
            if(params[i] != null) {
                if (!(parameterTypes[i].getClass().isInstance(params[i].getClass()))) {
                    System.out.println(parameterTypes[i].getClass() + " != " + params[i].getClass());
                    return false;
                }
            }
        }
        return true;
    }
}
