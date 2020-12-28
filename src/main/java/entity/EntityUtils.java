package entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import org.joml.Vector2f;
import org.joml.Vector4f;
import serialization.SerializationHelper;

import java.util.Collection;

public class EntityUtils {

    private static FBO buffer;

    static {
        buffer = new FBO(128, 128);
    }

    public static Entity cloneTarget(Entity target){
        Entity entity;

        //This has broken attributes, set from parent
        JsonObject serialziedEntity = target.serialize();

        //Deserialize the entity
        if(target.getClass().equals(Entity.class)) {
            //Regula old entity
            entity = new Entity().deserialize(serialziedEntity);
        }else{
            //Fancy entity from another class or namespace :)
            try {
                Class<?> classType = Class.forName(target.getClass().getName());
                entity = ((Entity) SerializationHelper.getGson().fromJson(serialziedEntity, classType)).deserialize(serialziedEntity);

            } catch (ClassNotFoundException e) {
                //TODO play sound that that entity is unknown, maybe show message dialogue too.
                e.printStackTrace();
                return null;
            }
        }

        //Check if the moved entity was a child
        if(target.hasParent()){
            entity.setParent(target.getParent());
        }

        return entity;
    }

    public static boolean isDescendedFrom(Entity toCheck, Entity possibleAncestor){
        if(toCheck == null){
            return false;
        }

        if(toCheck.hasParent()){
            if(toCheck.getParent().hasParent()){
                return isDescendedFrom(toCheck.getParent(), possibleAncestor);
            }else{
                if(toCheck.getParent().equals(possibleAncestor)){
                    return true;
                }
            }
        }

        return toCheck.equals(possibleAncestor);
    }

    public static Entity group(Collection<Entity> entities){
        //Out save object. This will be a .tek file eventually.
        JsonObject out = new JsonObject();
        //First lets get our output object into the json format that we expect it to be in.

        //We need to modify our entities using this old texture at this point, to now lookup from atlas position defined by x,y we will do this with attributes
        for(Entity e : entities){
            //new length of a texture.
//            float scaleValue = 1.0f / (float) row_col;
            //set position
//            e.addAttribute(new Attribute<Vector2f>("t_offset", new Vector2f((x / s_width) * scaleValue, (y / s_height) * scaleValue)));
            //set texture scale
//            e.addAttribute(new Attribute<Vector2f>("t_scale" , new Vector2f(scaleValue, scaleValue)));

            //set real scale
//            e.setScale(new Vector3f((this.sheet.getSpriteWidth() / 16f), 0, (this.sheet.getSprightHeight() / 16f)));

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
//                textureCoords.mul(new Vector2f(scaleValue, scaleValue));
                //Offset by n * scale
//                textureCoords.add(new Vector2f((x / s_width) * scaleValue, (y / s_height) * scaleValue));

                texBytes.set((i * 2 + 0), new JsonPrimitive(textureCoords.x()));
                texBytes.set((i * 2 + 1), new JsonPrimitive(textureCoords.y()));
            }

            //Merge these bad boys together.
            out = SerializationHelper.merge(out, serializedEntity);

        }

        //At this point texture atlas is generated and the entities have had their attributes modified.
        out.add("image", SpriteBinder.getInstance().getSprite(SpriteBinder.getInstance().getFileNotFoundID()).serialize());

        return new Entity().deserialize(out);
    }

    //TODO make SPRITE Preview LOD a setting.
    public static Sprite takePicture(Entity Entity){
        return new Sprite(128, 128);
    }

}

