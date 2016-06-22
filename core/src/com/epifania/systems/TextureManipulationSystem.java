package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.epifania.components.TextureComponent;

/**
 * Created by juan on 6/14/16.
 */
public class TextureManipulationSystem extends IteratingSystem {

    ComponentMapper<TextureComponent> textureMapper;

    public TextureManipulationSystem(){
        super(Family.all(TextureComponent.class).get());
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TextureComponent textureComponent = textureMapper.get(entity);
        textureComponent.alpha = MathUtils.lerp(textureComponent.alpha,textureComponent.tmp,0.1f);
    }

    public void decreaseAlpha(Entity entity, float value){
        if(!getFamily().matches(entity))return;
        TextureComponent textureComponent = textureMapper.get(entity);
        textureComponent.tmp = value;
    }
}
