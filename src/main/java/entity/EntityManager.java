package entity;

import com.google.gson.JsonObject;
import graphics.renderer.VAO;
import input.MousePicker;
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
    private HashMap<Class, LinkedList<Entity>> typedEntities = new HashMap<Class, LinkedList<Entity>>();

    //Used for the batch renderer
    private LinkedHashMap<VAO, LinkedList<Entity>> batches = new LinkedHashMap<VAO, LinkedList<Entity>>();

    //On remove see if a registered parent was removed, if it was, remove all children.
    private HashMap<Entity, LinkedList<Entity>> links = new HashMap<>();

    //Lock for locking our entity set
    private Lock lock;

    //Singleton design pattern
    private EntityManager(){
        lock = new ReentrantLock();
    }

    public void addEntity(Entity entity){
        //Disallow adding a null entity.
        if(entity == null){
            return;
        }

        //Make sure we haven't added this object already.
        //The same entity cant exist in the list twice.
        if(toAdd.contains(entity) || entities.contains(entity)){
            return;
        }

        //if not null, lock the array and add an entity, then unlock.
        lock.lock();
        try {
            //Add the entity
            this.toAdd.add(entity);
        } finally {
            lock.unlock();
        }
    }

    public void addEntity(Collection entity){
        //Disallow adding a null entity.
        if(entity == null){
            return;
        }

        //Make sure we haven't added this object already.
        //The same entity cant exist in the list twice.
        if(toAdd.contains(entity) || entities.contains(entity)){
            return;
        }

        //if not null, lock the array and add an entity, then unlock.
        lock.lock();
        try {
            //Add the entity
            this.toAdd.addAll(entity);
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

    public synchronized LinkedList<Entity> getEntitiesOfType(Class type, Class ... types){
        LinkedList<Entity> entities = new LinkedList<>();

        if(!this.typedEntities.containsKey(type)) {
            this.typedEntities.put(type, new LinkedList<Entity>());
        }
        entities.addAll(this.typedEntities.get(type));

        for(Class additionalType : types){
            if(this.typedEntities.containsKey(additionalType)) {
                entities.addAll(this.typedEntities.get(additionalType));
            }
        }

        return entities;
    }

    //Once per insert
    private void sortEntities() {
//        Collections.sort(entities, new Comparator<Entity>() {
//            @Override
//            public int compare(Entity e1, Entity e2) {
//                //If we are comparing a model.
//                if(e1.getModel() != null && e2.getModel() != null){
//                    return e1.getModel().getID() - e2.getModel().getID();
//                }else{
//                    if(e2.getModel() != null){
//                        return 0 - e2.getModel().getID();
//                    }
//                    if(e1.getModel() != null){
//                        return 0 - e1.getModel().getID();
//                    }
//                }
//                return 0;
//            }
//        });
    }

    //Update all entities
    public void update(double delta){
        sync();
        if(PlatformManager.getInstance().getDevelopmentStatus().equals(EnumDevelopment.PRODUCTION)) {
            for (Entity e : new LinkedList<>(entities)) {
                e.selfUpdate(delta);
            }
        }else{
            for (Entity e : new LinkedList<>(entities)) {
                if(e.hasAttribute("updateInEditor")) {
                    if((boolean)e.getAttribute("updateInEditor").getData()) {
                        e.selfUpdate(delta);
                    }
                }
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

    public LinkedList<Entity> getHitEntities(Vector3f pos, Vector3f dir, Class ... types){

        LinkedList<Entity> check = new LinkedList<Entity>();

        class Meta{
            public Vector3f collisionPosition;
            public Entity hit;
            Meta(Entity hit, Vector3f collisionPosiiton){
                this.hit = hit;
                this.collisionPosition = collisionPosiiton;
            }
        }

        LinkedList<Meta> hits  = new LinkedList<Meta>();

        //TODO
        //Calculate view frustum to filter

        if(types.length == 0){
            check.addAll(this.entities);
        }else{
            for(Class type : types){
                check.addAll(getEntitiesOfType(type));
            }
        }

        for(Entity e : check){
            Vector3f[] aabb = e.getAABB();
            Vector3f worldPos = MousePicker.rayHitsAABB(pos, dir, aabb[0], aabb[1]);
            if(worldPos != null){
                hits.add(new Meta(e, worldPos));
            }
        }

        Collections.sort(hits, new Comparator<Meta>() {
            @Override
            public int compare(Meta o1, Meta o2) {
                return -1 * (int) (o1.collisionPosition.distance(pos) - o2.collisionPosition.distance(pos));
            }
        });

        LinkedList<Entity> out = new LinkedList<>();
        for(Meta meta : hits){
            out.addLast(meta.hit);
        }

        return out;
    }

    public LinkedList<Entity> getHitEntities(Vector3f pos, Vector3f dir, Collection<Entity> check){
        //Invert Pos
        pos = new Vector3f(pos).mul(-1, -1, -1);

        LinkedList<Entity> hits  = new LinkedList<Entity>();

        for(Entity e : check){
            //TODO calc hit based off AABB broadphase, then tri-test narrow-phase
            Vector3f[] aabb = e.getAABB();
            if(MousePicker.rayHitsAABB(pos, dir, aabb[0], aabb[1]) != null){
                hits.add(e);
            }
        }

        Vector3f finalPos = pos;
        Collections.sort(hits, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                return -1 * (int) (o1.getPosition().distance(finalPos) - o2.getPosition().distance(finalPos));
            }
        });

        return hits;
    }

    public LinkedList<Entity> raycastFromEntity(Entity entity, Vector3f dir){
        // Get the world position
        Vector3f pos = new Vector3f(entity.getPosition());
        //Invert Pos
        pos = new Vector3f(pos).mul(-1, -1, -1);

        LinkedList<Entity> hits  = new LinkedList<Entity>();

        for(Entity e : this.entities){
            if(!e.equals(entity)){
                Vector3f[] aabb = e.getAABB();
                if(MousePicker.rayHitsAABB(pos, dir, aabb[0], aabb[1]) != null){
                    hits.add(e);
                }
            }
        }

        Vector3f finalPos = pos;
        Collections.sort(hits, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                return -1 * (int) (o1.getPosition().distance(finalPos) - o2.getPosition().distance(finalPos));
            }
        });

        return hits;
    }

    private void sync() {
        if(toAdd.size() > 0 || toRemove.size() > 0) {
            lock.lock();
            try {
                for (Entity e : new LinkedList<>(toAdd)) {
                    this.entities.add(e);
                    e.onAdd();
                    if(e.getModel() != null){
                        VAO id = e.getModel().getVAO();
                        if(!batches.containsKey(id)){
                            batches.put(id, new LinkedList<Entity>());
                        }
                        batches.get(id).add(e);
                    }
                    //Check if this class list exists.
                    if(this.typedEntities.get(e.getClass()) == null){
                        this.typedEntities.put(e.getClass(), new LinkedList<>());
                    }
                    this.typedEntities.get(e.getClass()).add(e);
                    if(e.hasAttribute("zIndex")) {
                        this.resort();
                    }
                }
                toAdd.clear();
                for (Entity e : new LinkedList<>(toRemove)) {
                    //Skip over null entities.
                    if(e == null){
                        //e is null so skip.
                        continue;
                    }

                    //Now do the remove
                    this.entities.remove(e);
                    //If we have a registered type for this entity
                    if(this.typedEntities.containsKey(e.getClass())){
                        this.typedEntities.get(e.getClass()).remove(e);
                    }

                    //Removal
                    if(e.getModel() != null){
                        VAO id = e.getModel().getVAO();
                        if(batches.containsKey(id)){
                            batches.get(id).remove(e);
                        }
                    }

                    e.onRemove();
                    //TODO refactor maybe?
                    e.cleanup();
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

    public void removeEntity(Entity[] toRemove) {
        lock.lock();
        try {
            for(Entity e : toRemove){
                if(this.entities.contains(e)) {
                    parentRemoveHelper(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeEntitySync(Entity toRemove) {
        removeEntity(toRemove);
        sync();
    }

    public void removeEntitySync(Entity[] toRemove) {
        removeEntity(toRemove);
        sync();
    }

    public void removeEntitySync(Collection<Entity> toRemove) {
        for(Entity e : toRemove){
            removeEntity(e);
        }
        sync();
    }

    private void parentRemoveHelper(Entity parent){
        //Remove parent
        this.toRemove.add(parent);
        if(parent.getParent() != null){
            getEntitiesChildren(parent.getParent()).remove(parent);
        }
        if(this.links.containsKey(parent)){
            LinkedList<Entity> links = new LinkedList<>(this.links.get(parent));
            for(Entity child : links){
                parentRemoveHelper(child);
            }
            this.links.remove(parent);
        }
    }

    public void unlink(Entity parent, Entity child) {
        //if this parent does not have any registered children, allow this parent to have children because if we are trying to unlink, we assumed that this entity had children and it is probably important.
        if(!this.links.containsKey(parent)){
            this.links.put(parent, new LinkedList<Entity>());
        }

        //Do a check to see if this parent has an entry for this child.
        if(this.links.get(parent).contains(child)){
            this.links.get(parent).remove(child);
        }
    }

    public void link(Entity parent, Entity child) {
        if(!this.links.containsKey(parent)){
            this.links.put(parent, new LinkedList<Entity>());
        }
        this.links.get(parent).push(child);
    }

    public LinkedList<Entity> getEntitiesChildren(Entity parent){
        if(this.links.containsKey(parent)){
            return this.links.get(parent);
        }
        return new LinkedList<>();
    }

    public boolean entityHasChildren(Entity parent){
        if(this.links.containsKey(parent)){
            return this.links.get(parent).size() > 0;
        }
        return false;
    }

    public int getSize(){
        return this.entities.size();
    }

    public LinkedHashMap<VAO, LinkedList<Entity>> getBatches(){
        return batches;
    }
}
