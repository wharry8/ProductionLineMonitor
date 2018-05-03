package com.production.w.productionlinemonitor.Model;

import android.content.Context;

import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.R;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

/**
 * Created by w on 2018/5/3.
 */

public class Light {
    private float x;
    private float y;
    private float width;
    private float height;

    Texture grayTexture;
    Texture yellowTexture;
    Texture greenTexture;
    Texture redTexture;

    Sprite sprite;

    private int status;

    public Light () {
    }

    public Light (float x, float y, float width, float height) {
        this.x = x;
        this.x += 2;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init (Context context, int status) {
        this.status = status;
        grayTexture = new Texture(context, R.drawable.gray_light);
        greenTexture = new Texture(context, R.drawable.green_light);
        yellowTexture = new Texture(context, R.drawable.yellow_light);
        redTexture = new Texture(context, R.drawable.red_light);
        sprite = new Sprite((int)width, (int)height);
        sprite.setDisplayPriority(-1);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(x, y);
        switch (status) {
            case Constants.SUCCESS:
                sprite.setTexture(greenTexture);
                break;
            case Constants.WARNING:
                sprite.setTexture(yellowTexture);
                break;
            case Constants.DANGER:
                sprite.setTexture(redTexture);
                break;
            case Constants.STOPPED:
                sprite.setTexture(grayTexture);
                break;
            default:
                break;
        }
    }
    public void render (RenderPassSprite renderPassSprite) {
        renderPassSprite.addSprite(sprite);
    }
}
