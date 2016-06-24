package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.MapTileComponent;

public class TiledMapSystem extends IteratingSystem {

	private ComponentMapper<MapTileComponent> tm;
	
	public TiledMapSystem(){
		super(Family.all(MapTileComponent.class).get());
		tm  = ComponentMapper.getFor(MapTileComponent.class);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
	}

	public void setTile(Entity entity, int tileID){
		if(!getFamily().matches(entity))return;
		MapTileComponent mapTileComponent = tm.get(entity);
		mapTileComponent.cell.setTile(mapTileComponent.tiledMaps.get(tileID));
	}

}
