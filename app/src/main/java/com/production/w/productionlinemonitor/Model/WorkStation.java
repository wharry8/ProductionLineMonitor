package com.production.w.productionlinemonitor.Model;

import android.content.Context;
import android.util.Log;

import com.production.w.productionlinemonitor.Helper.Constants;
import com.production.w.productionlinemonitor.R;

import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;

import static android.support.constraint.Constraints.TAG;

/*
 * Created by w on 2018/5/2.
 */

public class WorkStation {
    private Area storageArea;
    private Area processingArea;
    private Area completionArea;

    private Body body;
    private Hand hand;
    private Platform platformTop, platformBottom;
    private Light lightTop, lightMiddle, lightBottom;

    private Context context;

    boolean isMatch;

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
        this.isMatch = false;
    }

    public void init (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        initArea(unitWidth, unitHeight);
        initBody(glWidth, glHeight, unitWidth, unitHeight);
        initPlatform(glWidth, glHeight, unitWidth, unitHeight);
        initHand(glWidth, glHeight, unitWidth, unitHeight);
        initLight(glWidth, glHeight, unitWidth, unitHeight);
    }

    private void initLight (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        float lightWidth = 25;
        float lightHeight = 25;
        float lightTopX = x;
        float lightTopY = getPlatformTopY(glWidth, glHeight, unitWidth, unitHeight) - getPlatformHeight(glWidth, glHeight, unitWidth, unitHeight) / 2 - lightHeight / 2;
        lightTop = new Light(lightTopX, lightTopY, lightWidth, lightHeight);
        lightTop.init(context, Constants.STOPPED);

        float lighMiddleX = x;
        float lightMiddleY = glHeight / 2;
        lightMiddle = new Light(lighMiddleX, lightMiddleY, lightWidth, lightHeight);
        lightMiddle.init(context, Constants.STOPPED);


        float lightBottomX = x;
        float lightBottomY = getPlatformBottomY(glWidth, glHeight, unitWidth, unitHeight) + getPlatformHeight(glWidth, glHeight, unitWidth, unitHeight) / 2 + lightHeight / 2;
        lightBottom = new Light(lightBottomX, lightBottomY, lightWidth, lightHeight);
        lightBottom.init(context, Constants.STOPPED);
        Log.e(TAG, "initLight: light!");
    }
    private float getPlatformTopY (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        return glHeight / 2 -  unitHeight * 12 / 2 - getPlatformHeight(glWidth, glHeight, unitWidth, unitHeight) / 2 + unitHeight / 2;
    }
    private float getPlatformHeight (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        return unitHeight * 3;
    }
    private float getPlatformBottomY (float glWidth, float glHeight, float unitWidth, float unitHeight) {
        return  glHeight / 2 + unitHeight * 12 / 2 + getPlatformHeight(glWidth, glHeight, unitWidth, unitHeight) / 2 - unitHeight / 2;
    }

    private void initArea (float unitWidth, float unitHeight) {
        storageArea = new Area();
        processingArea = new Area();
        completionArea = new Area();

        storageArea.x = x - unitWidth / 2 - unitWidth * 2;
        storageArea.width = unitWidth;

        processingArea.x = x - unitWidth / 2 - unitWidth;
        processingArea.width = unitWidth;

        completionArea.x = x + unitWidth / 2 + unitWidth;
        completionArea.width = unitWidth;
    }

    public void render (RenderPassSprite renderPassSprite) {
        body.render(renderPassSprite);
        platformTop.render(renderPassSprite);
        platformBottom.render(renderPassSprite);
        hand.render(renderPassSprite);
        lightMiddle.render(renderPassSprite);
        lightTop.render(renderPassSprite);
        lightBottom.render(renderPassSprite);
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

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public void updateLight (int status) {
        lightMiddle.update(status);
    }

    public Area getStorageArea() {
        return storageArea;
    }

    public void setStorageArea(Area storageArea) {
        this.storageArea = storageArea;
    }

    public Area getProcessingArea() {
        return processingArea;
    }

    public void setProcessingArea(Area processingArea) {
        this.processingArea = processingArea;
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
