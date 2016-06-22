package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.epifania.components.BodyComponent;
import com.epifania.components.BridgeComponent;
import com.epifania.components.MovementComponent;
import com.epifania.components.TransformComponent;

/**
 * Created by juan on 11/05/16.
 */
public class BridgeSystem extends IteratingSystem {

    public ComponentMapper<BridgeComponent> bm;
    public ComponentMapper<TransformComponent> tm;
    public ComponentMapper<MovementComponent> mm;
    public ComponentMapper<BodyComponent> bm2;

    public BridgeSystem(){
        super(Family.all(BridgeComponent.class, TransformComponent.class,MovementComponent.class).get());
        tm = ComponentMapper.getFor(TransformComponent.class);
        bm = ComponentMapper.getFor(BridgeComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
        bm2 = ComponentMapper.getFor(BodyComponent.class);
    }
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BridgeComponent bridgeComponent = bm.get(entity);
        BodyComponent bodyComponent = bm2.get(entity);

        //If body position is not in the target position move
        if(bridgeComponent.target.epsilonEquals(bodyComponent.body.getTransform().getPosition(),0.1f)){
            bodyComponent.body.setLinearVelocity(0,0);
            nextTarget(bridgeComponent);
            bridgeComponent.moving=false;
        }
    }

    private void nextTarget(BridgeComponent bridgeComponent){
        if(bridgeComponent.targetIndex +1<bridgeComponent.targets.size){
            bridgeComponent.targetIndex++;
        }else{
            bridgeComponent.targetIndex =0;
        }

        bridgeComponent.target.set(bridgeComponent.targets.get(bridgeComponent.targetIndex));
    }

    public void moveBy(Entity entity, Vector2 target){
        moveBy(entity,target.x,target.y);
    }

    public void moveBy(Entity entity, float x, float y){
        float vX = 0;
        float vY = 0;
        BodyComponent bodyComponent = bm2.get(entity);
        BridgeComponent bridgeComponent = bm.get(entity);

        if(x!=0)
            vX = x>0? 1:-1;
        if (y!= 0)
            vY = y>0? 1:-1;


        bodyComponent.body.setLinearVelocity(vX*bridgeComponent.speed,vY*bridgeComponent.speed);
        bridgeComponent.target.set(x,y);
        bridgeComponent.target.add(bodyComponent.body.getTransform().getPosition());
        bridgeComponent.moving=true;
    }


}
