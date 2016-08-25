package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.badlogic.gdx.Gdx;
import com.epifania.components.CameraComponent;
import com.epifania.components.TransformComponent;

public class CameraSystem extends IteratingSystem {
	
	private ComponentMapper<TransformComponent> tm;
	private ComponentMapper<CameraComponent> cm;
	
	public CameraSystem() {
		super(Family.all(CameraComponent.class).get());
		
		tm = ComponentMapper.getFor(TransformComponent.class);
		cm = ComponentMapper.getFor(CameraComponent.class);
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		CameraComponent cam = cm.get(entity);
		
		if (cam.target == null) {
			return;
		}
		
		TransformComponent target = tm.get(cam.target);
		
		if (target == null) {
			return;
		}
		float distance = cam.camera.position.x - target.pos.x;
		if(Math.abs(distance)>cam.DISTANCEX){
			if(distance>0){
				cam.camera.position.x = cam.DISTANCEX + target.pos.x;
			}else{
				cam.camera.position.x = -cam.DISTANCEX + target.pos.x;
			}
		}
		
		distance = cam.camera.position.y - target.pos.y;
		//Keeps the camera above the player, hence the player has more visibility of the upper part of the map.
		if(distance>CameraComponent.DISTANCEYD){
			cam.camera.position.y = CameraComponent.DISTANCEYD + target.pos.y;
		}else if(distance < -CameraComponent.DISTANCEYU){
			cam.camera.position.y = -CameraComponent.DISTANCEYU + target.pos.y;
		}

		//Keep camera into bounds
		float wv = cam.camera.viewportWidth;
		float wh = cam.camera.viewportHeight;
		if(cam.camera.viewportHeight<cam.camera.viewportWidth) {
			wv = cam.camera.viewportHeight * Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		}else {
			wh = cam.camera.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
		}

		if(cam.camera.position.x - wv*0.5f < 0){
			cam.camera.position.x = wv*0.5f;
		}else if(cam.camera.position.x + wv*0.5f > cam.bounds.width){
			cam.camera.position.x = cam.bounds.width-wv*0.5f;
		}

		if(cam.camera.position.y - wh*0.5f <0){
			cam.camera.position.y = wh*0.5f;
		}else if(cam.camera.position.y + wh*0.5f > cam.bounds.height){
			cam.camera.position.y = cam.bounds.height - wh*0.5f;
		}

		cam.camera.update();
	}
}
