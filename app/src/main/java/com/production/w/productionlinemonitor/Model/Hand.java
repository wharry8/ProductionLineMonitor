package com.production.w.productionlinemonitor.Model;

import android.util.Log;

import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.Helper.HandPosition;
import com.production.w.productionlinemonitor.ProductionLineActivity;

import javax.net.ssl.SSLProtocolException;

import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

import static android.support.constraint.Constraints.TAG;

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
    private float middleY;

    public int previousPosition;

    boolean isMatch;

    public Hand (float x, float y, float width, float height, Texture texture, Sprite sprite) {
        super(x, y, width, height, texture, sprite);
        this.previousPosition = HandPosition.Middle;
        this.initHeight = height;
        this.downHeight = height - 20;
        this.initY = y;
        this.middleY = y;
        this.speed = 100;
        this.status = Constants.handStatic;
        this.isMatch = false;
    }
    public void updatePosition (int position) {
        float newy = 0.f;
        if (position == Constants.HAND_LEFT_BOTTOM || position == Constants.HAND_LEFT_TOP) {
            newy = leftEndY;
        }
        if (position == Constants.HAND_RIGHT_BOTTOM || position == Constants.HAND_RIGHT_TOP) {
            this.previousPosition = HandPosition.Right;
            newy = rightEndY;
        }
        if (position == Constants.HAND_MIDDLE_BOTTOM || position == Constants.HAND_MIDDLE_TOP) {
            this.previousPosition = HandPosition.Middle;
            newy = middleY;
        }
        setY(newy);
        initY = newy;
        sprite.setPos(getX(), getY());
    }
    public void update (float deltaTime) {
        switch (status) {
            case Constants.handLeftShifting:
                if (Float.compare(initY, this.middleY) == 0) {
                    return;
                }
                if (getY() > initY - horizontalDistance) {
                    float newY = getY() - speed * deltaTime;
                    boolean ok = false;
                    if (newY <= initY - horizontalDistance) {
                        newY = initY - horizontalDistance;
                        ok = true;
                    }
                    if (ok) {
//                        this.previousPosition = HandPosition.Middle;
                        status = Constants.handLeftShifted;
                        initY = newY;
                    }
                    setY(newY);
                    sprite.setPos(getX(), getY());
                }
                break;
            case Constants.handRightShifting:
                if (Float.compare(initY, this.rightEndY) == 0) {
                    return;
                }
                if (getY() < initY + horizontalDistance) {
                    float newY = getY() + speed * deltaTime;
                    boolean ok = false;
                    if (newY >= initY + horizontalDistance) {
                        newY = initY + horizontalDistance;
                        ok = true;
                    }
                    if (ok) {
//                        this.previousPosition = HandPosition.Right;
                        status = Constants.handRightShifted;
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
                        status = Constants.handDeclined;
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
                        status = Constants.handRised;
                    }
                    setHeight(newHeight);
                    sprite.resize((int)getWidth(), (int)getHeight());
                }
                break;
            default:
                break;
        }
    }

    public float getMiddleY() {
        return middleY;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
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

    public void setMiddleY(float middleY) {
        this.middleY = middleY;
    }

    public int getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(int previousPosition) {
        this.previousPosition = previousPosition;
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
