package com.epifania.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.utils.*;

/**
 * Created by juan on 6/20/16.
 */
public class IntroScreen extends ScreenAdapter {

    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private Viewport viewport;
    private TiledMap map;

    private Label header;
    private Label title;
    private Label body;
    private Image image;
    private Image loading;
    private Button nextButton;
    private Table table;

    private I18NBundle bundle;

    private int level;

    private boolean debug = false;

    public IntroScreen(SpriteBatch batch, Viewport viewport, int level){
        this.batch = batch;
        this.viewport = viewport;
        this.level = level;
        //load resources
        Assets.instance.load("i18n/strings_intro", I18NBundle.class);
        Assets.instance.finishLoading();
    }

    @Override
    public void render(float delta){
        stage.act();
        stage.draw();

        if(Assets.instance.update()) {
            nextButton.addAction(Actions.sequence(
                    Actions.visible(true),
                    Actions.touchable(Touchable.enabled),
                    Actions.fadeIn(1)
                )
            );
            loading.setVisible(false);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.D)){
            debug = !debug;
            stage.setDebugAll(debug);
        }
    }

    @Override
    public void resize(int width,int height){
        viewport.update(width,height);
        setPositions();
    }

    public void show(){
        stage = new Stage(viewport,batch);
        skin = Assets.instance.get("user interface/uiskin.json");
        bundle = Assets.instance.get("i18n/strings_intro");
        loadGameScreenAssets(level);
        buildUI();
        addActionsToUI();
        Gdx.input.setInputProcessor(stage);
        stage.setDebugAll(debug);
    }

    private void buildUI(){

        String headerText = bundle.format("header",getRoman(level+1));
        header = new Label(headerText,skin,"header");
        header.setAlignment(Align.center);

        String titleText = bundle.get("title"+level);
        title = new Label(titleText,skin,"h1title");
        title.setAlignment(Align.center);
        title.setWrap(true);
        title.setWidth(stage.getWidth());

        String bodyText = bundle.get("body"+level);
        body = new Label(bodyText,skin,"body");
        body.setAlignment(Align.center);
        body.setWrap(true);
        body.setWidth(stage.getWidth()*0.75f);

        image = new Image(new Texture(Gdx.files.internal("user interface/placeHolder.png")));

        loading = new Image(skin.getDrawable("loading"));
        loading.setOrigin(Align.center);
        loading.addAction(Actions.forever(Actions.rotateBy(-7)));

        nextButton = new Button(skin,"rightArrow");
        nextButton.setTouchable(Touchable.disabled);
        nextButton.setVisible(false);
        nextButton.setColor(1,1,1,0);
        nextButton.addListener(UI_Utils.clickSound());
        nextButton.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                ScreenManager.getInstance().setScreen(getGameScreen(level));
            }
        });

        float padA = 20;
        float padB = 20;
        float padC = 40;

        table = new Table();
        table.add(header).padBottom(padA*0.5f);
        table.row();
        table.add(title).prefWidth(stage.getWidth()).padTop(padA*0.5f).padBottom(padB*0.5f);
        table.row();
        table.add(image).padTop(padB*0.5f).padBottom(padC*0.5f);
        table.row();
        table.add(body).prefWidth(stage.getWidth()*0.75f).padTop(padC*0.5f);
        table.setWidth(stage.getWidth());
        table.pack();

        setPositions();

        stage.addActor(table);
        stage.addActor(loading);
        stage.addActor(nextButton);
    }

    private void addActionsToUI(){
        stage.addAction(Actions.sequence(
                Actions.fadeOut(0),
                Actions.fadeIn(1.0f)
        ));
    }

    private void setPositions(){
        float padEdge = 50;
        table.setPosition(0,stage.getHeight()*0.5f - table.getHeight()*0.5f);

//        loading.setSize(Math.min(nextButton.getWidth(),nextButton.getHeight()),
//                Math.min(nextButton.getWidth(),nextButton.getHeight()));
        loading.setPosition(stage.getWidth() - padEdge - loading.getWidth(), table.getY());

        nextButton.setPosition(loading.getX(),loading.getY());
    }

    /**
     * @param level starts in ZERO
     * @return Game Screen in the selected level
     */
    private Screen getGameScreen(int level){
        String scriptPath = Constants.scriptsNames[level].split(".xml")[0] +"_"+ bundle.getLocale() + ".xml";

        FileHandle file = Gdx.files.internal(scriptPath);
        return new GameScreen(batch,map,file,level);
    }

    private void loadGameScreenAssets(int level){
        //Load resources

        //
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        Assets.instance.load(Constants.mapsNames[level], TiledMap.class, parameters);
        Assets.instance.finishLoading();

        map = Assets.instance.get(Constants.mapsNames[level],TiledMap.class);
        String musicPath = "sounds/"+map.getProperties().get("music","default_music.ogg",String.class);
        Assets.instance.load(musicPath,Music.class);
        Assets.instance.finishLoading();
        SoundManager.stopMusic("sounds/Farm Frolics.ogg");
        SoundManager.playMusic(musicPath,true);

        TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
        textureParameter.wrapU = Texture.TextureWrap.Repeat;
        textureParameter.wrapV = Texture.TextureWrap.ClampToEdge;
        textureParameter.magFilter = Texture.TextureFilter.Nearest;
        textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        textureParameter.genMipMaps = true;
        Assets.instance.load("backgrounds/bg.png",Texture.class,textureParameter);
        Assets.instance.load("backgrounds/bg1.png",Texture.class,textureParameter);
        Assets.instance.load("backgrounds/bg2.png",Texture.class,textureParameter);
        Assets.instance.load("sounds/doorOpen.ogg",Sound.class);
        Assets.instance.load("sounds/jump.ogg",Sound.class);
        Assets.instance.load("sounds/jump_spring.ogg",Sound.class);
        Assets.instance.load("sounds/lose.ogg",Sound.class);
        Assets.instance.load("sounds/pickup_coin.ogg",Sound.class);
        Assets.instance.load("sounds/pickup_object.ogg",Sound.class);
        Assets.instance.load("sounds/footstep.ogg",Sound.class);
    }

    private String getRoman(int number){
        RomanNumeral numeral = new RomanNumeral(number);
        return numeral.toString();
    }


}
