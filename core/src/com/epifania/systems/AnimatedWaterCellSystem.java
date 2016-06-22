package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.epifania.components.AnimatedWaterCellComponent;
import com.epifania.components.StateComponent;

/**
 * Created by juan on 27/04/16.
 */
public class AnimatedWaterCellSystem extends IteratingSystem {

    ComponentMapper<AnimatedWaterCellComponent> animMapper ;
    ComponentMapper<StateComponent> sm;
    public AnimatedWaterCellSystem(){
        super(Family.all(AnimatedWaterCellComponent.class, StateComponent.class).get());
        animMapper = ComponentMapper.getFor(AnimatedWaterCellComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
    }
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimatedWaterCellComponent anim = animMapper.get(entity);
        StateComponent stateComponent = sm.get(entity);

        stateComponent.time+= deltaTime;
        if(stateComponent.time> AnimatedWaterCellComponent.frameTime){
            stateComponent.time = 0.0f;
            anim.currentCell.setFlipHorizontally(!anim.currentCell.getFlipHorizontally());
        }
    }
}
