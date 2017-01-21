package com.epifania.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.ui.AnimatedImage;
import com.epifania.ui.SettingsPanel;
import com.epifania.utils.Assets;
import com.epifania.utils.Constants;
import com.epifania.utils.SoundManager;
import com.epifania.utils.UI_Utils;

public class MainMenuScreen extends ScreenAdapter {

	private Stage stage;
	private Skin skin;
	private Viewport viewPort;
	private SpriteBatch batch;

	//List of actors
	private Label title;
	private Image bgnd1;
	private AnimatedImage val;
	private Table table2;
	private Button infoButton;
	private Button shareButton;
	private SettingsPanel settingsPanel;
	private boolean debug = false;

	private I18NBundle bundle;

	private float d = 1/60f;

	public MainMenuScreen(SpriteBatch batch){
		this.batch = batch;
		TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
		parameter.wrapU = Texture.TextureWrap.Repeat;
		parameter.wrapV = Texture.TextureWrap.ClampToEdge;
		parameter.genMipMaps = true;
		parameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
		Assets.instance.load("user interface/bgnd1.png",Texture.class,parameter);
		Assets.instance.load("user interface/main menu.png",Texture.class,parameter);
		Assets.instance.load("sounds/Farm Frolics.ogg",Music.class);
		Assets.instance.finishLoading();
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.BACK)){
			Gdx.app.exit();
		}
	}
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width,height);
	}

	@Override
	public void show() {
		float minW = Constants.UIViewportWidth;
		float minH = Constants.UIViewportHeight;
//		float minH = minW*2f/3f;
		viewPort = new ExtendViewport(minW,minH);
//		viewPort = new FillViewport(minW,minH);
		stage= new Stage(viewPort,batch);
		skin = Assets.instance.get("user interface/uiskin.json");
		bundle = Assets.instance.get("i18n/strings_ui");
		buildUI();
		SoundManager.playMusic("sounds/Farm Frolics.ogg",true);
		Gdx.input.setInputProcessor(stage);
	}

	public void buildUI(){

		//Settings Panel
		settingsPanel = new SettingsPanel(skin,bundle);
		settingsPanel.setPosition(0,0);
		settingsPanel.setFillParent(true);

		bgnd1 = new Image(new TextureRegion(Assets.instance.get("user interface/bgnd1.png",Texture.class)));
		bgnd1.setName("bgnd1");
		bgnd1.setPosition(0,0);

		val = new AnimatedImage(Assets.instance.valAssets.idle);
		val.setWidth(75);
		val.setPosition(540f,113.6f);
		val.setName("val");

		float bw = 64;
		float gap = 10;

		title = new Label(bundle.get("game").toUpperCase(),skin,"h3title");
		title.setColor(new Color(0.54f, 0.27f, 0.07f, 1));
		title.setName("title");

		infoButton = new Button(skin,"info");
		infoButton.setName("infoButton");
		infoButton.addListener(UI_Utils.clickSound());

		shareButton = new Button(skin,"share");
		shareButton.setPosition(30,30+gap+bw);
		shareButton.setName("shareButton");
		shareButton.addListener(UI_Utils.clickSound());

		//Table

		float pad = 20;
		//Second Style
//		TextButton playTB = UI_Utils.genericTextButton("PLAY",skin,"longBrown",getGameScreen(0));
		TextButton playTB = UI_Utils.genericTextButton(bundle.get("play").toUpperCase(),skin,"longBrown",new LevelSelectionScreen(batch,viewPort));
		TextButton settingsTB = UI_Utils.genericTextButton(bundle.get("settings").toUpperCase(),skin,"longBrown");
		settingsTB.addListener(new ClickListener(){
			public void clicked (InputEvent event, float x, float y) {
				settingsPanel.show();
			}
		});
		TextButton creditsButton = UI_Utils.genericTextButton(bundle.get("credits").toUpperCase(),skin,"longBrown");

		table2 = new Table();
		table2.add(playTB).pad(pad);
		table2.row();
		table2.add(settingsTB).pad(pad);
		table2.row();
		table2.add(creditsButton).pad(pad);
		table2.pack();
		table2.setName("Table");
		table2.setPosition(780,175);

		title.setPosition(stage.getWidth()*0.5f - title.getWidth()*0.5f,
				stage.getHeight() - title.getHeight() - 40);

		//Add actors
		stage.addActor(bgnd1);
		stage.addActor(val);
		stage.addActor(title);
		stage.addActor(table2);
		stage.addActor(settingsPanel);
		stage.setDebugAll(debug);
		//Arrange buttons
//		if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
//			for (Actor actor : stage.getActors()) {
//				UI_Utils.moveWithMouse(actor);
//			}
//		}
	}

	@Override
	public void hide() {
		Assets.instance.unload("user interface/bgnd1.png");
		Assets.instance.unload("user interface/main menu.png");
	}

	@Override
	public void pause() {
	}

}
