package com.epifania.ui;

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
        super(new Image(region));
        this.skin = skin;
        init();
    }

    private void init(){
        float size = 70;
        prefHeight(size);
        prefWidth(size);
        setTransform(true);
        setBackground(skin.getDrawable("panel_brown2"));
    }
}
