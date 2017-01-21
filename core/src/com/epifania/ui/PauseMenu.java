package com.epifania.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.epifania.utils.Assets;
import com.epifania.utils.UI_Utils;

/**
 * Created by juan on 6/15/16.
 */
public class PauseMenu extends Table {

    private Skin skin;
    private Table root;
    private TextButton resume;
    private TextButton settings;
    private TextButton exit;
    private Label label;
    public Listener listener;

    public interface Listener{
        void exit();
        void resume1();
        void showSettings();
    }

    public  PauseMenu(final Listener listener1, I18NBundle bundle){
        this.listener = listener1;
        skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
        root = new Table(skin);
        root.setBackground(skin.getDrawable("pause_panel"));

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
                listener.showSettings();
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
        root.add(label).pad(padA).padBottom(padC).padTop(padD);
        root.row();
        root.add(resume).pad(padA);
        root.row();
        root.add(settings).pad(padA);
        root.row();
        root.add(exit).pad(padA).padBottom(padD);
        root.pad(padB);
        root.pack();
        root.setTransform(true);
        root.setOrigin(Align.center);

        float size=Math.max(root.getWidth(), root.getHeight());

        add(root).center().size(size);

        this.setBackground(skin.getDrawable("opaque_pixel"));
        this.setTouchable(Touchable.enabled);
        this.setFillParent(true);
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        super.setScale(scaleX, scaleY);
        root.setScale(scaleX, scaleY);
    }

    public void show(){
        float duration = 0.2f;
        this.setScale(1.1f,1.1f);
        this.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.show(),
                Actions.parallel(
                        Actions.fadeIn(duration),
                        Actions.scaleTo(1f,1f,duration*2,Interpolation.bounceOut))
        ));
    }
    public void hide(){
        float duration = 0.2f;
        this.addAction(Actions.sequence(
                Actions.alpha(1),
                Actions.parallel(
                        Actions.fadeOut(duration),
                        Actions.scaleTo(1.1f,1.1f,duration*2)),
                Actions.hide()
        ));
    }
}
