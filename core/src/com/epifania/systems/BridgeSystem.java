package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
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
    private final Vector2 tmp = new Vector2();
    private Array<Body> processedBodies = new Array<Body>();

    public BridgeSystem(){
        super(Family.all(BridgeComponent.class, TransformComponent.class, BodyComponent.class).get());
        tm = ComponentMapper.getFor(TransformComponent.class);
        bm = ComponentMapper.getFor(BridgeComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
        bm2 = ComponentMapper.getFor(BodyComponent.class);
    }

    @Override
    public void update(float delta){
        super.update(delta);
        processedBodies.clear(); //remove all the bodies to restart the process in the next cycle
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BridgeComponent bridgeComponent = bm.get(entity);
        BodyComponent bodyComponent = bm2.get(entity);

        /*As the same body is used by different entities of the same bridge,
         only process one entity with the respective body
          */
        if(processedBodies.contains(bodyComponent.body,true))
            return;

        processedBodies.add(bodyComponent.body);
        //Slow down the bridge when is near the target (1 unit)
        if(bridgeComponent.target.epsilonEquals(bodyComponent.body.getTransform().getPosition(),1f)){
            float vX = bodyComponent.body.getLinearVelocity().x;
            float vY = bodyComponent.body.getLinearVelocity().y;
            vX = MathUtils.lerp(vX,0,0.025f);
            vY = MathUtils.lerp(vY,0,0.025f);
            bodyComponent.body.setLinearVelocity(vX,vY);
        }
        //If target reached, stop
        if(bridgeComponent.target.epsilonEquals(bodyComponent.body.getTransform().getPosition(),0.05f)){
            bodyComponent.body.setLinearVelocity(0,0);
            bodyComponent.body.getTransform().setPosition(bridgeComponent.target);
            bridgeComponent.moving=false;

            if(bridgeComponent.continuous){
                nextTarget(bridgeComponent);
                moveBy(entity,bridgeComponent.targets.get(bridgeComponent.targetIndex));
                bridgeComponent.moving=true;
            }
        }
    }

    public void nextTarget(BridgeComponent bridgeComponent){
        //TODO add bridge sound and stop
        if(bridgeComponent.targetIndex +1<bridgeComponent.targets.size){
            bridgeComponent.targetIndex++;
        }else{
            bridgeComponent.targetIndex =0;
        }
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

        tmp.set(bridgeComponent.target);
        tmp.sub(bodyComponent.body.getTransform().getPosition());

        bodyComponent.body.setLinearVelocity(vX*bridgeComponent.speed,vY*bridgeComponent.speed);
        bridgeComponent.target.set(x,y);
        bridgeComponent.target.add(tmp);
        bridgeComponent.target.add(bodyComponent.body.getTransform().getPosition());
        bridgeComponent.moving=true;
    }

    public void start(){
        for(Entity entity : getEngine().getEntitiesFor(getFamily())){
            BridgeComponent bridgeComponent = bm.get(entity);
            if(bridgeComponent.continuous && bridgeComponent.moving){
                moveBy(entity,bridgeComponent.targets.get(bridgeComponent.targetIndex));
                bridgeComponent.moving=true;
            }
        }
    }


}
