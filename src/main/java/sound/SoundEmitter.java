package sound;

import entity.Entity;
import entity.component.Attribute;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import util.Callback;

public class SoundEmitter extends Entity {

    Attribute<String> soundSource;
    Attribute<Float> volume;
    Attribute<Float> pitch;
    Attribute<Callback> play;
    Attribute<Callback> pause;

    private int soundBufferID = 0;
    private int soundSourceID = 0;

    public SoundEmitter(){
        //Enum Playback type
        addAttribute(new Attribute<Float>("playback rate", (float) 1));

        soundSourceID = SoundEngine.getInstance().createSoundSource();

    }

    @Override
    public void update(double delta){
        Vector3f pos = this.getPosition();
        AL10.alSource3f(soundSourceID, AL10.AL_POSITION, -pos.x, -pos.y, -pos.z);
    }

    @Override
    public void onAdd() {
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

        //Custom Callbacks

        soundSource = new Attribute<String>("soundSource", "dragonroost.ogg");
        soundSource.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                soundBufferID = SoundEngine.getInstance().loadSound(soundSource.getData());
                SoundEngine.getInstance().loadSoundIntoSource(soundSourceID, soundBufferID);
                return null;
            }
        });
        addAttribute(soundSource);
        SoundEngine.getInstance().loadSoundIntoSource(soundSourceID, soundBufferID);

        volume = new Attribute<Float>("volume", 1f);
        volume.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL11.AL_GAIN, volume.getData());
                return null;
            }
        });
        addAttribute(volume);

        pitch = new Attribute<Float>("pitch", 1f);
        pitch.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                AL10.alSourcef(soundSourceID, AL11.AL_PITCH, pitch.getData());
                return null;
            }
        });
        addAttribute(pitch);

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
        addAttribute(play);

        pause = new Attribute<Callback>("Pause", new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(soundSourceID >= 0){
                    SoundEngine.getInstance().pauseSound(soundSourceID);
                }
                return null;
            }
        });
        addAttribute(pause);

    }

    @Override
    public void onRemove() {

    }
}
