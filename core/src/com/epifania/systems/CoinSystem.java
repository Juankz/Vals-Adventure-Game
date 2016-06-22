package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.BoundsComponent;
import com.epifania.components.CoinComponent;
import com.epifania.components.StateComponent;
import com.epifania.components.TransformComponent;

public class CoinSystem extends IteratingSystem {
	
	public CoinSystem(){
		super(Family.all(CoinComponent.class,StateComponent.class,TransformComponent.class,BoundsComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {

	}

}
