package com.epifania.screens;

import com.badlogic.gdx.Screen;
import com.epifania.valsadventure.main;

public class ScreenManager {
	
	private static ScreenManager instance;
	private main game;
	
	private ScreenManager(){
		super();
	}

	public static ScreenManager getInstance(){
		if(instance==null){
			instance = new ScreenManager();
		}
		return instance;
	}
	
	public void initialize(main game){
		this.game = game;
	}
	
	public void showScreen(ScreenEnum screenEnum,Object... params){
		game.setScreen(screenEnum.getScreen(game.batch,params));
	}

	public void setScreen(Screen screen){
		game.setScreen(screen);
	}
}
