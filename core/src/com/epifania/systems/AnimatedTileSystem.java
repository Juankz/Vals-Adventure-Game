package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.epifania.components.AnimatedTileComponent;

/**
 * Created by juan on 10/05/16.
 */
public class AnimatedTileSystem extends IteratingSystem {
    public AnimatedTileSystem() {
        super(Family.all(AnimatedTileComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }
}
