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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.ui.ImageAnimation;
import com.epifania.utils.Assets;
import com.epifania.utils.Constants;
import com.epifania.utils.Settings;
import com.epifania.utils.UI_Utils;

import java.util.Locale;

public class MainMenuScreen extends ScreenAdapter {

	private Stage stage;
	private Skin skin;
	private Viewport viewPort;
	private SpriteBatch batch;

	//List of actors
	private Label title;
	private Image bgnd1;
	private Image bgnd2;
	private ImageAnimation val;
	private Table table2;
	private Button infoButton;
	private Button shareButton;

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
		Gdx.input.setInputProcessor(stage);
	}

	public void buildUI(){
		bgnd1 = new Image(new TextureRegion(Assets.instance.get("user interface/bgnd1.png",Texture.class)));
		bgnd1.setName("bgnd1");
		bgnd1.setPosition(0,10);
		bgnd2 = new Image(new TextureRegion(Assets.instance.get("user interface/main menu.png",Texture.class)));
		bgnd2.setName("bgnd2");

		val = new ImageAnimation(Assets.instance.valAssets.idle);
		val.setWidth(120);
		val.setPosition(375,121.6f);
		val.setName("val");

		float bw = 64;
		float gap = 10;

		title = new Label(bundle.get("game").toUpperCase(),skin,"header");
		title.setColor(Color.BROWN);
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

		table2 = new Table();
		table2.add(playTB).pad(pad).colspan(2);
		table2.row();
		table2.add(settingsTB).pad(pad).colspan(2);
		table2.row();
		table2.add(shareButton).pad(pad).left();
		table2.add(infoButton).pad(pad).right();
		table2.pack();
		table2.setName("Table");
		table2.setPosition(771.6f,136);

		title.setPosition(table2.getX() + table2.getWidth()*0.5f - title.getWidth()*0.5f,
				table2.getY() + table2.getHeight() + 40);

		//Add actors
		stage.addActor(bgnd1);
		stage.addActor(bgnd2);
		stage.addActor(val);
		stage.addActor(title);
		stage.addActor(table2);
		//Arrange buttons
		if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
			for (Actor actor : stage.getActors()) {
				UI_Utils.moveWithMouse(actor);
			}
		}
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
