package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.utils.Assets;
import com.epifania.utils.InputController;

/**
 * Created by juan on 8/29/16.
 */
public class StageHUD extends Stage {

    private static final String tag = "StageHUD class";
    private InputController inputController;
    private Button leftButton;
    private Button rightButton;
    private Button upButton;
    private Button downButton;
    private Table controlTable;
    private boolean isController = false;
    public boolean isControllerActive = false;

    public StageHUD() {
        super();
    }

    public StageHUD(Viewport viewport) {
        super(viewport);
    }

    public StageHUD(Viewport viewport, Batch batch) {
        super(viewport, batch);
    }

    public StageHUD(Viewport viewPort, Batch batch, InputController inputController){
        super(viewPort,batch);
        this.inputController = inputController;
        setAsController();
    }

    @Override
    public void act() {
        super.act();

        if(!(isController && isControllerActive)) return;

        if(leftButton.isOver())
            inputController.left();
        if(rightButton.isOver())
            inputController.right();
        if(upButton.isOver())
            inputController.up();
        if(downButton.isOver())
            inputController.down();
    }

    private void init(){
        float size = 70;
        Skin skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
        leftButton = new Button(skin,"leftArrow");
        leftButton.setSize(70,70);
        leftButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return isControllerActive;
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputController.stopHorizontal();
            }
        });
        rightButton = new Button(skin,"rightArrow");
        rightButton.setSize(70,70);
        rightButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return isControllerActive;
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputController.stopHorizontal();
            }
        });
        upButton = new Button(skin,"upArrow");
        upButton.setSize(70,70);
        upButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return isControllerActive;
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputController.stopVertical();
            }
        });
        downButton = new Button(skin,"downArrow");
        downButton.setSize(70,70);
        downButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return isControllerActive;
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputController.stopVertical();
            }
        });

        float pad = 60;
        controlTable = new Table();
        controlTable.add(leftButton).padLeft(pad);
        controlTable.add(rightButton).padLeft(pad);
        controlTable.add().expandX();
        controlTable.add(downButton).padRight(pad);
        controlTable.add(upButton).padRight(pad);
        controlTable.pack();
        controlTable.setWidth(getWidth());
        controlTable.setPosition(0,50);
    }

    public void setAsController(){
        init();
        addActor(controlTable);
        isController = true;
        isControllerActive = true;
    }
    public void removeAsController(){
        controlTable.remove();
        isController = false;
        isControllerActive = false;
    }

    public void setActive(boolean b){
        isControllerActive = b;
    }
}
