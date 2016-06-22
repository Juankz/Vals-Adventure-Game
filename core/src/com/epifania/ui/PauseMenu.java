package com.epifania.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.epifania.utils.Assets;
import com.epifania.utils.UI_Utils;

/**
 * Created by juan on 6/15/16.
 */
public class PauseMenu extends Group {

    private Skin skin;
    private Table table;
    private Button close;
    private TextButton resume;
    private TextButton settings;
    private TextButton exit;
    private Label label;
    public Listener listener;

    public interface Listener{
        public void exit();
        public void resume1();
    }

    public  PauseMenu(Listener listener1, I18NBundle bundle){
        this.listener = listener1;
        skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
        table = new Table(skin);
        table.setBackground(skin.getDrawable("pause_panel"));

        close = new Button(skin,"close");
        close.addListener(UI_Utils.clickSound());
        close.addListener(new ClickListener(){
           @Override
            public void clicked(InputEvent event, float x, float y){
               listener.resume1();
           }
        });

        label = new Label(bundle.get("pause").toUpperCase(),skin,"header");
        label.setColor(Color.LIGHT_GRAY.BROWN);

        resume = new TextButton(bundle.get("resume").toUpperCase(),skin,"longBrown");
        resume.addListener(UI_Utils.clickSound());
        resume.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                listener.resume1();
            }
        });

        settings = new TextButton(bundle.get("settings").toUpperCase(),skin,"longBrown");
        settings.addListener(UI_Utils.clickSound());
        settings.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                //TODO add settings: may be a window
            }
        });


        exit = new TextButton(bundle.get("exit").toUpperCase(),skin,"longNegative");
        exit.addListener(UI_Utils.clickSound());
        exit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                listener.exit();
            }
        });

        float padA = 20;
        float padB = 0;
        float padC = 30;
        float padD = 70;
        table.add(label).pad(padA).padBottom(padC).padTop(padD);
        table.row();
        table.add(resume).pad(padA);
        table.row();
        table.add(settings).pad(padA);
        table.row();
        table.add(exit).pad(padA).padBottom(padD);
        table.pad(padB);
        table.pack();

        table.setWidth(Math.max(table.getWidth(),table.getHeight()));
        table.setHeight(Math.max(table.getWidth(),table.getHeight()));

        setButtonPosition();

        addActor(table);
//        addActor(close);
    }

    @Override
    public float getWidth(){
        return table.getWidth() + close.getWidth()*0.5f;
    }

    @Override
    public float getHeight(){
        return table.getHeight() + close.getHeight()*0.25f;
    }

    @Override
    public void setPosition (float x, float y) {
        super.setPosition(x,y);
        setButtonPosition();
    }

    private void setButtonPosition(){
        close.setPosition(-close.getWidth()*0.5f,table.getHeight()-close.getHeight()*0.75f);
    }
}
