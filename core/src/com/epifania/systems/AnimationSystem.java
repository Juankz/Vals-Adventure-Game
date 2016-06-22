package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.AnimationComponent;
import com.epifania.components.StateComponent;
import com.epifania.components.TextureComponent;

public class AnimationSystem extends IteratingSystem {
	
	ComponentMapper<AnimationComponent> am;
	ComponentMapper<StateComponent> sm;
	ComponentMapper<TextureComponent> tm;
	
	public AnimationSystem(){
		super(Family.all(AnimationComponent.class,StateComponent.class,TextureComponent.class).get());
		
		am = ComponentMapper.getFor(AnimationComponent.class);
		sm = ComponentMapper.getFor(StateComponent.class);
		tm = ComponentMapper.getFor(TextureComponent.class);
	}
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		AnimationComponent animation =am.get(entity);
		StateComponent state = sm.get(entity);
		TextureComponent texture = tm.get(entity);
		
		texture.region = animation.animations.get(state.get()).getKeyFrame(state.time);
		state.time+=deltaTime;
	}

}
