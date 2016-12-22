package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent implements Component {
	public TextureRegion region = null;
	public float alpha = 1;
	public float tmp = 1;
	public boolean transition = false;
	public float elapsed=0;
	public static float DURATION = 0.4f;
	public CallBack callBack = null;

	public interface CallBack{
		void callback();
	}
}
