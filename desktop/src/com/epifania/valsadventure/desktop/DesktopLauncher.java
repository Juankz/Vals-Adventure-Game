package com.epifania.valsadventure.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.epifania.valsadventure.main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		//TODO Use command line texture packer
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new main(), config);
		config.width = 800;
		config.height = config.width*3/4;
//		config.height = config.width*9/16;
		config.resizable = false;
	}
}
