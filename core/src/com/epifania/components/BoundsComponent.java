package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BoundsComponent implements Component {
	public Rectangle bounds = new Rectangle();
	public Vector2 posOffset = new Vector2();
}
