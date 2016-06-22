package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class BodyComponent implements Component {
	public Body body;
	public final Vector2 offsetPosition = new Vector2();
}
