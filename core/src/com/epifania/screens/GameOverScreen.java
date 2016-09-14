package com.epifania.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.ui.ImageAnimation;
import com.epifania.utils.Assets;
import com.epifania.utils.Constants;
import com.epifania.utils.LevelsData;
import com.epifania.utils.UI_Utils;

/**
 * Created by juan on 6/15/16.
 */
public class GameOverScreen extends ScreenAdapter {
    private static final String tag = "Game Over Screen";

    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private Viewport viewport;

    //UI Vars
    private Image background;
    private ImageAnimation victoryAnim;
    private Button nextButton;
    private Label epilogueLabel;
    private Label coinsLabel;
    private Image coinImage;
    private Table coinsTable;
    private Image medal;

    private String epilogueText = "Val Journey has just began and danger awaits in her way";
    private String coinsText = "";
    private int labelIndex = 0;
    private int labelIndex2 = 0;

    private Sprite floor;
    private Sprite bgnd;

    private I18NBundle bundle;

    //data
    private int totalCoins = 0;
    private int coinsAmount = 0;
    private int level = 0;

    //vars
    float scroll = 0;
    float max = 1024;
    float inc = 5;
    private ShaderProgram circle;
    private float radiusA = 0.3f;
    private float radiusB = 1f;
    private float radius = 0;
    private float[] resolution;

    private States state = States.DELAY;
    private static final float time1=0.5f;
    private static final float time2=0.5f;
    private float stateTime = 0;

    Vector3 tmp = new Vector3();

    public GameOverScreen(SpriteBatch batch,int coinsAmount, int totalCoins, int level){
        this.batch = batch;
        this.coinsAmount = coinsAmount;
        this.totalCoins = totalCoins;
        this.level = level;
        circle = new ShaderProgram(Gdx.files.internal("shaders/vignette.vert"),Gdx.files.internal("shaders/vignette.frag"));
        resolution = new float[2];
        init();
    }

    private void buildUI(){
        bundle = Assets.instance.get("i18n/strings_outro");
        background = new Image(Assets.instance.get("user interface/game over/background1.png",Texture.class));
        background.setPosition(stage.getWidth()*0.5f - background.getWidth()*0.5f,
                stage.getHeight()*0.5f-background.getHeight()*0.5f);

//        background.setPosition(stage.getWidth()*0.5f - background.getWidth()*0.5f,-background.getHeight() - 20);

        float offset = -158;
        victoryAnim = new ImageAnimation(Assets.instance.valAssets.walkRight);
        victoryAnim.setWidth(120);
        victoryAnim.setPosition(background.getX()+background.getWidth()*0.5f-victoryAnim.getWidth()*0.5f,
                background.getY()+background.getHeight()*0.5f-victoryAnim.getHeight()*0.5f + offset);

        nextButton = new Button(skin,"rightArrow");
        nextButton.setPosition(stage.getWidth() - nextButton.getWidth() - 50,100 - nextButton.getHeight()*0.5f);
        nextButton.addListener(UI_Utils.clickSound());
        nextButton.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                ScreenManager.getInstance().setScreen(new LevelSelectionScreen(batch,viewport));
            }
        });

        epilogueLabel = new Label(bundle.get("content"+level),skin,"middle");
        epilogueLabel.setWrap(true);
        epilogueLabel.setWidth(stage.getWidth()*0.5f);
        epilogueLabel.setPosition(stage.getWidth()*0.25f,100-epilogueLabel.getHeight()*0.5f);
        epilogueLabel.setAlignment(Align.center);

        coinImage = new Image(skin.getDrawable("gold"));
        coinImage.setSize(50,50);
        coinImage.setOrigin(coinImage.getWidth()*0.5f,coinImage.getHeight()*0.5f);
        coinImage.setPosition(background.getX() - 100,stage.getHeight() - coinImage.getHeight() - 50);

        coinsLabel = new Label(coinsAmount+"/"+totalCoins,skin,"middle_outline");
        coinsLabel.setColor(Color.GOLD);
        coinsLabel.setPosition(coinImage.getX() + coinImage.getWidth() + 20,
                coinImage.getY());

        float cw = coinImage.getWidth() + coinsLabel.getWidth() + 20;

        medal = new Image(skin.getDrawable("medal"));
        medal.setPosition(coinImage.getX() + cw*0.5f - medal.getWidth()*0.5f
                ,coinImage.getY() - 30 - medal.getHeight());
        medal.setOrigin(medal.getWidth()*0.5f,medal.getHeight());

        TextureRegion region =new TextureRegion(Assets.instance.get("user interface/game over/background1.png",Texture.class),
                0,0,512*4,512);
        bgnd = new Sprite(region);
        bgnd.setPosition(stage.getWidth()*0.5f - 512*0.5f,
                stage.getHeight()*0.5f-512*0.5f);
        bgnd.setScale(1);
        bgnd.setOriginCenter();
        floor = new Sprite(region);
        floor.setSize(512,512);
        floor.setPosition(bgnd.getX()+bgnd.getWidth(),bgnd.getY());
        floor.setScale(1);
        floor.setOriginCenter();

//        stage.addActor(background);
        stage.addActor(victoryAnim);
        stage.addActor(nextButton);
        stage.addActor(epilogueLabel);
        stage.addActor(coinImage);
        stage.addActor(coinsLabel);
        if(coinsAmount==totalCoins) {
            stage.addActor(medal);
        }

        if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
            for (Actor actor : stage.getActors()) {
                UI_Utils.moveWithMouse(actor);
            }
        }
    }

    private void addActions(){
        coinImage.setScale(0);
        coinsText = String.valueOf(coinsLabel.getText());
        epilogueText = String.valueOf(epilogueLabel.getText());
        coinsLabel.setText("");
        epilogueLabel.setText("");
        medal.setScale(0);
        nextButton.setTouchable(Touchable.disabled);
        float nbposX = nextButton.getX();
        nextButton.setX(stage.getWidth() + 100);

        float actionTime=0.25f;
        float actionTime2=1f;
        float d = 0.4f;
        float delay = d;

        coinImage.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.scaleTo(1,1,actionTime, Interpolation.bounceOut)));

        delay += actionTime + d;

        Runnable coinsLabelRunnable = new Runnable() {
            @Override
            public void run() {
                coinsLabel.setText(coinsLabel.getText()+String.valueOf(coinsText.charAt(labelIndex2)));
                labelIndex2++;
            }
        };

        coinsLabel.addAction(
                Actions.sequence(
                        Actions.delay(delay),
                        Actions.repeat(coinsText.length(),
                                Actions.sequence(
                                        Actions.delay(0.05f),
                                        Actions.run(coinsLabelRunnable)
                                )
                        )
                )
        );

        delay += 0.05f*coinsText.length() + d;

        medal.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.scaleTo(1,1,actionTime,Interpolation.bounceOut)));

        delay += actionTime + d;

        Runnable epilogueLabelRunnable = new Runnable() {
            @Override
            public void run() {
                epilogueLabel.setText(epilogueLabel.getText()+String.valueOf(epilogueText.charAt(labelIndex)));
                labelIndex++;
            }
        };

        epilogueLabel.addAction(
                Actions.sequence(
                        Actions.delay(delay),
                        Actions.repeat(epilogueText.length(),
                                Actions.run(epilogueLabelRunnable)
                        )
                )
        );

        delay+=(1/60f) * epilogueText.length() + d;

        nextButton.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.moveTo(nbposX,nextButton.getY(),actionTime2,Interpolation.bounceOut),
                Actions.touchable(Touchable.enabled)
        ));
    }

    @Override
    public void hide(){
        batch.setShader(null);
        Assets.instance.unload("user interface/game over/background1.png");
        Assets.instance.unload("user interface/game over/background2.png");
        Assets.instance.unload("user interface/game over/mask.png");
    }

    private void init(){
        TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
        parameter.wrapU = Texture.TextureWrap.Repeat;
        parameter.wrapV = Texture.TextureWrap.Repeat;
        parameter.genMipMaps = true;
//        parameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        Assets.instance.load("user interface/game over/background1.png",Texture.class,parameter);
        Assets.instance.load("user interface/game over/background2.png",Texture.class,parameter);
        Assets.instance.load("user interface/game over/mask.png",Texture.class,parameter);
        Assets.instance.load("i18n/strings_outro", I18NBundle.class);
//        Assets.instance.finishLoading();

        modifyLevelsData();
    }

    private void modifyLevelsData(){
        LevelsData.getInstance().getLevelDataOf(level).medal |= coinsAmount==totalCoins;
        LevelsData.LevelData nextLevel = LevelsData.getInstance().getLevelDataOf(level+1);
        if(nextLevel!=null){
            nextLevel.locked = false;
        }
    }

    @Override
    public void render(float delta){
        switch (state){
            case DELAY:
                //update delay
                if(Assets.instance.update()&&stateTime>time1){
                    state = States.TRANSITION1;
                    stateTime = 0;
                    buildUI();
                }else {
                    stateTime+=delta;
                }
                return;
            case TRANSITION1:
                stateTime+=delta;
                radius = (stateTime / time2)*0.3f;
                draw(radius,radius);
                if (stateTime > time2) {
                    state = States.NORMAL;
                    addActions();
                }
                break;
            case NORMAL:
                draw(radiusA,radiusB);
                break;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            for(Actor actor : stage.getActors()){
                System.out.println(actor.getName()+"\t position =("+actor.getX()+","+actor.getY()+") \t"
                        +"size = ("+actor.getWidth()+","+actor.getHeight()+")");
            }
        }
    }

    private void draw(float radiusA, float radiusB){
        tmp.set(stage.getCamera().position);
        scroll += inc;
        stage.getCamera().position.x = scroll;
        if(scroll>=max+tmp.x){
            scroll=tmp.x;
        }
        stage.getCamera().update();
        batch.setProjectionMatrix(stage.getCamera().combined);
        radius = radiusA;
        batch.begin();
        circle.setUniform2fv("resolution", resolution , 0, 2);
        circle.setUniformf("radius", radius);
        batch.end();
        batch.setShader(circle);
        batch.begin();
        bgnd.draw(batch);
        batch.end();
        stage.getCamera().position.set(tmp);
        stage.getCamera().update();

        batch.begin();
        radius = radiusB;
        circle.setUniform2fv("resolution", resolution , 0, 2);
        circle.setUniformf("radius", radius);
        batch.end();
        stage.act();
        stage.draw();
    }

    @Override
    public void show(){
        batch.setShader(null); //Destroy shader from previous screen
        viewport = new ExtendViewport(Constants.UIViewportWidth,Constants.UIViewportHeight);
        stage = new Stage(viewport,batch);
        skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
//        buildUI();
        scroll = stage.getWidth()*0.5f;
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width,int height){
        resolution[0] = width;
        resolution[1] = height;
        viewport.update(width,height);
    }

    private enum States{
        DELAY,TRANSITION1, NORMAL
    }
}
