package graphics.renderer;

import entity.Entity;
import models.Model;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;
import particle.Particle;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class VAO {

    private final int VAO_DI;

    private HashMap<String, EnumGLDatatype> uniforms = new HashMap<>();

    /*
        NOTE the only thing that is crucial is that index 0 of this array contains the triangulated model data such that VBO[0] / 3 = #of trinagles to render.
     */
    private LinkedHashMap<String, VBO> vbo_indices  = new LinkedHashMap<>();
    private LinkedHashMap<String, VBO> vbo_uniforms = new LinkedHashMap<>();

    private int numFaces = 0;

    protected VAO(Model model){
        //Create our vao
        VAO_DI = VAOManager.getInstance().genVertexArrays();
        GL46.glBindVertexArray(VAO_DI);

        //Our attributes
        String[] attributes = new String[]{
            "vPosition",
            "vNormal",
            "vTangent",
            "vBitangent",
            "vTexture",
        };

        //Any uniform loads go here.
        registerUniform("pos", EnumGLDatatype.VEC3);

        //Load some data in there!
        Handshake shape = model.getHandshake();

        int index = 0;

        for(String name : attributes) {
            //IF this attribute is contained in our Handshake
            if(shape.hasAttribute(name)) {
                //Get the attribute
                FloatBuffer buffered_data = ((FloatBuffer) shape.getAttribute(name)).asReadOnlyBuffer();
                float[] floatBuffer = new float[buffered_data.remaining()];
                buffered_data.get(floatBuffer);

                int size = shape.getBytesPerVertex(name);

                //Load our float data into a VBO that is attached to our VAO.
                int vbo_data = GL46.glGenBuffers();
                GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_data);
                GL46.glBufferData(GL46.GL_ARRAY_BUFFER, floatBuffer, GL46.GL_STATIC_DRAW);
                GL46.glVertexAttribPointer(index, size, GL46.GL_FLOAT, false, 0, 0);

                //make a VBO to hold our data
                VBO vbo = new VBO(vbo_data, size, shape.getAttributeDataLength(name));

                vbo_indices.put(name, vbo);

                //Check if this is the first time, if it is define the face count.
                if (index == 0) {
                    //NOTE we assume that index 0 is the VBO that storing indices of this model.
                    //NumFaces = totalNumberOfVertices / 3(vertices per face)
                    numFaces = vbo.dataSize / 3;
                }
            }else{
                System.err.println("[VAO] ERROR: Requested attribute:" + name + " however that is not an attribute stored in models handshakes. Is this supposed to be a uniform?");
            }
            index++;
        }

        //Load Uniforms
        for(String name : this.uniforms.keySet()) {
            //Load our float data into a VBO that is attached to our VAO.
            int vbo_data = GL46.glGenBuffers();
            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_data);
            GL46.glBufferData(GL46.GL_ARRAY_BUFFER, new float[]{}, GL46.GL_STREAM_DRAW);
            GL46.glVertexAttribPointer(index, this.uniforms.get(name).sizePerVertex, GL46.GL_FLOAT, false, 0, 0);

            //make a VBO to hold our data
            VBO vbo = new VBO(vbo_data, this.uniforms.get(name).sizePerVertex, -1);

            //Buffer this.
            vbo_uniforms.put(name, vbo);
            //increase index.
            index++;
        }

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    private void registerUniform(String uniformName, EnumGLDatatype datatype){
        this.uniforms.put(uniformName, datatype);
    }

    /*
        Prepare for this VAO to be rendered.
     */
    public void render(Collection<Entity> toRender){
        // --------------  Prepare / Update Buffers  ----------------
        //Bind our VAOID into GL
        GL46.glBindVertexArray(VAO_DI);

        int renderCount = toRender.size();

        for(VBO vbo : vbo_uniforms.values()){
            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo.VBO_ID);
            //Lets build our buffer!

            float[] uniformData = new float[renderCount * vbo.vertexStride];

//            float[] transform_buffer = new float[]{
//                1, 0, 0, 0,
//                0, 1, 0, 0,
//                0, 0, 1, 0,
//                0, 0, 0, 1
//            };
            int entityIndex = 0;
            for (Entity e : toRender) {
                Vector3f pos = e.getPosition();
//                for(int i = 0; i < 16; i++) {
//                    uniformData[(entityIndex * vbo.vertexStride) + i] = transform_buffer[i];
//                }
                uniformData[(entityIndex * vbo.vertexStride) + 0] = pos.x;
                uniformData[(entityIndex * vbo.vertexStride) + 1] = pos.y;
                uniformData[(entityIndex * vbo.vertexStride) + 2] = pos.z;

                entityIndex++;
            }

            GL46.glBufferData(GL46.GL_ARRAY_BUFFER, uniformData, GL46.GL_STREAM_DRAW);
        }

        // --------------       RENDERING CODE       ----------------
        //Keep track of our load index
        int index = 0;

        //Iterate through our attributes and enable them
        for(String attribute : vbo_indices.keySet()){
            // 1rst attribute buffer : vertices
            GL46.glEnableVertexAttribArray(index);
            //Now we need to tell OpenGL how big to stride each instance of draw over this VBO
            GL46.glVertexAttribDivisor(index, index == 0 ? 0 : 1);
            index++;
        }
        for(String uniform : vbo_uniforms.keySet()){
            // 1rst attribute buffer : vertices
            GL46.glEnableVertexAttribArray(index);
            //Now we need to tell OpenGL how big to stride each instance of draw over this VBO
            GL46.glVertexAttribDivisor(index,1);
            index++;
        }

        //Actual draw call.
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLES, 0, numFaces, renderCount);

        // --------------        CLEANUP CODE        ----------------
        //Free up our indices.
        index = 0;
        for(String attribute : vbo_indices.keySet()){
            GL46.glDisableVertexAttribArray(index);
            index++;
        }
        for(String uniform : vbo_uniforms.keySet()){
            GL46.glDisableVertexAttribArray(index);
            index++;
        }

        //unbind us.
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

}
