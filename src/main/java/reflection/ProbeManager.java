package reflection;

import graphics.sprite.SpriteBinder;
import org.joml.Vector3f;

import java.util.LinkedList;

public class ProbeManager {
    private static ProbeManager probeManager;

    private LinkedList<Probe> probes = new LinkedList<>();

    private final int REFLECTION_PROBE_SVG = SpriteBinder.getInstance().loadSVG("engine/svg/circle.svg", 1, 1f, 96f);

    private ProbeManager(){

    }

    public Probe getClosest(Vector3f sourcePoint){
        if(probes.size() <= 0){
            return null;
        }

        float closestDistance = Float.MAX_VALUE;
        Probe closest = null;
        for(Probe probe : probes){
            float thisProbeDistance = probe.getPosition().distance(sourcePoint);
            if(thisProbeDistance < closestDistance){
                closest = probe;
                closestDistance = thisProbeDistance;
            }
        }

        return closest;
    }

    public void addProbe(Probe probe){
        if(probe == null){
            return;
        }

        if(!probes.contains(probe)){
            probes.add(probe);
        }
    }

    public void removeProbe(Probe probe){
        if(probe == null){
            return;
        }

        if(probes.contains(probe)){
            probes.remove(probe);
        }
    }

    public int getReflectionProbeSVG(){
        return this.REFLECTION_PROBE_SVG;
    }

    public static void initialize(){
        if(probeManager == null){
            probeManager = new ProbeManager();
        }
    }

    public static ProbeManager getInstance(){
        return probeManager;
    }

}
