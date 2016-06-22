package com.epifania.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;

public enum ScreenEnum {

	MAIN_MENU{
		public ScreenAdapter getScreen(Object... params){
			return new MainMenuScreen((SpriteBatch)params[0]);
		}
	};
	
	public abstract ScreenAdapter getScreen(Object... params);
}
