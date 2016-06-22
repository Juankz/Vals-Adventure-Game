package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsDebugSystem extends IteratingSystem {

    private Box2DDebugRenderer debugRenderer;
    public World world;
    public OrthographicCamera camera;

    public PhysicsDebugSystem(World world,OrthographicCamera camera) {
        super(Family.all().get());
        this.world = world;
        this.debugRenderer = new Box2DDebugRenderer();
        this.camera = camera;
    }

    public PhysicsDebugSystem(Family family, int priority) {
        super(family, priority);
    }

    @Override
    public void update (float deltaTime) {
        super.update(deltaTime);
        debugRenderer.render(world, camera.combined);
    }
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
    }

}