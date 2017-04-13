package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.epifania.components.AnimatedTileComponent;
import com.epifania.components.BridgeComponent;
import com.epifania.components.SwitchComponent;

/**
 * Created by juan on 12/05/16.
 */
public class SwitchSystem extends IteratingSystem {

    ComponentMapper<SwitchComponent> sm;
    ComponentMapper<AnimatedTileComponent> atm;
    ComponentMapper<BridgeComponent> bm;

    public SwitchSystem(){
        super(Family.all(SwitchComponent.class, AnimatedTileComponent.class).get());
        sm = ComponentMapper.getFor(SwitchComponent.class);
        bm = ComponentMapper.getFor(BridgeComponent.class);
        atm = ComponentMapper.getFor(AnimatedTileComponent.class);
    }
    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    public void activate(Entity entity){
        if(!getFamily().matches(entity)) return;
        SwitchComponent sw = sm.get(entity);
        AnimatedTileComponent animatedTileComponent = atm.get(entity);
        int number = sw.number;
        sw.cell.setTile(animatedTileComponent.animations.get(SwitchComponent.LEFT));
        ImmutableArray<Entity> bridges = this.getEngine().getEntitiesFor(Family.all(BridgeComponent.class).get());
        for(Entity bridge : bridges){
            BridgeComponent bridgeComponent = bm.get(bridge);
            int bridgeNumber = bridgeComponent.number;
            if(bridgeNumber == number && !bridgeComponent.moving){
                BridgeSystem bridgeSystem = getEngine().getSystem(BridgeSystem.class);
                bridgeSystem.moveBy(bridge,bridgeComponent.targets.get(bridgeComponent.targetIndex));
                bridgeSystem.nextTarget(bridgeComponent);
            }
        }

    }
}
