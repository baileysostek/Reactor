package graphics.renderer;

import java.util.HashMap;

//Metadata container for the metadata in shaders
public class Shader {
    private final String version;
    private final String name;
    private final int    programID;

    private String[] attributes;
    private HashMap<String, EnumGLDatatype> uniforms = new HashMap<>();

    protected Shader(String name, int programID, String version){
        this.name = name;
        this.programID = programID;
        this.version = version;
    }

    public Shader setAttributes(String[] attributes){
        this.attributes = attributes;
        return this;
    }

    public String[] getAttributes(){
        return this.attributes;
    }

    public EnumGLDatatype getUniform(String uniformName){
        return this.uniforms.get(uniformName);
    }



    public void addUniform(String name, String datatype){
        //convert string datatype to datatype
        EnumGLDatatype type = EnumGLDatatype.valueOf(datatype.toUpperCase());
        if(type != null){//if there is a type it means that this uniform was bound correctly
            System.out.println("Adding uniform:"+name+" of type:"+type);
            uniforms.put(name, type);
        }else{
            System.err.println("No datatype like " + datatype + " for uniform " + name + " could be found.");
        }
    }

    public void removeAttribute(String attribute) {
        int foundIndex = 0;
        for(String attrib : attributes){
            if(attrib == attribute){
                String[] attribModified = new String[attributes.length - 1];
                int index = 0;
                for(int i = 0; i < attributes.length; i++){
                    if(i == foundIndex){
                        i++;
                        if(i >= attributes.length){
                            this.attributes = attribModified;
                            return;
                        }
                    }
                    attribModified[index] = attributes[i];
                    index++;
                }

                this.attributes = attribModified;

                return;
            }
            foundIndex++;
        }
    }
}
