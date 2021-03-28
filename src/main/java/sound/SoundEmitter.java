package sound;

import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import graphics.renderer.DirectDraw;
import graphics.sprite.SpriteBinder;
import lighting.LightingManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import particle.ParticleSystem;
import util.Callback;

public class SoundEmitter extends Entity {

    //Attributes for source
    Attribute<String>   soundSource = new Attribute<String>("soundSource", "dragonroost.ogg");
    Attribute<Float>    volume = new Attribute<Float>("volume", 1f);
    Attribute<Float>    pitch = new Attribute<Float>("pitch", 1f);
    Attribute<Float>    rolloff = new Attribute<Float>("rolloff", 1f);
    Attribute<Float>    referenceDistance = new Attribute<Float>("referenceDistance", 16f);
    Attribute<Float>    maxDistance = new Attribute<Float>("maxDistance", 32f);
    Attribute<Boolean>  loop = new Attribute<Boolean>("Loop", false);

    //Controls
    Attribute<Callback> play;
    Attribute<Callback> pause;

    private int soundBufferID = 0;
    private int soundSourceID = 0;

    public SoundEmitter(){
        //Enum Playback type
        addAttribute(new Attribute<Float>("playback rate", (float) 1));

        soundSourceID = SoundEngine.getInstance().createSoundSource();

        //Custom Callbacks
        soundSource.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                soundBufferID = SoundEngine.getInstance().loadSound(soundSource.getData());
                SoundEngine.getInstance().loadSoundIntoSource(soundSourceID, soundBufferID);
                return null;
            }
        });

        volume.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL11.AL_GAIN, volume.getData());
                return null;
            }
        });

        pitch.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL11.AL_PITCH, pitch.getData());
                return null;
            }
        });

        rolloff.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL10.AL_ROLLOFF_FACTOR, rolloff.getData());
                return null;
            }
        });

        referenceDistance.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL10.AL_REFERENCE_DISTANCE, referenceDistance.getData());
                return null;
            }
        });

        maxDistance.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL10.AL_MAX_DISTANCE, maxDistance.getData());
                return null;
            }
        });

        loop.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                boolean loop = ((Attribute<Boolean>) objects[0]).getData();
                System.out.println("Setting looping to: " + loop);
                AL10.alSourcei(soundSourceID, AL10.AL_LOOPING, loop ? 1 : 0);
                return null;
            }
        });

        //Play
        play = new Attribute<Callback>("Play", new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(soundSourceID >= 0){
                    SoundEngine.getInstance().playSound(soundSourceID);
                }
                return null;
            }
        });

        pause = new Attribute<Callback>("Pause", new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(soundSourceID >= 0){
                    SoundEngine.getInstance().pauseSound(soundSourceID);
                }
                return null;
            }
        });

        SoundEngine.getInstance().loadSoundIntoSource(soundSourceID, soundBufferID);

    }

    @Override
    public void update(double delta){
        Vector3f pos = this.getPosition();
        AL10.alSource3f(soundSourceID, AL10.AL_POSITION, -pos.x, -pos.y, -pos.z);
    }

    @Override
    public void onAdd() {
        super.setTexture(SoundEngine.getInstance().getSoundEmitterSVG());
        //Add self to LightingManager
        if(this.hasAttribute("updateInEditor")) {
            this.getAttribute("updateInEditor").setVisible(false).setData(true);
        }
        if(this.hasAttribute("scale")) {
            this.getAttribute("scale").setVisible(false);
        }
        if(this.hasAttribute("rotation")) {
            this.getAttribute("rotation").setVisible(false);
        }
        if(this.hasAttribute("materials")) {
            this.getAttribute("materials").setVisible(false);
        }

        addAttribute(soundSource);
        addAttribute(volume);
        addAttribute(pitch);
        addAttribute(referenceDistance);
        addAttribute(maxDistance);
        addAttribute(rolloff);
        addAttribute(loop);
        addAttribute(play);
        addAttribute(pause);
    }

    public void setSoundSource(String soundSource){
        this.soundSource.setData(soundSource);
    }

    public void setLoop(boolean loop){
        this.loop.setData(loop);
    }

    public void setVolume(float volume){
        this.volume.setData(volume);
    }

    public void setPitch(float pitch){
        this.pitch.setData(pitch);
    }

    public void play(){
        if(soundSourceID >= 0){
            SoundEngine.getInstance().playSound(soundSourceID);
        }
    }

    public void pause(){
        if(soundSourceID >= 0){
            SoundEngine.getInstance().pauseSound(soundSourceID);
        }
    }

    @Override
    public void onRemove() {
        //Stop the source from playing
        SoundEngine.getInstance().removeSource(this.soundSourceID);
    }


    @Override
    public void renderInEditor(boolean selected){
        DirectDraw.getInstance().drawBillboard(super.getPosition(), new Vector2f(1), SoundEngine.getInstance().getSoundEmitterSVG());
        if(selected){
            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("maxDistance").getData()), new Vector3f(1, 0, 0), 32, new Vector3f(1, 1, 1));
            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("maxDistance").getData()), new Vector3f(0, 1, 0), 32, new Vector3f(1, 1, 1));
            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("maxDistance").getData()), new Vector3f(0, 0, 1), 32, new Vector3f(1, 1, 1));

            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("referenceDistance").getData()), new Vector3f(1, 0, 0), 32, new Vector3f(1, 0, 0));
            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("referenceDistance").getData()), new Vector3f(0, 1, 0), 32, new Vector3f(1, 0, 0));
            DirectDraw.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("referenceDistance").getData()), new Vector3f(0, 0, 1), 32, new Vector3f(1, 0, 0));
        }
    }

    @Override
    public JsonObject serialize(){
        return super.serialize();
    }

    @Override
    public SoundEmitter deserialize(JsonObject data) {
        super.deserialize(data);

//        soundSource   = AttributeUtils.synchronizeWithParent(soundSource, this);
//        volume        = AttributeUtils.synchronizeWithParent(volume, this);
//        pitch         = AttributeUtils.synchronizeWithParent(pitch , this);
//        loop          = AttributeUtils.synchronizeWithParent(loop , this);

//        this.updateSystem();

        return this;
    }
}
