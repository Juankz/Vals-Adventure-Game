package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.epifania.components.ParallaxComponent;
import com.epifania.components.TextureComponent;
import com.epifania.components.Val_Component;

/**
 * Created by juan on 6/14/16.
 */
public class TextureManipulationSystem extends IteratingSystem {

    ComponentMapper<TextureComponent> textureMapper;

    public interface callBack{
        void callBack();
    }

    public TextureManipulationSystem(){
        super(Family.all(TextureComponent.class).get());
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TextureComponent textureComponent = textureMapper.get(entity);
        if(textureComponent.transition) {
            float progress = Math.min(1, textureComponent.elapsed / TextureComponent.DURATION);
            textureComponent.elapsed += deltaTime;

            textureComponent.alpha = Interpolation.exp10In.apply(textureComponent.alpha,textureComponent.tmp,progress);

            if (progress == 1) {
                textureComponent.transition = false;
                textureComponent.elapsed = 0;
            }
        }
    }

    public void decreaseAlpha(Entity entity, float value){
        if(!getFamily().matches(entity))return;
        TextureComponent textureComponent = textureMapper.get(entity);
        textureComponent.tmp = value;
    }

    public void shuffleAlpha(){
        for(Entity entity : getEngine().getEntitiesFor(Family.all(TextureComponent.class).get())) {
            if (entity.flags != 0) continue; //Only shuffle the front layer
            if (entity.getComponent(Val_Component.class)!=null || entity.getComponent(ParallaxComponent.class)!=null) continue; //Don't process val neither background
            float tmp = textureMapper.get(entity).tmp;
            if (tmp == 0) {
                textureMapper.get(entity).tmp = 1;
            } else {
                textureMapper.get(entity).tmp = 0;
            }
            textureMapper.get(entity).transition = true;
        }
    }
}
