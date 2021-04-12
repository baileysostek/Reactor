package graphics.animation;

public class KeyFrame {
    private float value    = 0f;
    private float position = 0f;
    private KeyFrame next  = null;
    private EnumInterpolation interpolation = EnumInterpolation.LERP;

    public KeyFrame(float value, float position){
        this.value = value;
        this.position = position;
    }

    public KeyFrame setNext(KeyFrame frame){
        this.next = frame;
        return this;
    }

    public KeyFrame setInterpolation(EnumInterpolation interpolation){
        this.interpolation = interpolation;
        return this;
    }

    public float getValue(float position){
        //If keyframe is less than this, return this
        if(this.next == null || position < this.position){
            return value;
        }

        //If keyframe is greater than next, getValue of next
        if(position >= this.next.position){
            return this.next.getValue(position);
        }

        //Get the deltas
        float delta = (next.position - this.position); // Delta between frames
        float index = (position - this.position);      // Delta from start

        // Mu is our delta between the 2 frames
        float mu = (index / delta);

        //Calc interpolation
        switch (interpolation){
            case LERP:{
                //Linear interpolation
                float diff_prime = (1.0f - mu);
                return (diff_prime * this.value) + (mu * next.value);
            }
            case COSINE:{
                float y1 = this.value;
                float y2 = this.next.getValue(position);

                float mu2 = (float) ((1f-Math.cos(mu*Math.PI))/2f);

                return (y1*(1f-mu2)+y2*mu2);
            }
        }

        return value;
    }

    public KeyFrame getNext() {
        return this.next;
    }
}
