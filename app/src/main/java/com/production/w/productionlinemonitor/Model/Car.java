package com.production.w.productionlinemonitor.Model;

import android.util.Log;
import android.widget.Spinner;

import com.production.w.productionlinemonitor.Helper.Constants;

import org.w3c.dom.Text;

import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

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

    Texture texture;
    Sprite sprite;

    int direction;
    int speed;

    Box box;

    float destination;

    boolean match;

    public Car(float x, float y, float width, float height, Texture texture, Sprite sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.sprite = sprite;

        this.match = false;
    }

    public void move_v2 (float deltaTime) {
        float precision = 5.f;
        if (speed == 0) {
            return ;
        }
        float newX = x + deltaTime * direction * speed;
        if (Math.abs(newX - destination) < precision) {
            speed = 0;
            x = destination;
            newX = x;
        }
        sprite.setPos(newX, y);
        if (box != null) {
            box.setX(newX);
            box.update();
        }
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
            sprite.setPos(x, y);
        } else if (direction == Constants.RIGHT) {
            if (x >= blockX) {
                x = blockX;
                this.speed = 0;
            } else {
                x = newX;
            }
            sprite.setPos(x, y);
        }
        if (box != null) {
            Log.e(TAG, "move: " + box.getX());
            box.setX(newX);
            box.update();
        }
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public float getDestination() {
        return destination;
    }

    public void setDestination(float destination) {
        this.destination = destination;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
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
