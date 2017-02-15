package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.epifania.components.BodyComponent;
import com.epifania.components.PlatformComponent;
import com.epifania.utils.Constants;

/**
 * Created by juan on 6/24/16.
 */
public class PlatformSystem extends IteratingSystem {

    private ComponentMapper<PlatformComponent> platformMapper;
    private ComponentMapper<BodyComponent> bodyMapper;

    public PlatformSystem(){
        super(Family.all(PlatformComponent.class, BodyComponent.class).get());
        platformMapper = ComponentMapper.getFor(PlatformComponent.class);
        bodyMapper = ComponentMapper.getFor(BodyComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlatformComponent platformComponent = platformMapper.get(entity);
        BodyComponent bodyComponent = bodyMapper.get(entity);

        if(platformComponent.breaking){
            if(platformComponent.time<=0){
                bodyComponent.body.setType(BodyDef.BodyType.DynamicBody);
                for(Fixture fixture : bodyComponent.body.getFixtureList()){
                    Filter filter = new Filter();
                    filter.maskBits = Constants.BOUNDS;
                    filter.categoryBits = fixture.getFilterData().categoryBits;
                    filter.groupIndex = 0;
                    fixture.setFilterData(filter);
                }
            }else {
                platformComponent.time-=deltaTime;
            }
        }
    }

    public void characterCollision(Entity entity){
        if(!getFamily().matches(entity))return;
        PlatformComponent platformComponent = platformMapper.get(entity);
        if(platformComponent.breakable){
            platformComponent.breaking = true;
        }
    }

    public void resetPlatforms(){
        for(Entity entity : getEngine().getEntitiesFor(getFamily())){
            PlatformComponent platformComponent = entity.getComponent(PlatformComponent.class);
            BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

            platformComponent.breaking = false;
            platformComponent.time = platformComponent.breakingTime;

            bodyComponent.body.setType(BodyDef.BodyType.StaticBody);
            bodyComponent.body.setTransform(platformComponent.originalPosition,0);
            int flag = entity.flags;
            for(Fixture fixture : bodyComponent.body.getFixtureList()){
                Filter filter = new Filter();
                filter.maskBits = Constants.layerMaskBits[flag];
                filter.categoryBits = Constants.layerCategoryBits[flag];
                filter.groupIndex = Constants.groupsIndexes[flag];
                fixture.setFilterData(filter);
            }
        }
    }
}
