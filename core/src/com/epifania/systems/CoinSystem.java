package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.epifania.components.*;
import com.epifania.utils.Assets;

public class CoinSystem extends IteratingSystem {

	ParticleEffectPool pool;
	
	public CoinSystem(){
		super(Family.all(CoinComponent.class,StateComponent.class,TransformComponent.class, ParticleEffectComponent.class).get());

		ParticleEffect particleEffect = new ParticleEffect();
		TextureAtlas atlas = Assets.instance.get("game_objects/items.atlas",TextureAtlas.class);
		particleEffect.load(Gdx.files.internal("particleEffects/coin_collected.p"), atlas);
		pool = new ParticleEffectPool(particleEffect,5,10);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		ParticleEffectComponent particleEffectComponent = entity.getComponent(ParticleEffectComponent.class);
		if(particleEffectComponent.particleEffect==null)return;

		ParticleEffectPool.PooledEffect effect = particleEffectComponent.particleEffect;

		if (effect.isComplete()) {
			effect.reset();
			effect.free();
			getEngine().removeEntity(entity);
		}
	}

	public void picked(Entity entity){
		if(!getFamily().matches(entity))return;
		ParticleEffectComponent particleEffectComponent = entity.getComponent(ParticleEffectComponent.class);
		TransformComponent transformComponent = entity.getComponent(TransformComponent.class);
		particleEffectComponent.particleEffect = pool.obtain();
		particleEffectComponent.particleEffect.setPosition(transformComponent.pos.x,transformComponent.pos.y);
		entity.remove(BoundsComponent.class);
		entity.remove(TextureComponent.class);
	}

}
