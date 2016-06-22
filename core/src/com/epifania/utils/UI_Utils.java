package com.epifania.utils;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.epifania.screens.ScreenManager;

/**
 * Created by juan on 5/29/16.
 */
public class UI_Utils {

    public static ClickListener clickSound(){
        return  new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                SoundManager.playSound(Assets.instance.get("sounds/click1.ogg", Sound.class));
            }
        };
    }

    public static void moveWithMouse(final Actor actor){
        actor.addListener(new ActorGestureListener(){
            float tmpX;
            float tmpY;

            @Override
            public void touchDown (InputEvent event, float x, float y, int pointer, int button) {
                tmpX=x;
                tmpY=y;
            }

            @Override
            public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
                actor.moveBy(x-tmpX,y-tmpY);
            }
        });
    }

    public static Button genericButton(Skin skin, String style,float x, float y){
        Button button = new Button(skin,style);
        button.setPosition(x,y);
        button.addListener(clickSound());
        button.setName(style);
        button.setScale(1);
        button.setOrigin(button.getWidth()*0.5f,button.getHeight()*0.5f);
        button.setTransform(true);
        return button;
    }
    public static Button genericButton(Skin skin, String style, float x, float y, final Screen screen){
        Button button = new Button(skin,style);
        button.setPosition(x,y);
        button.addListener(clickSound());
        button.setName(style);
        button.setScale(1);
        button.setOrigin(button.getWidth()*0.5f,button.getHeight()*0.5f);
        button.setTransform(true);
        button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                ScreenManager.getInstance().setScreen(screen);
            }
        });
        return button;
    }

    public static TextButton genericTextButton(String text, Skin skin, String style, float x, float y, final Screen screen){
        TextButton button = genericTextButton(text,skin,style,screen);
        button.setPosition(x,y);
        return button;
    }
    public static TextButton genericTextButton(String text, Skin skin, String style, final Screen screen){
        TextButton button = genericTextButton(text,skin,style);
        button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                ScreenManager.getInstance().setScreen(screen);
            }
        });
        return button;
    }
    public static TextButton genericTextButton(String text, Skin skin, String style){
        TextButton button = new TextButton(text,skin,style);
        button.addListener(clickSound());
        button.setName(style);
        button.setScale(1);
        button.setOrigin(button.getWidth()*0.5f,button.getHeight()*0.5f);
        button.setTransform(true);
        return button;
    }
}
