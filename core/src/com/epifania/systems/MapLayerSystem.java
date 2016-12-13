package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.epifania.components.MapComponent;
import com.epifania.components.Val_Component;

/**
 * Created by juan on 12/12/16.
 */
public class MapLayerSystem extends IteratingSystem {

    public MapLayerSystem(){
        super(Family.all(MapComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        boolean transition = entity.getComponent(MapComponent.class).transition;
        int alpha = entity.getComponent(MapComponent.class).alpha;
        TiledMap map = entity.getComponent(MapComponent.class).map;

        if(transition) {
            float progress = Math.min(1,entity.getComponent(MapComponent.class).elapsedt/entity.getComponent(MapComponent.class).DURATION);
            entity.getComponent(MapComponent.class).elapsedt+=deltaTime;
            float a = map.getLayers().get("Items").getOpacity();
            a = Interpolation.exp10In.apply(a,alpha,progress);
            map.getLayers().get("Items").setOpacity(a);
            map.getLayers().get("Builds Front").setOpacity(a);
            map.getLayers().get("Builds").setOpacity(a);
            if(progress==1){
                entity.getComponent(MapComponent.class).transition=false;
                entity.getComponent(MapComponent.class).elapsedt=0;

                getEngine().getEntitiesFor(Family.all(Val_Component.class).get()).first().flags= alpha==0? 1:0;
                getEngine().getSystem(PhysicsSystem.class).setActiveObjects();
            }
        }
    }

    public void shuffleAlpha(Entity entity){
        if(!getFamily().matches(entity))return;
        entity.getComponent(MapComponent.class).transition = true;
        float alpha = entity.getComponent(MapComponent.class).alpha;
        if(alpha==0) {
            entity.getComponent(MapComponent.class).alpha = 1;
            getEngine().getEntitiesFor(Family.all(Val_Component.class).get()).first().flags = 0;
            getEngine().getSystem(PhysicsSystem.class).setActiveObjects();
        }else {
            entity.getComponent(MapComponent.class).alpha = 0;
        }
    }
}
