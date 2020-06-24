package graphics.sprite;

import java.util.LinkedList;

public class SpriteSheet {

    //Sprites themselves
    private LinkedList<Sprite> sprites = new LinkedList<>();
    int columns = 0;
    int rows    = 0;
    String name = "";

    //Info about the sub sprites.
    int s_width;
    int s_height;

    public SpriteSheet(int col, int row, int s_width, int s_height, String name, LinkedList<Sprite> sprites){
        this.sprites = sprites;
        this.columns = col;
        this.rows = row;
        this.name = name;

        //set the sprite dimensions
        this.s_width = s_width;
        this.s_height = s_height;
    }

    public int getColumns(){
        return this.columns;
    }

    public int getRows(){
        return this.rows;
    }

    public LinkedList<Sprite> getSprites(){
        return this.sprites;
    }

    public String getName() {
        return this.name;
    }

    public int getSpriteWidth(){
        return this.s_width;
    }

    public int getSprightHeight(){
        return this.s_height;
    }
}
