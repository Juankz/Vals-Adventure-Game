package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class MovementComponent implements Component {
	public final Vector2 velocity = new Vector2();
	public final Vector2 accel = new Vector2();
	public final Vector2 traslation = new Vector2();
	public boolean climbing = false;
	public Body bringerBody = null; // If val is moving along an other object
}