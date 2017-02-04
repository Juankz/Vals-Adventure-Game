package com.epifania.valsadventure;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.epifania.screens.*;
import com.epifania.utils.Assets;
import com.epifania.utils.LevelsData;
import com.epifania.utils.Settings;

public class main extends Game {
	public SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();
		Gdx.app.setLogLevel(Application.LOG_DEBUG); //For debug purposes
		Gdx.input.setCatchBackKey(true);
		Settings.instance.loadSettings();
		LevelsData.getInstance().loadInfo();
		Assets.instance.init(new AssetManager()); //AssetsManagement
		ScreenManager.getInstance().initialize(this);
		ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU,batch);
	}

	@Override
	public void render(){
		Gdx.gl20.glClearColor(0,0,0,1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	@Override
	public void resume(){
		Assets.instance.finishLoading();
		super.resume();
	}

	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
		Assets.instance.dispose();
		Settings.instance.saveSettings();
		LevelsData.getInstance().saveInfo();
	}
}
