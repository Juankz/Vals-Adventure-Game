package com.epifania.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

import com.epifania.components.BoundsComponent;
import com.epifania.components.PolygonComponent;
import com.epifania.components.TransformComponent;

public class DebugSystem extends IteratingSystem {

    private static final String tag = "DebugingSystem";
    private ShapeRenderer shapeRenderer;

    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera camera;

    private ComponentMapper<BoundsComponent> bm;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<PolygonComponent> pm;

    public DebugSystem(OrthographicCamera camera){
        super(Family.one(BoundsComponent.class,TransformComponent.class,PolygonComponent.class).get());

        bm = ComponentMapper.getFor(BoundsComponent.class);
        tm = ComponentMapper.getFor(TransformComponent.class);
        pm = ComponentMapper.getFor(PolygonComponent.class);
        renderQueue = new Array<Entity>();

//        comparator = new Comparator<Entity>() {
//            @Override
//            public int compare(Entity entityA, Entity entityB) {
//                return (int)Math.signum(tm.get(entityB).pos.z -
//                        tm.get(entityA).pos.z);
//            }
//        };

        shapeRenderer = new ShapeRenderer();
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
//        renderQueue.sort(comparator);
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        for (Entity entity : renderQueue) {
            BoundsComponent bc = bm.get(entity);
            PolygonComponent pc = pm.get(entity);
            if(pc!=null){
                shapeRenderer.polygon(pc.bounds.getTransformedVertices());
            }else if(bc!=null){
                shapeRenderer.rect(bc.bounds.x, bc.bounds.y, bc.bounds.width, bc.bounds.height);
            }
        }
        shapeRenderer.end();
        renderQueue.clear();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BoundsComponent bc = bm.get(entity);
//        TransformComponent tc = tm.get(entity);
        PolygonComponent pc = pm.get(entity);
//        if(tc!= null & (bc != null | pc!=null))
        if((bc != null | pc!=null))
            renderQueue.add(entity);
    }
}
