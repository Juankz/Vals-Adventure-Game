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
//    private Container<Label> container;
    private Table container;
    private Button next;
    private Cell cell;
    public Listener listener;

    public ConversationDialog(Skin skin){
        text = new Label("",skin,"dialog");
        text.setWrap(true);
        text.setAlignment(Align.center);
//        container = new Container<Label>(text);
        container = new Table();
        container.setBackground(skin.getDrawable("panel_brown"));
        cell = container.add(text).prefWidth(200).top().padBottom(30).padTop(15).padLeft(15).padRight(15).center();
//        container.top();
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
//        container.setPosition(x,y);
        setButtonPosition();
//        Gdx.app.debug("Conversation Dialog","pos = ["+x+","+y+"]");
//        Gdx.app.debug("Conversation Dialog","Button pos = ["+next.getX()+","+next.getY()+"]");
//        Gdx.app.debug("Conversation Dialog","container size = ["+container.getWidth()+","+container.getHeight()+"]");
//        Gdx.app.debug("Conversation Dialog","text = "+text.getText());
    }

    public void setVisible(boolean visible){
        setVisible(visible,visible);
    }

    public void setVisible(boolean visible , boolean buttonVisible){
        super.setVisible(visible);
        next.setVisible(buttonVisible);
    }
}
