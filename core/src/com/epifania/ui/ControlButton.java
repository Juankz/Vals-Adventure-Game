package com.epifania.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.epifania.utils.Assets;

/**
 * Created by juan on 6/21/16.
 */
public class ControlButton extends Table {

    private Image image;
    private Label label;
    private Label fotter;
    private Skin skin;

    public ControlButton(Image image, String header){
        this.image = image;

        skin = Assets.instance.get("user interface/uiskin.json");

        label = new Label(header,skin,"header");
//        label.setColor(Color.BROWN);

        fotter = new Label("",skin);
        fotter.setColor(Color.BROWN);

        Table table = new Table();
        table.add(label).prefWidth(100).pad(10);
        table.setBackground(skin.getDrawable("labelBgndBrown"));
        table.pack();

        float padA = 30;
        float padB = 10;
        float padC = 0;

        this.setBackground(skin.getDrawable("pause_panel"));
        this.add(table).pad(padA).padBottom(padB).padTop(50).fillX();
        this.row();
        this.add(image).pad(padA).padTop(padB);
        this.row();
        this.add(fotter).pad(padB).padTop(padC);
        this.pack();

        float size = Math.max(getWidth(),getHeight());
        this.setWidth(size);
        this.setHeight(size);
        this.setTouchable(Touchable.enabled);
    }

    public void setFooterText(String text){
        fotter.setText(text);
    }
}
