package graphics.sprite;

import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import math.Maths;

import java.util.ArrayList;
import java.util.LinkedList;

public class Sprite {
    private int       textureID = 0;
    private int       width     = 0;
    private int       height    = 0;

    public  Vector3f  position  = new Vector3f();
    private Vector3f  rotation  = new Vector3f();
    private Vector2f  scale     = new Vector2f(1.0f, 1.0f);

    private float hypotenuse = 0;
    private float hypSquared = 0;
    private float radius     = 0;

    private int ppi = 16;

    private boolean render = true;

    //Uniform information default values
    private Vector2f  uiScale       = new Vector2f(1.0f, 1.0f);
    private Vector2f  textureScale  = new Vector2f(1.0f, 1.0f);//Used when rendering from an atlas
    private Vector2f  textureOffset = new Vector2f(0.0f, 0.0f);

    private Vector2f  screenOffset = new Vector2f(0.0f, 0.0f);

    private ArrayList<Sprite> children = new ArrayList<>();

    private LinkedList<Sprite> linkedSprites = new LinkedList<>();

    protected Sprite(int textureID, int width, int height){
        this.textureID = textureID;
        this.width = width;
        this.height = height;
        this.uiScale.x = (width  / Renderer.PIXELS_PER_METER);
        this.uiScale.y = (height / Renderer.PIXELS_PER_METER);

        hypSquared = (float) (Math.pow(uiScale.x, 2) + Math.pow(uiScale.y, 2));
        hypotenuse = (float) Math.sqrt(Math.pow(uiScale.x, 2) + Math.pow(uiScale.y, 2));
        radius = hypotenuse / 2.0f;
    }

    public Sprite(Sprite sprite){
        this.textureID = sprite.getTextureID();
        this.width = sprite.getWidth();
        this.height = sprite.getHeight();
        this.hypSquared = sprite.getHypSquared();
        this.hypotenuse = sprite.getHypotenuse();
        this.position = new Vector3f(sprite.getPosition());
        this.rotation = new Vector3f(sprite.getRotation());
        this.scale = sprite.scale;
        this.uiScale = new Vector2f(sprite.uiScale);
        this.textureScale = new Vector2f(sprite.textureScale);
        this.textureOffset = new Vector2f(sprite.textureOffset);
        this.ppi = sprite.getPPI();
        //do not transfer children for now
//        this.children = sprite.children;
    }

    public void translate(Vector3f vec){
        this.position.add(vec);
    }

    public void rotate(Vector3f rot){
        this.rotation.add(rot);
        updateChildRotation(rot, this.children);
    }

    private void updateChildRotation(Vector3f rot, ArrayList<Sprite> children) {
        for(Sprite child : children){
            child.rotate(rot);
        }
    }

    public Vector3f getRotation(){
        return this.rotation;
    }

    public void setRotaiton(Vector3f rot){
        this.rotation = new Vector3f(rot);
    }

    public void scale(Vector2f scale){
        this.scale = scale;
    }

    public Matrix4f getTransform(){
        return Maths.createTransformationMatrix(position, rotation, scale);
    }

    public ArrayList<Sprite> getChildren(){
        return this.children;
    }

    public void addChildren(ArrayList<Sprite> children){
        if(children != null) {
            for (Sprite sprite : children) {
                addChild(sprite);
            }
        }
    }

    public void addChild(Sprite child){
        child.setPPI(this.getPPI());
        ArrayList<Sprite> newChildren = new ArrayList<Sprite>(this.children.size() + 1);
        for(Sprite i : this.children){
            newChildren.add(i);
        }
        newChildren.add(child);
        this.children = newChildren;
        //TODO optimise
        addChildren(child.getChildren());
    }

    public void removeChild(Sprite child){
        this.children.remove(child);
        for(Sprite sprite: linkedSprites){
            sprite.removeChild(child);
        }
    }

    public void setPosition(Vector3f position) {
        alignChildren(position, children);
        this.position = position;
    }

    private void alignChildren(Vector3f position, ArrayList<Sprite> children) {
        for(Sprite child : children) {
            Vector3f delta = new Vector3f(position).sub(new Vector3f(child.position));
            child.position.add(delta);
            alignChildren(position, child.children);
        }
    }

    public void setUiScale(int width, int height){
        this.uiScale.x = (width  / Renderer.PIXELS_PER_METER);
        this.uiScale.y = (height / Renderer.PIXELS_PER_METER);
//        this.width = width;
//        this.height = height;
    }

    public void setPPI(int ppi){
        this.ppi = ppi;
        this.uiScale.x = ((float)this.width  / (float)ppi);
        this.uiScale.y = ((float)this.height / (float)ppi);
        for(Sprite child : this.children){
            child.setPPI(ppi);
        }
    }

    public void setTextureScale(float width, float height) {
        this.textureScale.x = width;
        this.textureScale.y = height;
    }

    //Getters
    public Vector3f getPosition(){
        return new Vector3f(this.position);
    }
    public Vector2f getUiScale(){
        return this.uiScale;
    }
    public Vector2f getTextureScale() {
        return textureScale;
    }
    public Vector2f getTextureOffset() {
        return textureOffset;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }

    public int getTextureID(){
        return this.textureID;
    }

    public void setTextureOffset(int x, int y) {
        this.textureOffset.x = x;
        this.textureOffset.y = y;
    }

    public void retexture(Sprite sprite){
        if(this.textureID != sprite.getTextureID()) {
            this.textureID = sprite.textureID;
        }
    }

    public int getPPI(){
        return this.ppi;
    }

    public float getHypSquared() {
        return this.hypSquared;
    }

    public float getRadius(){
        return this.radius * Math.max(this.getUiScale().x(),  this.getUiScale().y()) * Math.max(this.getScale().x(),  this.getScale().y());
    }

    public float getHypotenuse(){
        return this.hypotenuse;
    }

    public void setShouldRender(boolean render){
        this.render = render;
    }

    public boolean shouldRender() {
        return this.render;
    }

    public Vector2f getScale() {
        return this.scale;
    }

    public Vector2f getScreenOffset(){
        return this.screenOffset;
    }

    public void setScreenOffset(Vector2f offset){
        this.screenOffset = offset;
    }

    public void linkChildren(Sprite sprite){
        this.linkedSprites.add(sprite);
    }
}
