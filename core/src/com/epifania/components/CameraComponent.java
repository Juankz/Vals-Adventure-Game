package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

public class CameraComponent implements Component {

	public static final float DISTANCEX = 2f; //Distance from the center at which the camera will move on the horizontal axis
	public static final float DISTANCEYU = -1.7f; //Distance from the center at which the camera will move up
	public static final float DISTANCEYD = 1.7f; //Distance from the center at which the camera will move down
	public OrthographicCamera camera;
	public final Rectangle bounds = new Rectangle();
	public Entity target;
	
}
