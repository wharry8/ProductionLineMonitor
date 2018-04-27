package com.production.w.productionlinemonitor.Model;

import com.production.w.productionlinemonitor.Helper.Constants;

import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

/**
 * Created by w on 4/25/2018.
 */

public class Box extends BaseModel {

    private float upWidth;
    private float upHeight;
    private float initWidth;
    private float initHeight;

    private int status;
    private int speed;

   public Box (float x, float y, float width, float height, Texture texture, Sprite sprite) {
       super(x, y, width, height, texture, sprite);
       this.initWidth = width;
       this.initHeight = height;
       this.upWidth = width + 10;
       this.upHeight = height + 10;

       this.status = Constants.BOX_DECLINED;
       this.speed = 10;
   }

   public void update () {
       sprite.setPos(super.getX(), super.getY());
   }
   public void update (float deltaTime) {
       switch (status) {
           case Constants.BOX_RISING:
               if (getWidth() <= upWidth && getHeight() <= upHeight) {
                   float newWidth = getWidth() + deltaTime * speed;
                   float newHeight = getHeight() + deltaTime * speed;
                   if (newWidth >= upWidth && newHeight >= upHeight) {
                       newWidth = upWidth;
                       newHeight = upHeight;
                       status = Constants.BOX_RISED;
                   }
                   setWidth(newWidth);
                   setHeight(newHeight);
                   sprite.resize((int)newWidth, (int)newHeight);
               }
               break;
           case Constants.BOX_DECLING:
               if (getWidth() >= initWidth && getHeight() >= initHeight) {
                   float newWidth = getWidth() - deltaTime * speed;
                   float newHeight = getHeight() - deltaTime * speed;
                   if (newWidth >= initWidth && newHeight >= initHeight) {
                       newWidth = initWidth;
                       newHeight = initHeight;
                       status = Constants.BOX_DECLINED;
                   }
                   setWidth(newWidth);
                   setHeight(newHeight);
                   sprite.resize((int)newWidth, (int)newHeight);
               }
               break;
           default:
               break;
       }
   }

    public float getUpWidth() {
        return upWidth;
    }

    public void setUpWidth(float upWidth) {
        this.upWidth = upWidth;
    }

    public float getUpHeight() {
        return upHeight;
    }

    public void setUpHeight(float upHeight) {
        this.upHeight = upHeight;
    }

    public float getInitWidth() {
        return initWidth;
    }

    public void setInitWidth(float initWidth) {
        this.initWidth = initWidth;
    }

    public float getInitHeight() {
        return initHeight;
    }

    public void setInitHeight(float initHeight) {
        this.initHeight = initHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
