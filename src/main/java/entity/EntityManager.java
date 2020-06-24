package entity;

import com.google.gson.JsonObject;
import org.joml.Intersectionf;
import org.joml.RayAabIntersection;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EntityManager {

    private static EntityManager entityManager;

    //This is the list of all entities in the scene.
    private LinkedList<Entity> entities = new LinkedList<Entity>();
    private LinkedList<Entity> toAdd    = new LinkedList<Entity>();
    private LinkedList<Entity> toRemove = new LinkedList<Entity>();
    private HashMap<EnumEntityType, LinkedList<Entity>> typedEntities = new HashMap<EnumEntityType, LinkedList<Entity>>();

    //On remove see if a registered parent was removed, if it was, remove all children.
    private HashMap<Entity, LinkedList<Entity>> links = new HashMap<>();

    //Lock for locking our entity set
    private Lock lock;

    //Singleton design pattern
    private EntityManager(){
        lock = new ReentrantLock();

        for(EnumEntityType type : EnumEntityType.values()){
            typedEntities.put(type, new LinkedList<Entity>());
        }
    }

    public void addEntity(Entity entity){
        lock.lock();
        try {
            this.toAdd.add(entity);
        } finally {
            lock.unlock();
        }
    }

    public void clearEntities(){
        this.entities.clear();
        for(LinkedList<Entity> typeList : typedEntities.values()){
            typeList.clear();
        }
    }

    public LinkedList<Entity> getEntities(){
        return this.entities;
    }

    public synchronized LinkedList<Entity> getEntitiesOfType(EnumEntityType type, EnumEntityType ... types){
        if(types.length == 0) {
            return this.typedEntities.get(type);
        }else{
            LinkedList<Entity> entities = new LinkedList<>(this.typedEntities.get(type));
            for(EnumEntityType additionalType : types){
                entities.addAll(this.typedEntities.get(additionalType));
            }
            return entities;
        }
    }

    //Once per insert
    private void sortEntities() {
        Collections.sort(entities, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
            return e1.getModel().getID() - e2.getModel().getID();
            }
        });
    }

    //Update all entities
    public void update(double delta){
        sync();
        for(Entity e : entities){
            e.selfUpdate(delta);
        }
    }

    //Singleton functions
    public static void initialize(){
        if(entityManager == null){
            entityManager = new EntityManager();
        }
    }

    public static EntityManager getInstance(){
        return entityManager;
    }

    public LinkedList<Entity> getHitEntities(Vector3f pos, Vector3f dir, EnumEntityType ... types){

        //Invert Pos
        pos = new Vector3f(pos).mul(1, 1, -1);

        LinkedList<Entity> check = new LinkedList<Entity>();
        LinkedList<Entity> hits  = new LinkedList<Entity>();
        //TODO
        //Calculate view frustum to filter

        if(types.length == 0){
            check.addAll(this.entities);
        }else{
            for(EnumEntityType type : types){
                check.addAll(getEntitiesOfType(type));
            }
        }

        for(Entity e : check){
            if(Intersectionf.testRayAab(new Vector3f(pos), new Vector3f(dir), new Vector3f(e.getPosition()).sub(1, 1, 1), new Vector3f(e.getPosition()).add(1, 1, 1))){
                hits.add(e);
            }
        }

        return hits;
    }

    public LinkedList<Entity> getHitEntities(Vector3f pos, Vector3f dir, Collection<Entity> check){
        //Invert Pos
        pos = new Vector3f(pos).mul(1, 1, -1);

        LinkedList<Entity> hits  = new LinkedList<Entity>();

        for(Entity e : check){
            if(Intersectionf.testRayAab(new Vector3f(pos), new Vector3f(dir), new Vector3f(e.getPosition()).sub(1, 1, 1), new Vector3f(e.getPosition()).add(1, 1, 1))){
                hits.add(e);
            }
        }
        return hits;
    }

    public Entity generateEntityOfType(EnumEntityType type, JsonObject saveData){
        int floorX = 0;
        int floorY = 0;

        if(saveData.has("x")){
            floorX = saveData.get("x").getAsInt();
        }

        if(saveData.has("y")){
            floorY = saveData.get("y").getAsInt();
        }

        switch (type){
//            case MOLE:{
//                return new Mole(floorX, floorY);
//            }
//            case AVOCADO:{
//                return new Avocado(floorX, floorY);
//            }
//            case ONION:{
//                return new Onion();
//            }
//            case STONE:{
//                return new Stone();
//            }
//            case ICE:{
//                return new Ice(floorX, floorY);
//            }
//            case WATER:{
//                return new Water();
//            }
            default:{
                System.out.println("Undefined case for type:" + type);
//                System.exit(1);
            }
        }
        return null;
    }

    public void sync() {
        if(toAdd.size() > 0 || toRemove.size() > 0) {
            lock.lock();
            try {
                for (Entity e : toAdd) {
                    this.entities.add(e);
                    e.onAdd();
                    this.typedEntities.get(e.getType()).add(e);
                }
                toAdd.clear();
                for (Entity e : toRemove) {
                    this.entities.remove(e);
                    this.typedEntities.get(e.getType()).remove(e);
                }
                toRemove.clear();
                sortEntities();
            } finally {
                lock.unlock();
            }
        }
    }

    public void removeEntity(Entity toRemove) {
        lock.lock();
        try {
            if(this.entities.contains(toRemove)) {
                parentRemoveHelper(toRemove);
            }
        } finally {
            lock.unlock();
        }
    }

    private void parentRemoveHelper(Entity parent){
        this.toRemove.add(parent);
        if(this.links.containsKey(parent)){
            LinkedList<Entity> links = this.links.get(parent);
            for(Entity child : links){
                parentRemoveHelper(child);
            }
            this.links.remove(links);
        }
    }

    public void link(Entity parent, Entity child) {
        if(!this.links.containsKey(parent)){
            this.links.put(parent, new LinkedList<Entity>());
        }
        this.links.get(parent).push(child);
    }

    public int getSize(){
        return this.entities.size();
    }
}
