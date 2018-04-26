package com.production.w.productionlinemonitor.Model;

import android.util.Log;

import com.production.w.productionlinemonitor.Constants;

import static android.content.ContentValues.TAG;

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

    int direction;
    int speed;

    Box box;

    public Car(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void move (float deltaTime, float blockX) {
//        Log.e(TAG, "move: anyway: " + x);
        if (speed == 0) {
            return;
        }
        float newX = x + deltaTime * direction * speed;
        Log.e(TAG, "move: " + newX + "," + blockX);
        if (direction == Constants.LEFT) {
            if (x <= blockX) {
                x = blockX;
                this.speed = 0;
            } else {
                x = newX;
            }
        } else if (direction == Constants.RIGHT) {
            if (x >= blockX) {
                x = blockX;
                this.speed = 0;
            } else {
                x = newX;
            }
        }
        if (box != null) {
            Log.e(TAG, "move: " + box.getX());
            box.setX(newX);
            box.update();
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
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
