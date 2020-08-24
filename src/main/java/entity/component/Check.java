package entity.component;

import camera.CameraManager;
import input.Keyboard;
import org.joml.Vector3f;
import util.Callback;
import util.DistanceCalculator;

import java.util.LinkedList;

public class Check extends Component{

    //This Components attributes
    private Attribute<String> attributeName;
    private Attribute<EnumComparison> check;
    private Attribute<Object> comparisonValue;

    public Check(){
        attributeName   = new Attribute<String>         (super.getID()+"attribute_name"   , null);
        check           = new Attribute<EnumComparison> (super.getID()+"comparison_type"  , EnumComparison.EQUAL);
        comparisonValue = new Attribute<Object>         (super.getID()+"comparison_value" , null);
    }


    public Check setAttributeToCheck(String name){
        this.attributeName.setData(name);
        return this;
    }

    public Check setComparioson(EnumComparison comparioson){
        this.check.setData(comparioson);
        return this;
    }

    public Check setComparisonValue(Object value){
        this.comparisonValue.setData(value);
        return this;
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        //Add the attributes
        out.add(attributeName);
        out.add(check);
        out.add(comparisonValue);

        //Do initial state check
        onAttributeUpdate(null);

        //Return out
        return out;
    }

//    @Override
//    public void postInitialize() {
//        //Do initial state check
//        onAttributeUpdate(null);
//    }

    @Override
    public void update(double delta) {
        //This is the frame update
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {
        //Called back every time an attribute changes
        if(parent.hasAttribute(attributeName.getData())){
            Attribute comparison = parent.getAttribute(attributeName.getData());
            boolean checkPassed = false;

            switch (check.getData()) {
                case EQUAL:
                    checkPassed = comparison.getData().equals(comparisonValue.getData());
                    break;
                case NOT_EQUAL:
                    checkPassed = !comparison.getData().equals(comparisonValue.getData());
                    break;
                case GREATERTHAN:
                    break;
                case LESSTHAN:
                    break;
                case GREATERTHAN_EQUAL:
                    break;
                case LESSTHAN_EQUAL:
                    break;
                case IS_NULL:
                    break;
            }

            if(checkPassed){
                invoke("checkPassed");
            }else {
                invoke("checkFailed");
            }

            System.out.println("Checking:"+checkPassed);
        }

    }

    //This components name
    @Override
    public String getName() {
        return "Check:"+this.attributeName.getData();
    }
}
