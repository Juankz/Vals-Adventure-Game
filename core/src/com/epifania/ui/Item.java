package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by juan on 6/8/16.
 */
public class Item extends Container<Image> {

    private Skin skin;
    public String key;

    public Item(Skin skin){
        super();
        this.skin = skin;
        init();
    }

    public Item(Skin skin, Image image){
        super(image);
        this.skin = skin;
        init();
    }

    public Item(Skin skin, TextureRegion region){
        super();
        this.skin = skin;
        init();
        super.setActor(new Image(getCuttenRegion(region)));
    }

    private TextureRegion getCuttenRegion(TextureRegion region) {
        int x = 0;
        int y = 0;
        int w = region.getRegionWidth();
        int h = region.getRegionHeight();
        //TODO revisar estas dimensiones
        if(w>70){
            w=70;
        }
        if(h>70){
            h=70;
        }

        return new TextureRegion(region,x,y,70,70);
    }

    private void init(){
        float size = 80;
        prefHeight(size);
        prefWidth(size);
        setTransform(true);
        setBackground(skin.getDrawable("panel_brown2"));
    }
}
