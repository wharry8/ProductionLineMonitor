package com.production.w.productionlinemonitor.Model;

import android.util.Log;

import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.MainActivity;
import com.production.w.productionlinemonitor.ProductionLineActivity;

import java.lang.reflect.GenericArrayType;

import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by w on 4/25/2018.
 */

public class Box extends BaseModel {

    private float upWidth;
    private float upHeight;
    private float initWidth;
    private float initHeight;
    private float initY;

    float yOffset;

    private int status;
    private int speed;

   public Box (float x, float y, float width, float height, Texture texture, Sprite sprite) {
       super(x, y, width, height, texture, sprite);
       this.initWidth = width;
       this.initHeight = height;
       this.upWidth = width + 10;
       this.upHeight = height + 10;
       this.initY = y;

       this.status = Constants.BOX_DECLINED;
       this.speed = 10;
       this.yOffset = 20;
   }

   public void changeSize (int state) {
       this.status = state;
       switch (state) {
           case Constants.BOX_RISED:
               setWidth(upWidth);
               setHeight(upHeight);
               setY(initY + yOffset);
               sprite.setPos(getX(), getY());
               sprite.resize((int)getWidth(), (int)getHeight());
               break;
           case Constants.BOX_DECLINED:
               setWidth(initWidth);
               setHeight(initHeight);
               setY(initY);
               sprite.setPos(getX(), getY());
               sprite.resize((int)getWidth(), (int)getHeight());
               break;
       }
   }


   public void update () {
       sprite.setPos(super.getX(), super.getY());
       if (getX() >= Constants.glWidth - Constants.unitWidth / 2) {
           sprite.releaseResources();
       }
   }
   public void update (float deltaTime) {
       switch (status) {
           case Constants.BOX_RISING:
               if (getWidth() <= upWidth && getHeight() <= upHeight) {
                   float newWidth = getWidth() + deltaTime * speed;
                   float newHeight = getHeight() + deltaTime * speed;
                   float newY = getY() + deltaTime * speed;
                   boolean part1Ok = false;
                   boolean part2Ok = false;
                   if (newWidth >= upWidth && newHeight >= upHeight) {
                       newWidth = upWidth;
                       newHeight = upHeight;
                       part1Ok = true;
//                       status = Constants.BOX_RISED;
                   }
                   if (newY  >= initY + yOffset) {
                       newY = initY + yOffset;
                       part2Ok = true;
                   }
                   if (part1Ok && part2Ok) {
                       status = Constants.BOX_RISED;
                   }
                   setWidth(newWidth);
                   setHeight(newHeight);
                   setY(newY);
                   sprite.resize((int)newWidth, (int)newHeight);
                   sprite.setPos(getX(), getY());
               }
               break;
           case Constants.BOX_DECLING:
               boolean part1 = false;
               boolean part2 = false;
               boolean part3 = false;
               float newWidth = 0, newHeight = 0, newY = 0;

               if (getWidth() >= initWidth) {
                   newWidth = getWidth() - deltaTime * speed;
                   if (newWidth <= initWidth) {
                       part1 = true;
                       newWidth = initWidth;
                   }
                   setWidth(newWidth);
               }
               if (getHeight() >= initHeight) {
                   newHeight = getHeight() - deltaTime * speed;
                   if (newHeight <= initHeight) {
                       newHeight = initHeight;
                       part2 = true;
                   }
                   setHeight(newHeight);
               }
               if (getY() >= initY) {
                   newY = getY() - deltaTime * speed;
                   if (newY <= initY) {
                       newY = initY;
                       part3 = true;
                   }
                   setY(newY);
               }
               if (part1 && part2 && part3) {
                   status = Constants.BOX_DECLINED;
               }
               sprite.resize((int)newWidth, (int)newHeight);
               sprite.setPos(getX(), getY());
               /*
               if (getWidth() >= initWidth && getHeight() >= initHeight) {
                   Log.e(TAG, "update: declining.");
                   float newWidth = getWidth() - deltaTime * speed;
                   float newHeight = getHeight() - deltaTime * speed;
                   float newY = getY() - deltaTime * speed;
                   boolean part1Ok = false;
                   boolean part2Ok = false;
                   if (newWidth <= initWidth && newHeight <= initHeight) {
                       newWidth = initWidth;
                       newHeight = initHeight;
//                       status = Constants.BOX_DECLINED;
                       part1Ok = true;
                   }
                   if (newY <= initY) {
                       newY = initY;
                       part2Ok = true;
                   }
                   if (part1Ok && part2Ok) {
                       status = Constants.BOX_DECLINED;
                   }
                   setWidth(newWidth);
                   setHeight(newHeight);
                   setY(newY);
                   sprite.resize((int)newWidth, (int)newHeight);
                   sprite.setPos(getX(), getY());
               }
               */
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
        changeSize(status);
    }
}
