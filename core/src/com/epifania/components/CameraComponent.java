package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

public class CameraComponent implements Component {

	public static final float DISTANCE = 2f;
	public OrthographicCamera camera;
	public final Rectangle bounds = new Rectangle();
	public Entity target;
	
}
