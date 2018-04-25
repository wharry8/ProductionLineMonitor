package com.production.w.productionlinemonitor.Model;

import org.w3c.dom.Text;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

/**
 * Created by w on 4/25/2018.
 */

public class BaseModel {
    private float width;
    private float height;
    private float x;
    private float y;
    Texture texture;
    Sprite sprite;

    public BaseModel (float x, float y, float width, float height, Texture texture, Sprite sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.sprite = sprite;
    }

    public void render (RenderPassSprite renderPassSprite) {
        renderPassSprite.addSprite(sprite);
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
}

