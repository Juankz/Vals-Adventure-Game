package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.BoundsComponent;
import com.epifania.components.TransformComponent;

public class BoundsSystem extends IteratingSystem {
	
	ComponentMapper<TransformComponent> tm;
	ComponentMapper<BoundsComponent> bm;

	public BoundsSystem() {
		super(Family.all(TransformComponent.class,BoundsComponent.class).get());
		tm = ComponentMapper.getFor(TransformComponent.class);
		bm = ComponentMapper.getFor(BoundsComponent.class);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		BoundsComponent bounds = bm.get(entity);
		TransformComponent transform = tm.get(entity);
		float hw = bounds.bounds.width*transform.origin.x;
		float hh = bounds.bounds.height*transform.origin.y;
		bounds.bounds.x = transform.pos.x +bounds.posOffset.x - hw;
		bounds.bounds.y = transform.pos.y +bounds.posOffset.y - hh;
	}

}
