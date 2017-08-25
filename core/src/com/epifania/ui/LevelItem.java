package com.epifania.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.epifania.utils.Assets;
import com.epifania.utils.RomanNumeral;

/**
 * Created by juan on 1/9/17.
 */
public class LevelItem extends WidgetGroup {

    static String path="user interface/level_selection.atlas";
    static final String tag ="LevelItem";
    Image background;
    Image frame;
    Image levelImage;
    Image medalImage;
    Image play;
    Label title;
    boolean locked;

    public LevelItem(Skin skin, I18NBundle bundle, int level, boolean locked, boolean medal, float width, float height){
        this.setFillParent(true);
        this.locked = locked;

        TextureAtlas atlas = Assets.instance.get(path,TextureAtlas.class);

        background = new Image(atlas.findRegion("background"));
        frame = new Image(atlas.findRegion("frame"));
        medalImage = new Image(skin.getDrawable("medal"));
        play = new Image(skin.getDrawable("play"));
        medalImage = new Image(skin.getDrawable("medal"));
        title = new Label(bundle.get("chapter").toUpperCase()+" "+new RomanNumeral(level).toString(),skin,"header");

        //More actors settings
        title.setColor(0,0,0,1);

        if(locked){
            levelImage = new Image(atlas.findRegion("level"+level+"_locked"));
        }else{
            levelImage = new Image(atlas.findRegion("level"+level));
        }

        //set visibility
        if(locked){
            medalImage.setVisible(false);
            play.setVisible(false);
        }else{
            if(!medal)
                medalImage.setVisible(false);
        }

        setActorsPosition(width,height);

        //Add actors to this group
        this.addActor(background);
        this.addActor(levelImage);
        this.addActor(frame);
        this.addActor(play);
        this.addActor(medalImage);
        this.addActor(title);
    }

    public void setActorsPosition(float width, float height){
        //Set positions and sizes
        background.setPosition(0,0);
        background.setSize(width,height);

        float hw = width*0.5f; //half width
        float hh = height*0.5f;

        frame.setPosition(hw - frame.getWidth()*0.5f , hh - frame.getHeight()*0.5f + 25);
        levelImage.setPosition(getCenteredX(frame,levelImage),getCenteredY(frame,levelImage));
        title.setPosition(getCenteredX(background,title),frame.getY() - title.getHeight() -10);
        play.setPosition(frame.getX()+frame.getWidth() - 150 , frame.getY()+100);
        play.setTouchable(Touchable.disabled);
        medalImage.setPosition(getCenteredX(frame,medalImage),frame.getY() + frame.getHeight()-medalImage.getHeight());
    }

    public float getCenteredX(Actor reference, Actor current){
        return (float)(reference.getX() + reference.getWidth()*0.5 - current.getWidth()*0.5f);
    }
    public float getCenteredY(Actor reference, Actor current){
        return (float)(reference.getY() + reference.getHeight()*0.5 - current.getHeight()*0.5f);
    }

    public void setListener(ClickListener clickListener){
        if(!locked){
            frame.addListener(clickListener);
        }
    }
}
