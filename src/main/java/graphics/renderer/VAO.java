package graphics.renderer;

import java.util.HashMap;

public class VAO {
    private final int ID;
    private int size = 0;

    private HashMap<Integer, Integer> sizes = new HashMap<>();

    public VAO() {
        ID = VAOManager.getInstance().createVAO();
    }

    public void addVBO(int size, float[] data){
        sizes.put(this.size, data.length);
        VAOManager.getInstance().addVBO(this, size, data);
    }

    public int getSize(){
        return this.size;
    }

    public void setSize( int size){
        this.size = size;
    }

    public int getID(){
        return this.ID;
    }

    public int getVBOLength(int index){
        return sizes.get(index);
    }
}
