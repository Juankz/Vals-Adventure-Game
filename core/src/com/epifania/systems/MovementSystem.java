package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import com.epifania.components.BodyComponent;
import com.epifania.components.MovementComponent;

public class MovementSystem extends IteratingSystem {
	
	private static final String tag = "MovementSystem";

	private Vector2 tmp = new Vector2();

	private ComponentMapper<MovementComponent> mm;
	private ComponentMapper<BodyComponent> bm;
	
	public MovementSystem() {
		super(Family.all(MovementComponent.class,BodyComponent.class).get());
		
		mm = ComponentMapper.getFor(MovementComponent.class);
		bm = ComponentMapper.getFor(BodyComponent.class);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		MovementComponent mov = mm.get(entity);
		BodyComponent body = bm.get(entity);
		
		tmp.set(body.body.getLinearVelocity());
		float velChangeX = mov.velocity.x - tmp.x;
		float velChangeY = mov.velocity.y - tmp.y;
		float impulseX = body.body.getMass()*velChangeX;
		float impulseY = body.body.getMass();

		if(!mov.climbing) {
			impulseY*=mov.velocity.y;
		}else{
			impulseY *= velChangeY;
		}
		tmp.set(impulseX, impulseY);
		body.body.applyLinearImpulse(tmp, body.body.getWorldCenter(), true);
		mov.velocity.setZero();
		body.body.setTransform(body.body.getPosition().add(mov.traslation),0);
		mov.traslation.setZero();
		body.body.applyForceToCenter(mov.accel,false);
	}

}
