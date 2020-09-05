package entity;

import com.google.gson.JsonObject;
import org.joml.Intersectionf;
import org.joml.Vector3f;
import platform.EnumDevelopment;
import platform.PlatformManager;

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
    private HashMap<EnumEntityType, LinkedList<Entity>> sceneEntities = new HashMap<EnumEntityType, LinkedList<Entity>>();

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
            //Add the entity
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
                if(e1.getModel() != null && e2.getModel() != null){
                    return e1.getModel().getID() - e2.getModel().getID();
                }else{
                    if(e1.getModel() != null){
                        return 0 - e2.getModel().getID();
                    }
                    if(e2.getModel() != null){
                        return 0 - e1.getModel().getID();
                    }
                }
                return 0;
            }
        });
    }

    //Update all entities
    public void update(double delta){
        sync();
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.PRODUCTION)) {
            for (Entity e : entities) {
                e.selfUpdate(delta);
            }
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
        pos = new Vector3f(pos).mul(-1, -1, -1);

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
            if(Intersectionf.testRayAab(new Vector3f(pos), new Vector3f(dir), new Vector3f(e.getPosition()).sub(new Vector3f(1, 0, 1).mul(e.getScale())).sub(0, 0.1f, 0), new Vector3f(e.getPosition()).add(new Vector3f(1, 0, 1).mul(e.getScale())).add(0, 0.1f, 0))){
                hits.add(e);
            }
        }

        return hits;
    }

    public LinkedList<Entity> getHitEntities(Vector3f pos, Vector3f dir, Collection<Entity> check){
        //Invert Pos
        pos = new Vector3f(pos).mul(-1, -1, -1);

        LinkedList<Entity> hits  = new LinkedList<Entity>();

        for(Entity e : check){
            //TODO calc hit based off AABB broadphase, then tri-test narrow-phase
            if(Intersectionf.testRayAab(new Vector3f(pos), new Vector3f(dir), new Vector3f(e.getPosition()).sub(new Vector3f(1, 0, 1).mul(e.getScale())).sub(0, 0.1f, 0), new Vector3f(e.getPosition()).add(new Vector3f(1, 0, 1).mul(e.getScale()).add(0, 0.1f, 0)))){
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

    private void sync() {
        if(toAdd.size() > 0 || toRemove.size() > 0) {
            lock.lock();
            try {
                for (Entity e : toAdd) {
                    this.entities.add(e);
                    e.onAdd();
                    this.typedEntities.get(e.getType()).add(e);
                    if(e.hasAttribute("zIndex")) {
                        this.resort();
                    }
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

    public void resort(){
        this.entities.sort((entity1, entity2) -> {
            if(entity1.hasAttribute("zIndex") && entity2.hasAttribute("zIndex")){
                return (int) entity1.getAttribute("zIndex").getData() - (int) entity2.getAttribute("zIndex").getData();
            }
            return 0;
        });
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
