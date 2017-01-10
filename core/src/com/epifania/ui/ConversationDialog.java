package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * Created by juan on 5/24/16.
 */
public class ConversationDialog extends Group {

    public interface Listener{
        boolean action();
    }

    private Label text;
    private Table container;
    private Button next;
    public Listener listener;

    public ConversationDialog(Skin skin){
        text = new Label("",skin,"dialog");
        text.setWrap(true);
        text.setAlignment(Align.center);
        container = new Table();
        container.setBackground(skin.getDrawable("panel_brown"));
        container.add(text).prefWidth(300).top().padBottom(30).padTop(15).padLeft(15).padRight(15).center();

        next = new Button(skin,"next");
        next.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                listener.action();
            }
        });
        setButtonPosition();
        addActor(container);
        addActor(next);
    }

    public void setText(String text) {
        this.text.setText(text);
        container.pack();
        setButtonPosition();
    }

    public void setButtonPosition(){
        float x = container.getX() + container.getWidth()*0.5f - next.getWidth()*0.5f;
        float y = container.getY() - next.getHeight()*0.5f;
        next.setPosition(x,y);
    }

    @Override
    public float getWidth (){
        float width = 0;
        for(Actor actor : getChildren()){
            width = Math.max(width,actor.getWidth());
        }
        return  width;
    }

    @Override
    public void setPosition (float x, float y) {
        super.setPosition(x,y);
        setButtonPosition();
    }

    public void setVisible(boolean visible){
        setVisible(visible,visible);
    }

    public void setVisible(boolean visible , boolean buttonVisible){
        super.setVisible(visible);
        next.setVisible(buttonVisible);
    }
}
