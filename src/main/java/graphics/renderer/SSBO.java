package graphics.renderer;

import org.lwjgl.opengl.GL46;

public class SSBO {
    private int id;
    private int location;
    private float[] data;
    private int numElements;
    private EnumGLDatatype datatype;

    private static final byte ALIGNMENT = 4;

    protected SSBO(int id, int location, EnumGLDatatype datatype){
        this.id       = id;
        this.location = location;
        this.datatype = datatype;
    }

    public void flush(){
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, id);
        GL46.glBufferData(GL46.GL_SHADER_STORAGE_BUFFER, this.data, GL46.GL_STATIC_DRAW);
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, location, id);
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void allocate(int size){
        int offset = 0;
        int deficit = this.datatype.sizePerVertex % ALIGNMENT;
        if(deficit != 0){
            //Not aligned correctly
            offset = deficit * size;
        }
        this.data = new float[(size * datatype.sizePerVertex) + offset];
    }

    public void setData(int index, float[] data){
        if(data.length != datatype.sizePerVertex){
            //No work
            System.err.println("Error: Tried to load " + data.length + "floats into an SSBO expecting " + this.datatype.sizePerVertex + " floats per element.");
        }else{
            put(index, data);
        }
    }

    /*
    SSBOs align data to the nearest 4floats if we have a primative that is less that 4 floats, we need to insert padding data to maintain alignment when we try to access our data.
     */
    private void put(int index, float[] data){
        int deficit = data.length % ALIGNMENT;
        //Check if our data is able to be aligned to the GPU alignement
        if(deficit != 0){
            int padding = (ALIGNMENT - deficit);
            int offset = (index * (datatype.sizePerVertex + padding));
            for(int i = 0; i < data.length; i++){
                this.data[offset + i] = data[i];
            }
            for(int i = 0; i < padding; i++){
                //Pad trailing data with 0
                this.data[offset + data.length + i] = 0;
            }
        }else{
            // There is no misalignment, we can simply write directly to memory
            for(int i = 0; i < data.length; i++){
                this.data[(index * this.datatype.sizePerVertex) + i] = data[i];
            }
        }
    }
}
