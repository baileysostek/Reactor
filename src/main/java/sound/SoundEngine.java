package sound;

import org.joml.Vector3f;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;
import util.StringUtils;

import javax.script.ScriptEngine;
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
    }


    public int loadSound(String fileName){
        if(!bufferPointers.containsKey(fileName)) {
            String path = StringUtils.getRelativePath() + "sound/" + fileName;
            System.out.println("Loading sound at:" + path);

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
        }else{
            return bufferPointers.get(fileName);
        }
    }

    public int createSoundSource(int soundBufferPointer){
        int sourcePointer = AL10.alGenSources();
        soundSources.push(sourcePointer);

        //Assign our buffer to the source
        AL10.alSourcei(sourcePointer, AL10.AL_BUFFER, soundBufferPointer);

        return sourcePointer;
    }

    public void playSound(int sourcePointer){
        System.out.println("Playing sound");
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
}
