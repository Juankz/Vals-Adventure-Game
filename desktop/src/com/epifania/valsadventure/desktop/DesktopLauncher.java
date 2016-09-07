package com.epifania.valsadventure.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.epifania.valsadventure.main;

import java.io.IOException;

public class DesktopLauncher {
	public static void main (String[] arg) throws IOException{
		if(true){
			TexturePacker.Settings settings = new TexturePacker.Settings();
			settings.filterMin = Texture.TextureFilter.MipMapLinearNearest;
			settings.duplicatePadding=true;
			settings.combineSubdirectories=true;
			settings.paddingY = 3;
			TexturePacker.process(settings,"/home/juan/GameProjects/Val's Adventure/assetsRaw/characters","../assets/characters","characters");
			TexturePacker.process(settings,"/home/juan/GameProjects/Val's Adventure/assetsRaw/items","../assets/game_objects","items");
			settings.filterMag = Texture.TextureFilter.Linear;
			TexturePacker.process(settings,"/home/juan/GameProjects/Val's Adventure/assetsRaw/user interface/buttons","../assets/user interface","uiskin");
			settings.filterMag = Texture.TextureFilter.Nearest;
			settings.filterMin = Texture.TextureFilter.Nearest;
			TexturePacker.process(settings,"/home/juan/GameProjects/Val's Adventure/assetsRaw/user interface/nine patches","../assets/user interface","ninePatches");

		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new main(), config);
		config.width = 800;
		config.height = config.width*3/4;
//		config.height = config.width*9/16;
		config.resizable = false;
	}
}
