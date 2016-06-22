package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.TiledMapComponent;

public class TiledMapSystem extends IteratingSystem {

	private ComponentMapper<TiledMapComponent> tm;
	
	public TiledMapSystem(String map_path){
		super(Family.all(TiledMapComponent.class).get());
		tm  = ComponentMapper.getFor(TiledMapComponent.class);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
	}

}
