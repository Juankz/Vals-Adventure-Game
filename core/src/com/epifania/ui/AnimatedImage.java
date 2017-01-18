package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by juan on 6/1/16.
 */
public class AnimatedImage extends Image{

    private Animation animation;
    private TextureRegionDrawable drawable;
    private float stateTime = 0;

    public AnimatedImage(Animation animation){
        this.animation = animation;
        this.drawable = new TextureRegionDrawable(animation.getKeyFrame(0));
    }

    @Override
    public void act(float delta){
        super.act(delta);
        stateTime+=delta;
        TextureRegion region = animation.getKeyFrame(stateTime);
        drawable.setRegion(region);
        setHeight(region.getRegionHeight()*getWidth()/region.getRegionWidth());
        this.setDrawable(drawable);
    }

    public void setAnimation(Animation animation){
        this.animation = animation;
    }
}
