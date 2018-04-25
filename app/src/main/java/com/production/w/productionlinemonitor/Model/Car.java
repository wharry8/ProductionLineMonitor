package com.production.w.productionlinemonitor.Model;

/**
 * Created by w on 4/25/2018.
 */

public class Car {
    float x;
    float y;
    float width;
    float height;

    boolean hookOut;
    boolean hookIn;

    Box box;

    public Car(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isHookOut() {
        return hookOut;
    }

    public void setHookOut(boolean hookOut) {
        this.hookOut = hookOut;
    }

    public boolean isHookIn() {
        return hookIn;
    }

    public void setHookIn(boolean hookIn) {
        this.hookIn = hookIn;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }
}
