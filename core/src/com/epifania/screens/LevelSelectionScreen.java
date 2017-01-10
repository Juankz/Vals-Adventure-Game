package com.epifania.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.ui.LevelItem;
import com.epifania.utils.*;

/**
 * Created by juan on 6/14/16.
 */
public class LevelSelectionScreen extends ScreenAdapter {

    private Stage stage;
    private Skin skin;
    private Viewport viewPort;
    private SpriteBatch batch;

    private Container<Button> leftArrow;
    private Container<Button> rightArrow;
    private ScrollPane scrollPane;
    private Table levelsContainer;
    private Image loadingImage;

    private I18NBundle bundle;

    private boolean goToScreen=false;
    private boolean goToGameScreen=false;
    private Screen nextScreen=null;
    private int level;

    private float d = 1/60f;
    private boolean debug = false;

    public LevelSelectionScreen(SpriteBatch batch, Viewport viewport){
        this.viewPort = viewport;
        this.batch = batch;
        loadAssets();
    }

    @Override
    public void render(float deltaTime) {
        stage.act(Math.min(d,deltaTime));
        stage.draw();
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            for(Actor actor : stage.getActors()){
                System.out.println(actor.getName()+"\t position =("+actor.getX()+","+actor.getY()+") \t"
                        +"size = ("+actor.getWidth()+","+actor.getHeight()+")");
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.D)){
            debug = !debug;
            stage.setDebugAll(debug);
        }
        if(goToScreen){
            if(Assets.instance.update()) {
                ScreenManager.getInstance().setScreen(nextScreen);
            }
        }
        if(goToGameScreen){
            ScreenManager.getInstance().setScreen(new IntroScreen(batch,viewPort,level));
        }
    }

    @Override
    public void show(){
        bundle = Assets.instance.get("i18n/strings_ui");
        stage= new Stage(viewPort,batch);
        skin = Assets.instance.get("user interface/uiskin.json");

        levelsContainer = new Table();

        float pageWidth = stage.getWidth();
        float pageHeight = stage.getHeight();

        for(int i = 0;i<Constants.mapsNames.length;i++){
            final int finalI = i;
            boolean locked = LevelsData.getInstance().getLevelDataOf(i).locked;
            boolean medal = LevelsData.getInstance().getLevelDataOf(i).medal;
            LevelItem levelItem = new LevelItem(skin,bundle,i+1,locked,medal,pageWidth,pageHeight);
            levelItem.setListener(new ClickListener(){
                public void clicked (InputEvent event, float x, float y) {
                    if(!LevelsData.getInstance().getLevelDataOf(finalI).locked) {
                        loadingImage.setVisible(true);
                        goToGameScreen=true;
                        level = finalI;
                    }
                }
            });
            levelsContainer.add(levelItem).size(pageWidth,pageHeight);
        }

        levelsContainer.pack();

        scrollPane = new ScrollPane(levelsContainer);
        scrollPane.setSize(stage.getWidth(),stage.getHeight());
        scrollPane.setPosition(0,0);
        scrollPane.setFlickScroll(false);

        final int scrollX = (int)(pageWidth);

        //Add buttons to scroll through the panel

        float buttonsPad = 175;
        float buttonsWidth = 200;

        leftArrow = new Container<Button>(new Button(skin,"left_arrow_menu"));
        leftArrow.addListener(UI_Utils.clickSound());
        leftArrow.setSize(buttonsWidth,pageHeight);
        leftArrow.setPosition(buttonsPad,stage.getHeight()*0.5f - leftArrow.getHeight()*0.5f);
        leftArrow.setTouchable(Touchable.enabled);
        leftArrow.addListener(new ClickListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                leftArrow.getActor().setColor(0.75f,0.75f,0.75f,1);
                return super.touchDown(event,x,y,pointer,button);
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                leftArrow.getActor().setColor(1,1,1,1);
                super.touchUp(event,x,y,pointer,button);
            }
            public void clicked (InputEvent event, float x, float y) {
                scrollPane.scrollTo(scrollPane.getScrollX()-scrollX,0,scrollX,0);
            }
        });

        rightArrow = new Container<Button>(new Button(skin,"right_arrow_menu"));
        rightArrow.getActor().setTransform(true);
        rightArrow.addListener(UI_Utils.clickSound());
        rightArrow.setSize(buttonsWidth,pageHeight);
        rightArrow.setPosition(stage.getWidth()-rightArrow.getWidth()-buttonsPad,0);
        rightArrow.setTouchable(Touchable.enabled);
        rightArrow.addListener(new ClickListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                rightArrow.getActor().setColor(0.75f,0.75f,0.75f,1);
                return super.touchDown(event,x,y,pointer,button);
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                rightArrow.getActor().setColor(1,1,1,1);
                super.touchUp(event,x,y,pointer,button);
            }
            public void clicked (InputEvent event, float x, float y) {
                scrollPane.scrollTo(scrollPane.getScrollX()+scrollX,0,scrollX,0);
            }
        });

        loadingImage = new Image(skin.getDrawable("loading"));
        loadingImage.setOrigin(Align.center);
        loadingImage.setColor(Color.BLACK);
        loadingImage.setPosition(stage.getWidth()*0.5f - loadingImage.getWidth()*0.5f,
                stage.getHeight()*0.5f - loadingImage.getHeight()*0.5f);
        loadingImage.addAction(Actions.forever(Actions.rotateBy(-7)));
        loadingImage.setVisible(false);

        stage.addActor(scrollPane);
        stage.addActor(leftArrow);
        stage.addActor(rightArrow);

        stage.setDebugAll(debug);
        Gdx.input.setInputProcessor(stage);

//        if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
//            for (Actor actor : stage.getActors()) {
//                UI_Utils.moveWithMouse(actor);
//            }
//        }

    }

    private void loadAssets(){
        TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
        textureParameter.magFilter = Texture.TextureFilter.Linear;
        textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        textureParameter.genMipMaps = true;
        textureParameter.wrapV = Texture.TextureWrap.ClampToEdge;
        textureParameter.wrapU = Texture.TextureWrap.Repeat;
        Assets.instance.load("user interface/level selection/background.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/level1.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/level2.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/level3.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/level4.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/level5.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/lock_chain.png",Texture.class,textureParameter);
        Assets.instance.load("user interface/level selection/play.png",Texture.class,textureParameter);
        Assets.instance.finishLoading();
    }

    @Override
    public void hide(){
        Assets.instance.unload("user interface/level selection/background.png");
        Assets.instance.unload("user interface/level selection/level1.png");
        Assets.instance.unload("user interface/level selection/level2.png");
        Assets.instance.unload("user interface/level selection/level3.png");
        Assets.instance.unload("user interface/level selection/level4.png");
        Assets.instance.unload("user interface/level selection/level5.png");
        Assets.instance.unload("user interface/level selection/lock_chain.png");
        Assets.instance.unload("user interface/level selection/play.png");
    }

    private void goToScreen(Screen screen){
        goToScreen = true;
        nextScreen = screen;
    }
}
