package graphics.renderer;

import models.AABB;

import java.util.LinkedList;

public class DirectDrawData {
    private LinkedList<DrawIndex> data;
    private AABB aabb;

    public DirectDrawData(){
        this.data = new LinkedList<DrawIndex>();
    }


    public void addDrawData(DrawIndex drawData) {
        data.push(drawData);
    }

    public void setAABB(AABB aabb) {
        this.aabb = aabb;
    }

    public AABB getAABB() {
        return this.aabb;
    }

    public LinkedList<DrawIndex> getDrawIndices(){
        return this.data;
    }

    public DirectDrawData merge(DirectDrawData other) {
        this.aabb.recalculateFromPoint(other.getAABB().getMIN());
        this.aabb.recalculateFromPoint(other.getAABB().getMAX());

        for(DrawIndex otherIndex : other.getDrawIndices()){
            this.addDrawData(otherIndex);
        }

        return this;
    }
}
