package com.production.w.productionlinemonitor.Model;

import android.content.Context;
import android.widget.Spinner;

import com.production.w.productionlinemonitor.MainActivity;
import com.production.w.productionlinemonitor.R;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

/**
 * Created by w on 2018/5/2.
 */

public class WorkStation {
    private Area preparationArea;
    private Area workingArea;
    private Area completionArea;

    private Body body;
    private Hand hand;
    private Platform platformTop, platformBottom;

    private Context context;

    // light
    // cnc

    private float x;
    private float y;

    public WorkStation () {
    }

    public WorkStation (Context context, float x, float y) {
        this.x = x;
        this.y = y;
        this.context = context;
    }

    public void init (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        initArea(unitWidth, unitHeight);
        initBody(glWidth, glHeight, unitWidth, unitHeight);
        initPlatform(glWidth, glHeight, unitWidth, unitHeight);
        initHand(glWidth, glHeight, unitWidth, unitHeight);
    }

    private void initArea (float unitWidth, float unitHeight) {
        preparationArea = new Area();
        workingArea = new Area();
        completionArea = new Area();

        preparationArea.x = x - unitWidth / 2 - unitWidth * 2;
        preparationArea.width = unitWidth;

        workingArea.x = x - unitWidth / 2 - unitWidth;
        workingArea.width = unitWidth;

        completionArea.x = x + unitWidth / 2 + unitWidth;
        completionArea.width = unitWidth;
    }

    public void render (RenderPassSprite renderPassSprite) {
        body.render(renderPassSprite);
        platformTop.render(renderPassSprite);
        platformBottom.render(renderPassSprite);
        hand.render(renderPassSprite);
    }
    private void initBody (float glWidth, float glHeight, float unitWidth, float unitHeight) {

        float bodyX = x;
        float bodyY = glHeight / 2;
        float bodyWidth = unitWidth;
        float bodyHeight = unitHeight * 12;
        int bodyPriority = 0;

        Texture texture = new Texture(context, R.drawable.body);
        Sprite sprite = new Sprite((int)bodyWidth, (int)bodyHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(bodyX, bodyY);
        sprite.setDisplayPriority(bodyPriority);
        sprite.setTexture(texture);
        body = new Body(bodyX, bodyY, bodyWidth, bodyHeight, texture, sprite);
    }
    private void initPlatform (float glWidth, float glHeight, float unitWidth, float unitHeight) {

        float platformTopWidth = unitWidth * 2;
        float platformTopHeight = unitHeight * 3;
        float platformTopX = x;
        float platformTopY = glHeight / 2 -  unitHeight * 12 / 2 - platformTopHeight / 2 + unitHeight / 2;
        int platformPriority = 5;

        float platformBottomWidth = platformTopWidth;
        float platformBottomHeight = platformTopHeight;
        float platformBottomX = x;
        float platformBottomY = glHeight / 2 + unitHeight * 12 / 2 + platformBottomHeight / 2 - unitHeight / 2;

        Texture texture = new Texture(context, R.drawable.platform);
        Sprite sprite = new Sprite((int)platformTopWidth, (int) platformTopHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(platformTopX, platformTopY);
        sprite.setDisplayPriority(platformPriority);
        sprite.setTexture(texture);
        platformTop = new Platform(platformTopX, platformTopY, platformTopWidth, platformTopHeight, texture, sprite);

        texture = new Texture(context, R.drawable.platform_inverse);
        sprite = new Sprite((int)platformBottomWidth, (int)platformBottomHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(platformBottomX, platformBottomY);
        sprite.setDisplayPriority(platformPriority);
        sprite.setTexture(texture);
        platformBottom = new Platform(platformBottomX, platformBottomY, platformBottomWidth, platformBottomHeight, texture, sprite);

    }
    private void initHand (float glWidth, float glHeight, float unitWidth, float unitHeight) {

        float handWidth = unitWidth * 1;
        float handHeight = unitHeight * 2;
        float handX = x - unitWidth / 2 - handWidth / 2;
        float handY = glHeight / 2;
        int handPriority = 0;

        Texture texture = new Texture(context, R.drawable.hand);
        Sprite sprite = new Sprite((int)handWidth, (int)handHeight);
        sprite.setPivot(0.5f, 0.5f);
        sprite.setPos(handX, handY);
        sprite.setDisplayPriority(handPriority);
        sprite.setTexture(texture);
        hand = new Hand(handX, handY, handWidth, handHeight, texture, sprite);

        hand.setHorizontalDistance(unitHeight * 12 / 2);
    }

    public Area getPreparationArea() {
        return preparationArea;
    }

    public void setPreparationArea(Area preparationArea) {
        this.preparationArea = preparationArea;
    }

    public Area getWorkingArea() {
        return workingArea;
    }

    public void setWorkingArea(Area workingArea) {
        this.workingArea = workingArea;
    }

    public Area getCompletionArea() {
        return completionArea;
    }

    public void setCompletionArea(Area completionArea) {
        this.completionArea = completionArea;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public Platform getPlatformTop() {
        return platformTop;
    }

    public void setPlatformTop(Platform platformTop) {
        this.platformTop = platformTop;
    }

    public Platform getPlatformBottom() {
        return platformBottom;
    }

    public void setPlatformBottom(Platform platformBottom) {
        this.platformBottom = platformBottom;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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
