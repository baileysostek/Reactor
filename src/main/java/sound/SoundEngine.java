package sound;

import camera.CameraManager;
import graphics.sprite.SpriteBinder;
import org.joml.Vector3f;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;
import util.StringUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.system.MemoryStack.stackMallocInt;

public class SoundEngine{

    //Singleton instance
    private static SoundEngine soundEngine;

    //Sound data buffered
    private HashMap<String, Integer> bufferPointers = new HashMap<String, Integer>();
    //Speakers in world playing the sound buffer
    private LinkedList<Integer> soundSources = new LinkedList<Integer>();

    private final long context;
    private final long device;

    private final int VOLUME_UP_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/volume-up.svg", 1, 1f, 96f);

    /*SG*/
    private SoundEngine() {
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        device = ALC10.alcOpenDevice(defaultDeviceName);
        int[] attributes = {0};
        context = ALC10.alcCreateContext(device, attributes);
        ALC10.alcMakeContextCurrent(context);
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
        if(alCapabilities.OpenAL10) {
            System.out.println("OpenAL 1.0 is supported!");
        }

        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);

    }


    public int loadSound(String fileName){
        if(!bufferPointers.containsKey(fileName)) {
            String path = StringUtils.getPathToResources() + "sound/" + fileName;
            System.out.println("Loading sound at:" + path);

            File check = new File(path);
            if(!check.exists()){
                System.out.println("Sound does not exist.");
                return -1;
            }

            //OGG
            if(fileName.endsWith(".ogg")) {
                //Allocate space to store return information from the function
                MemoryStack.stackPush();
                IntBuffer channelsBuffer = stackMallocInt(1);
                MemoryStack.stackPush();
                IntBuffer sampleRateBuffer = stackMallocInt(1);

                ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);

                //Retreive the extra information that was stored in the buffers by the function
                int channels = channelsBuffer.get();
                int sampleRate = sampleRateBuffer.get();
                //Free the space we allocated earlier
                MemoryStack.stackPop();
                MemoryStack.stackPop();

                //Find the correct OpenAL format
                int format = -1;
                if (channels == 1) {
                    format = AL10.AL_FORMAT_MONO16;
                } else if (channels == 2) {
                    format = AL10.AL_FORMAT_STEREO16;
                }

                //Request space for the buffer
                int bufferPointer = AL10.alGenBuffers();

                System.out.println("Allocated sound buffer:" + bufferPointer);
                System.out.println("rawAudioBuffer:" + rawAudioBuffer);
                bufferPointers.put(fileName, bufferPointer);

                //Send the data to OpenAL
                AL10.alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

                //Free the memory allocated by STB
                LibCStdlib.free(rawAudioBuffer);
                return bufferPointer;
            }

            if(fileName.endsWith(".wav")){
                int bufferPointer = AL10.alGenBuffers();
                try {
                    InputStream bufferedIn = new BufferedInputStream(new FileInputStream(check));
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                    WaveData data = WaveData.create(audioStream);
                    AL10.alBufferData(bufferPointer, data.format, data.data, data.samplerate);
                    data.dispose();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
                return bufferPointer;
            }

        }else{
            return bufferPointers.get(fileName);
        }

        return -1;
    }

    public int createSoundSource(){
        int sourcePointer = AL10.alGenSources();
        soundSources.push(sourcePointer);

        AL10.alSourcef(sourcePointer, AL10.AL_ROLLOFF_FACTOR, 1);
        AL10.alSourcef(sourcePointer, AL10.AL_REFERENCE_DISTANCE, 15);
        AL10.alSourcef(sourcePointer, AL10.AL_MAX_DISTANCE, 32);

        AL10.alSource3f(sourcePointer, AL10.AL_POSITION, 0, 0, 0);
        AL10.alSource3f(sourcePointer, AL10.AL_VELOCITY, 0, 0, 0);

        return sourcePointer;
    }

    public void loadSoundIntoSource(int source, int bufferPointer){
        //Assign our buffer to the source
        if(soundSources.contains(source)) {
            AL10.alSourcei(source, AL10.AL_BUFFER, bufferPointer);
        }
    }

    public void playSound(int sourcePointer){
        AL10.alSourcePlay(sourcePointer);
    }

    public void pauseSound(int sourcePointer){
        AL10.alSourcePause(sourcePointer);
    }

    public int getSound(String sourceName){
        if(this.bufferPointers.containsKey(sourceName)){
            return this.bufferPointers.get(sourceName);
        }else{
            System.out.println("The sound:"+sourceName+" could not be found. Has a call to Load been made for it yet?");
        }
        return 0;
    }

    public void update(double delta){
        Vector3f position = CameraManager.getInstance().getActiveCamera().getPosition();
        Vector3f lookDir  = CameraManager.getInstance().getActiveCamera().getLookingDirection();
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        float[] orientation = new float[]{
            -lookDir.x, -lookDir.y, -lookDir.z,
            0,1,0
        };
        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
    }

    public void removeSource(int soundSourceID) {
       //TODO nice fade out
        if(soundSources.contains(soundSourceID)) {
            soundSources.remove((Object)soundSourceID);
            AL10.alDeleteSources(soundSourceID);
        }
    }

    public void onShutdown() {
        for(Integer bufferPointer : bufferPointers.values()){
            AL10.alDeleteBuffers(bufferPointer);
        }
        for(Integer sourcePointer : soundSources){
            AL10.alDeleteSources(sourcePointer);
        }
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }

    public int getSourcePlaybackPosition(int sourcePointer){
        return AL10.alGetSourcei(sourcePointer, AL11.AL_SAMPLE_OFFSET);
    }

    public void setListener(Vector3f position){

    }

    public static void initialize(){
        if(soundEngine == null){
            soundEngine = new SoundEngine();
        }
    }

    public static SoundEngine getInstance(){
        return soundEngine;
    }

    public int getSoundEmitterSVG() {
        return VOLUME_UP_SVG;
    }
}
