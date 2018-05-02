package com.production.w.productionlinemonitor.Model;

import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.ProductionLineActivity;

import javax.net.ssl.SSLProtocolException;

import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

/**
 * Created by w on 4/25/2018.
 */

public class Hand extends BaseModel {

    private int status;
    private int speed;
    private float verticalDistance;
    private float horizontalDistance;
    private float initY;
    private float initHeight;
    private float downHeight;
    private float rightEndY;
    private float leftEndY;

    public Hand (float x, float y, float width, float height, Texture texture, Sprite sprite) {
        super(x, y, width, height, texture, sprite);

        this.initHeight = height;
        this.downHeight = height - 20;
        this.initY = y;
        this.speed = 100;
        this.status = Constants.handStatic;
    }
    public void update (float deltaTime) {
        switch (status) {
            case Constants.handLeftShifting:
                if (getY() > initY - horizontalDistance) {
                    float newY = getY() - speed * deltaTime;
                    boolean ok = false;
                    if (newY <= initY - horizontalDistance) {
                        newY = initY - horizontalDistance;
                        ok = true;
                    }
                    if (ok) {
                        status = Constants.handStatic;
                        initY = newY;
                    }
                    setY(newY);
                    sprite.setPos(getX(), getY());
                }
                break;
            case Constants.handRightShifting:
                if (getY() < initY + horizontalDistance) {
                    float newY = getY() + speed * deltaTime;
                    boolean ok = false;
                    if (newY >= initY + horizontalDistance) {
                        newY = initY + horizontalDistance;
                        ok = true;
                    }
                    if (ok) {
                        status = Constants.handStatic;
                        initY = newY;
                    }
                    setY(newY);
                    sprite.setPos(getX(), getY());
                }
                break;
            case Constants.handDeclining:
                if (getHeight() > downHeight) {
                    float newHeight = getHeight() - speed * deltaTime;
                    boolean ok = false;
                    if (newHeight <= downHeight) {
                        newHeight = downHeight;
                        ok = true;
                    }
                    if (ok) {
                        status = Constants.handStatic;
                    }
                    setHeight(newHeight);
                    sprite.resize((int)getWidth(), (int)getHeight());
                }
                break;
            case Constants.handRising:
                if (getHeight() < initHeight) {
                    float newHeight = getHeight() + speed * deltaTime;
                    boolean ok = false;
                    if (newHeight >= initHeight) {
                        newHeight = initHeight;
                        ok = true;
                    }
                    if (ok) {
                        status = Constants.handStatic;
                    }
                    setHeight(newHeight);
                    sprite.resize((int)getWidth(), (int)getHeight());
                }
                break;
            default:
                break;
        }
    }

    public float getInitY() {
        return initY;
    }

    public void setInitY(float initY) {
        this.initY = initY;
    }

    public float getInitHeight() {
        return initHeight;
    }

    public void setInitHeight(float initHeight) {
        this.initHeight = initHeight;
    }

    public float getDownHeight() {
        return downHeight;
    }

    public void setDownHeight(float downHeight) {
        this.downHeight = downHeight;
    }

    public float getRightEndY() {
        return rightEndY;
    }

    public void setRightEndY(float rightEndY) {
        this.rightEndY = rightEndY;
    }

    public float getLeftEndY() {
        return leftEndY;
    }

    public void setLeftEndY(float leftEndY) {
        this.leftEndY = leftEndY;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getVerticalDistance() {
        return verticalDistance;
    }

    public void setVerticalDistance(float verticalDistance) {
        this.verticalDistance = verticalDistance;
    }

    public float getHorizontalDistance() {
        return horizontalDistance;
    }

    public void setHorizontalDistance(float horizontalDistance) {
        this.horizontalDistance = horizontalDistance;
        leftEndY = initY - horizontalDistance;
        rightEndY = initY + horizontalDistance;
    }
}
