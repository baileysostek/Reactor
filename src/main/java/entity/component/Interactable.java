package entity.component;

import camera.CameraManager;
import input.Keyboard;
import org.joml.Vector3f;
import util.Callback;
import util.DistanceCalculator;

import java.util.LinkedList;

public class Interactable extends Component{

    //This Components attributes
    private Attribute<Integer> key;
    private Attribute<Float> range;

    private boolean inRange = false;

    private Callback distance_check_callback;

    public Interactable(){
        key   = new Attribute<Integer>("interactable_key"  , Keyboard.E);
        range = new Attribute<Float>  ("interactable_range", 2.5f);

        distance_check_callback = new Callback() {
            @Override
            public Object callback(Object... objects) {
                System.out.println("Test2:"+parent.getPosition());
                System.out.println("Test2:"+new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).mul(1, 0, -1));
                System.out.println("Test2:"+ range.getData());
                if (DistanceCalculator.distance(parent.getPosition(), new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).mul(1, 0, -1)) <= range.getData()) {
                    System.out.println("Test3");
                    invoke("onInteract", objects);
                }
                return null;
            }
        };
    }

    public Interactable setCallback(Callback callback){
        Keyboard.getInstance().switchCallbackKey(distance_check_callback, key.getData());
        return this;
    }

    public Interactable setKey(int key){
        this.key.setData(key);
        Keyboard.getInstance().switchCallbackKey(distance_check_callback, key);
        return this;
    }

    public Interactable setRange(float range){
        this.range.setData(range);
        return this;
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        //Add the attributes
        out.add(key);
        out.add(range);
        return out;
    }

    @Override
    public void update(double delta) {
        if(parent != null) {
            if (DistanceCalculator.distance(parent.getPosition(), new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).mul(1, 0, -1)) <= range.getData()) {
                invoke("inRange");
                if(inRange == false){
                    inRange = true;
                    invoke("onInRange");
                }
            }else{
                invoke("outRange");
                if(inRange == true){
                    inRange = false;
                    invoke("onOutRange");
                }
            }
        }
    }

    //This components name
    @Override
    public String getName() {
        return "Intractable";
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {

    }
}
