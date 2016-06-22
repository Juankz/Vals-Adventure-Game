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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.utils.*;

/**
 * Created by juan on 6/14/16.
 */
public class LevelSelectionScreen extends ScreenAdapter {

    private Stage stage;
    private Skin skin;
    private Viewport viewPort;
    private SpriteBatch batch;

    private Image background;
    private Image[] levelImages;
    private Button leftArrow;
    private Button rightArrow;
    private ScrollPane scrollPane;
    private Table levelsContainer;
    private Label instruction;
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

        background = new Image(
                new TextureRegion(
                        Assets.instance.get("user interface/level selection/background.png",Texture.class),0,0,(int)stage.getWidth(),(int)stage.getHeight()));
        background.setPosition(0,0);
        background.setFillParent(true);

        levelImages = new Image[3];
        for(int i = 0;i<levelImages.length;i++){
            levelImages[i] = new Image(Assets.instance.get("user interface/level selection/level"+(i+1)+".png",Texture.class));
        }

        float buttonsPad = 75;

        leftArrow = new Button(skin,"leftArrow");
        leftArrow.addListener(UI_Utils.clickSound());
        leftArrow.setPosition(buttonsPad,stage.getHeight()*0.5f - leftArrow.getHeight()*0.5f);

        rightArrow = new Button(skin,"rightArrow");
        rightArrow.addListener(UI_Utils.clickSound());
        rightArrow.setPosition(stage.getWidth()-rightArrow.getWidth()-buttonsPad,stage.getHeight()*0.5f - rightArrow.getHeight()*0.5f);

        levelsContainer = new Table();

        float pad = 50;
        float pageWidth = 750;
        float pageHeight = 600;

        for(int i = 0;i<levelImages.length;i++){
            Stack stack = new Stack();
            stack.add(levelImages[i]);
            if(LevelsData.getInstance().getLevelDataOf(i).locked) {
                stack.add(new Image(Assets.instance.get("user interface/level selection/lock_chain.png", Texture.class)));
            }
            Container<Stack> container = new Container<Stack>(stack);
            container.background(skin.getDrawable("panel_brown"));
            container.align(Align.center);
            levelsContainer.add(container).width(pageWidth).height(pageHeight).padRight(pad).padLeft(pad);
        }
        levelsContainer.row();

        for(int i = 0;i<levelImages.length;i++){
            Table smallTable = new Table();
            smallTable.background(skin.getDrawable("panel_brown"));
            String levelName = bundle.get("chapter").toUpperCase()+" "+new RomanNumeral(i+1).toString();
            Label label = new Label(levelName,skin,"header");
            Container<Label> container = new Container<Label>(label);
            container.pad(20).padLeft(pad).padRight(pad);

            if(LevelsData.getInstance().getLevelDataOf(i).medal){
                Image medal = new Image(skin.getDrawable("medal"));
                float h = 75;
                float w = medal.getWidth()*h/medal.getHeight();
                smallTable.add(medal).size(w,h).padRight(20);
            }

            smallTable.add(label).padLeft(pad).padRight(pad).expandX();
            smallTable.pack();
            levelsContainer.add(smallTable).fill(true,false).padRight(pad).padLeft(pad);
        }

        levelsContainer.pack();

        scrollPane = new ScrollPane(levelsContainer);
        scrollPane.setSize(pageWidth+2*pad,levelsContainer.getHeight());
        scrollPane.setPosition(stage.getWidth()*0.5f - scrollPane.getWidth()*0.5f,
                stage.getHeight()*.5f  - scrollPane.getHeight()*0.5f);
        scrollPane.setFlickScroll(false);

        instruction = new Label(bundle.get("tap_inst"),skin);
        instruction.setAlignment(Align.center);
        instruction.setWrap(true);
        instruction.setWidth(150);
        instruction.setPosition(stage.getWidth() - instruction.getWidth() - 50,
                scrollPane.getY()+scrollPane.getHeight()-instruction.getHeight() - 30);
        instruction.setColor(0,0,0,0.8f);

        for(int i = 0;i<levelImages.length;i++){
            final int finalI = i;
            levelImages[i].addListener(new ClickListener(){
               public void clicked (InputEvent event, float x, float y) {
                   if(!LevelsData.getInstance().getLevelDataOf(finalI).locked) {
                       loadingImage.setVisible(true);
                       goToGameScreen=true;
                       level = finalI;
                   }
               }
            });
        }

        final int scrollX = (int)(pageWidth+2*pad);

        rightArrow.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                scrollPane.scrollTo(scrollPane.getScrollX()+scrollX,0,scrollX,0);
            }
        });

        leftArrow.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                scrollPane.scrollTo(scrollPane.getScrollX()-scrollX,0,scrollX,0);
            }
        });

        loadingImage = new Image(skin.getDrawable("loading"));
        loadingImage.setOrigin(Align.center);
        loadingImage.setColor(Color.BLACK);
        loadingImage.setPosition(stage.getWidth()*0.5f - loadingImage.getWidth()*0.5f,
                stage.getHeight()*0.5f - loadingImage.getHeight()*0.5f);
        loadingImage.addAction(Actions.forever(Actions.rotateBy(-7)));
        loadingImage.setVisible(false);

        stage.addActor(background);
        stage.addActor(scrollPane);
        stage.addActor(leftArrow);
        stage.addActor(rightArrow);
        stage.addActor(instruction);

        stage.setDebugAll(debug);
        Gdx.input.setInputProcessor(stage);

        if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
            for (Actor actor : stage.getActors()) {
                UI_Utils.moveWithMouse(actor);
            }
        }

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
    }

    private void goToScreen(Screen screen){
        goToScreen = true;
        nextScreen = screen;
    }
}
