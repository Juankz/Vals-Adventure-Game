package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.epifania.components.SpringComponent;

/**
 * Created by juan on 11/05/16.
 */
public class SpringSystem extends IteratingSystem {

    ComponentMapper<SpringComponent> sm;

    public SpringSystem() {
        super(Family.all(SpringComponent.class).get());
        sm = ComponentMapper.getFor(SpringComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SpringComponent component = sm.get(entity);
        if(component.cell.getTile()!= component.normal){
           component.time += deltaTime;
            if(component.time> SpringComponent.RESTITUTION_TIME){
                component.cell.setTile(component.normal);
                component.time = 0.0f;
            }
        }
    }

    public void expandSpring(Entity entity){
        if (!getFamily().matches(entity)) return;
        SpringComponent component = sm.get(entity);
        component.cell.setTile(component.expanded);
        component.time=0.0f;
    }
}
