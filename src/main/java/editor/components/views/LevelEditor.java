package editor.components.views;

import camera.CameraManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import editor.components.UIComponet;
import editor.components.container.Image;
import entity.Entity;
import entity.EntityManager;
import entity.component.Attribute;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import graphics.sprite.SpriteSheet;
import imgui.ImGui;
import imgui.ImInt;
import imgui.ImString;
import input.Keyboard;
import input.MousePicker;
import models.ModelManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import serialization.SerializationHelper;
import util.Callback;
import util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class LevelEditor extends UIComponet {

    private Callback mouseCallback;

    private SpriteSheet sheet;

    //Variables
    int selectedTexture = 0;
    boolean pressed = false;

    //Level width and height
    int levelWidth  = 12;
    int levelHeight = 12;
    int MAX_LAYERS  = 4;

    //Layers
//    LinkedList<Entity>[] layers = new LinkedList[MAX_LAYERS];
    //Tiles
    HashMap<Integer, ArrayList<Entity>> tiles = new HashMap<>();

    //ImGui vars
    ImInt Im_layer     = new ImInt(0);
    ImString levelname = new ImString("level.tek");

    int sprite_width;
    int sprite_height;

    LinkedList<SpriteSheet> spriteSheets = new LinkedList<>();

    public LevelEditor(){
        //Add sprite sheets
        spriteSheets.add(SpriteBinder.getInstance().loadSheet("Tileset.png", 16, 16));
        //Genreate mouse callback
        mouseCallback = new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(LevelEditor.super.isVisable()) {
                    int button = (int) objects[0];
                    int action = (int) objects[1];

                    pressed = (action == GLFW.GLFW_PRESS);

                    raycastForTile();
                }
                return null;
            }
        };

        levelname.resize(32);

        this.sheet = spriteSheets.getFirst();
    }

    public void addSpriteSheet(SpriteSheet sheet){
        this.spriteSheets.addLast(sheet);
    }

    private void raycastForTile() {
        if(pressed && this.isVisable()){
            //Raycast to plane
            Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
            //Get hit pos
            if(pos != null){
                //Get world pos
                Vector3i worldPos = translateToWorld(pos);
                int hash = worldPos.hashCode();
                //Lookup to see if entity exists
                if(!tiles.containsKey(hash)){
                    //Create new entity
                    Entity tile = new Entity();

                    //Set props
                    tile.setModel(ModelManager.getInstance().loadModel("quad.tek").getFirst());
                    tile.setTexture(selectedTexture);
                    tile.setPosition(new Vector3f(worldPos).add(new Vector3f(0, 0.01f * Im_layer.get(), 0)));

                    tile.addAttribute(new Attribute<Integer>("zIndex", Im_layer.get()));

                    //Gen the linked list
                    ArrayList<Entity> entities = new ArrayList<Entity>(MAX_LAYERS);
                    for(int i = 0; i < MAX_LAYERS; i++){
                        entities.add(i, null);
                    }
                    entities.add(Im_layer.get(), tile);

                    //Buffer
                    tiles.put(hash, entities);

                    //Add to world
                    EntityManager.getInstance().addEntity(tile);
                }else{
                    Entity tile = tiles.get(hash).get(Im_layer.get());
                    if(tile != null){
                        //Set texture from buffer
                        tile.setTexture(selectedTexture);
                    }else{
                        //Create new entity
                        tile = new Entity();

                        //Set props
                        tile.setModel(ModelManager.getInstance().loadModel("quad.tek").getFirst());
                        tile.setTexture(selectedTexture);
                        tile.setPosition(new Vector3f(worldPos).add(new Vector3f(0, 0.01f * Im_layer.get(), 0)));

                        tile.addAttribute(new Attribute<Integer>("zIndex", Im_layer.get()));

                        //Add to world
                        EntityManager.getInstance().addEntity(tile);

                        //Buffer
                        tiles.get(hash).add(Im_layer.get(), tile);
                    }
                }
            }
        }
    }


    @Override
    public void onAdd() {
        MousePicker.getInstance().addCallback(mouseCallback);
    }

    @Override
    public void onRemove() {
        MousePicker.getInstance().removeCallback(mouseCallback);
    }

    @Override
    public void self_update(double delta) {
//        raycastForTile();
        if(Keyboard.getInstance().isKeyPressed(Keyboard.DELETE, Keyboard.BACKSPACE)){
            if(!levelname.get().isEmpty()) {
                String name = levelname.get().substring(0, levelname.get().length() - 1);
                levelname.set(name);
            }
        }
    }

    public void getTexture(){

    }

    @Override
    public void selfRender() {
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        ImGui.inputText("SaveLevel", levelname);
        if(ImGui.button("Save", 32, 32)){
            this.save();
        }
        //Level sets
        if (ImGui.beginCombo("", this.sheet.getName())){
            for (SpriteSheet sheet : spriteSheets){
                boolean is_selected = (this.sheet.getName().equals(sheet.getName()));
                if (ImGui.selectable(sheet.getName(), is_selected)){
                    this.sheet = sheet;
                }
                if (is_selected){
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        //Config
        ImGui.inputInt("Layer", Im_layer, 1);
        //Pre Textures
        ImGui.newLine();
        //Textures
        int index = 0;
        for(Sprite sprite : this.sheet.getSprites()){
            //If there is data here
            if(sprite != null){
                if (ImGui.imageButton(sprite.getTextureID(), 64, 64)){
                    selectedTexture = sprite.getTextureID();
                }
            }else{
                //Insert blank
                if (ImGui.imageButton(0, 64, 64)){
                    selectedTexture = -1;
                }
            }
            index++;
            if(index % this.sheet.getColumns() != 0){
                ImGui.sameLine();
            }
        }
        ImGui.endChild();
    }

    @Override
    public void self_post_render() {

    }

    public Vector3i translateToWorld(Vector3f pos){
        return new Vector3i(( Math.round(pos.x / 2f)), ( Math.round(pos.y / 2f)),  (Math.round(pos.z / 2f))).mul(2);
    }

    public void save(){
        //Out save object. This will be a .tek file eventually.
        JsonObject out = new JsonObject();
        //First lets get our output object into the json format that we expect it to be in.

        //List of all sprites
        HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
        HashMap<String, LinkedList<Entity>> entities = new HashMap<String, LinkedList<Entity>>();

        //We are going to iterate through all tiles and generate a large image representing the tile.
        for(ArrayList<Entity> tileCluster : tiles.values()){
            //For each of our layers in layerHeight
            int k = 0;
            //Get the base entity
            Entity baseEntity = tileCluster.get(k);
            if(baseEntity == null){
                for(; k < MAX_LAYERS; k++){
                    baseEntity = tileCluster.get(k);
                    if(baseEntity != null){
                        break;
                    }
                }
            }
            //Make sure the base sprite is not null.
            Sprite baseSprite = SpriteBinder.getInstance().getSprite(baseEntity.getTextureID());
            if(baseSprite != null) {
                Sprite sprite = new Sprite(baseSprite.getWidth(), baseSprite.getHeight());
                sprite.overlay(baseSprite);
                for (; k < MAX_LAYERS; k++) {
                    Entity layer = tileCluster.get(k);
                    //Overlay each parent sprite.
                    if (layer != null) {
                        sprite.overlay(SpriteBinder.getInstance().getSprite(layer.getTextureID()));
                        EntityManager.getInstance().removeEntity(layer);
                    }
                }
                sprite.flush();

                //This is our hash
                String spriteHash = sprite.getHash();

                //Only add one instance of this sprite to the map.
                if (!sprites.containsKey(spriteHash)) {
                    sprites.put(spriteHash, sprite);
                    entities.put(spriteHash, new LinkedList<Entity>());
                }

                //Now add base entity to a list indexed by id. we will know how far to offset this texture in the texture atlas later.
                entities.get(spriteHash).addLast(baseEntity);
            }
        }

        //Determine how many rows and columns we need
        int row_col = (int) Math.ceil(Math.sqrt(sprites.size()));

        //sprite width and height in pixels
        int s_width  = this.sheet.getSpriteWidth();
        int s_height = this.sheet.getSprightHeight();

        //Now we do our texture atlas generation
        Sprite textureAtlas = new Sprite(row_col * s_width, row_col * s_height);
        int index = 0;
        for(Sprite sprite : sprites.values()){
            int x = (index % row_col)*s_width;
            int y = ((int) Math.floor(index / row_col)) * s_height;

            //get our one instance to sprite hash
            String spriteHash = sprite.getHash();

            //We need to modify our entities using this old texture at this point, to now lookup from atlas position defined by x,y we will do this with attributes
            for(Entity e : entities.get(spriteHash)){
                //new length of a texture.
                float scaleValue = 1.0f / (float) row_col;
                //set position
                e.addAttribute(new Attribute<Vector2f>("t_offset", new Vector2f((x / s_width) * scaleValue, (y / s_height) * scaleValue)));
                //set texture scale
                e.addAttribute(new Attribute<Vector2f>("t_scale" , new Vector2f(scaleValue, scaleValue)));

                //set real scale
                e.setScale(new Vector3f((this.sheet.getSpriteWidth() / 16f), 0, (this.sheet.getSprightHeight() / 16f)));

                //Transform each entity by its transformation then offset its texture coordinates by the above.
                JsonObject serializedEntity = e.serialize();

                //Getting the attributes of the model for this entity
                JsonObject attributes = serializedEntity.get("model").getAsJsonObject().get("handshake").getAsJsonObject().get("attributes").getAsJsonObject();

                JsonArray posBytes = attributes.get("vPosition").getAsJsonObject().get("bytes").getAsJsonArray();
                JsonArray texBytes = attributes.get("vTexture").getAsJsonObject().get("bytes").getAsJsonArray();

                //Each of attributes keys is an array of model data. The pts are in vPositions
                //3 bytes per vec
                for(int i = 0; i < posBytes.size() / 3; i++){
                    float pos_x = posBytes.get(i * 3 + 0).getAsFloat();
                    float pos_y = posBytes.get(i * 3 + 1).getAsFloat();
                    float pos_z = posBytes.get(i * 3 + 2).getAsFloat();

                    Vector4f position = new Vector4f(pos_x, pos_y, pos_z, 1f);
                    position = e.getTransform().transform(position);

                    posBytes.set((i * 3 + 0), new JsonPrimitive(position.x()));
                    posBytes.set((i * 3 + 1), new JsonPrimitive(position.y()));
                    posBytes.set((i * 3 + 2), new JsonPrimitive(position.z()));
                }

                //2 bytes per vec
                for(int i = 0; i < texBytes.size() / 2; i++){
                    float tex_x = texBytes.get(i * 2 + 0).getAsFloat();
                    float tex_y = texBytes.get(i * 2 + 1).getAsFloat();

                    Vector2f textureCoords = new Vector2f(tex_x, tex_y);
                    //Go from 0-1 space to 0-scale
                    textureCoords.mul(new Vector2f(scaleValue, scaleValue));
                    //Offset by n * scale
                    textureCoords.add(new Vector2f((x / s_width) * scaleValue, (y / s_height) * scaleValue));

                    texBytes.set((i * 2 + 0), new JsonPrimitive(textureCoords.x()));
                    texBytes.set((i * 2 + 1), new JsonPrimitive(textureCoords.y()));
                }

                //Merge these bad boys together.
                out = SerializationHelper.merge(out, serializedEntity);

            }

            textureAtlas.overlay(sprite, x, y, s_width, s_height);
            index++;

        }
        textureAtlas.flush();

        //At this point texture atlas is generated and the entities have had their attributes modified.
        out.add("image", textureAtlas.serialize());

        System.out.println(sprites.size()+" unique tiles created.");

        String levelName = levelname.get();
        if(!levelName.endsWith(".tek")){
            levelName += ".tek";
        }

        //Save our serialized output level
        StringUtils.write(out.toString(), "/levels/"+levelName);
    }

    @Override
    public String getName(){
        return "Level Editor";
    }
}
