package com.epifania.systems;

import java.util.ArrayList;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.Array;
import com.epifania.components.*;
import com.epifania.utils.Constants;

public class PhysicsSystem extends IteratingSystem{

	private static final String tag = "PhysicsSystem";
	private ArrayList<Entity> entities;
	private Array<Body> bodies;
	private Array<Body> bodies4destroy;
	private World world;
	private Engine engine;
	private ImmutableArray<Entity> vals;
	private ImmutableArray<Entity> springs;
	private float accumulator = 0;
	public static float TIME_STEP = 1/45f;
	public static int VELOCITY_ITERATIONS = 6;
	public static int POSITION_ITERATIONS = 2;
	
	private ComponentMapper<TransformComponent> tm;
	private ComponentMapper<BodyComponent> bom;

	public PhysicsSystem(World world) {
		super(Family.all(BodyComponent.class,TransformComponent.class).get());
		this.world = world;
//		this.world.setContactListener(this);
		bodies = new Array<Body>();
		bodies4destroy = new Array<Body>();
		entities = new ArrayList<Entity>();
		tm = ComponentMapper.getFor(TransformComponent.class);
		bom = ComponentMapper.getFor(BodyComponent.class);
	}

	public void setContactListener(ContactListener contactListener){
		this.world.setContactListener(contactListener);
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
		vals = this.engine.getEntitiesFor(Family.all(Val_Component.class,TransformComponent.class, BoundsComponent.class, StateComponent.class).get());
		springs = this.engine.getEntitiesFor(Family.all(SpringComponent.class,BodyComponent.class).get());
	}

	@Override
	public void update (float deltaTime) {
		super.update(deltaTime);
		// fixed time step
	    // max frame time to avoid spiral of death (on slow devices)
	    float frameTime = Math.min(deltaTime, 0.25f);
	    accumulator += frameTime;
	    while (accumulator >= TIME_STEP) {
	        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	        accumulator -= TIME_STEP;
	    }

		for(Entity e : entities){
			TransformComponent transform = tm.get(e);
			BodyComponent body = bom.get(e);

			transform.rotation = MathUtils.radiansToDegrees * body.body.getAngle();
			transform.pos.x = body.body.getTransform().getPosition().x + body.offsetPosition.x;
			transform.pos.y = body.body.getTransform().getPosition().y + body.offsetPosition.y;
		}
		entities.clear();

		//Destroy bodies in a clean way
		for(Body body : bodies4destroy){
			world.destroyBody(body);
		}
		bodies4destroy.clear();
	}

	public void setActiveObjects(){
		Entity val = engine.getEntitiesFor(Family.all(Val_Component.class).get()).first();
		int valFlag = val.flags;
		BodyComponent valBodyComponent = bom.get(val);
		for(Fixture fixture :valBodyComponent.body.getFixtureList()){
			Filter filter = new Filter();
			filter.groupIndex = Constants.groupsIndexes[valFlag];
			filter.categoryBits = fixture.getFilterData().categoryBits;
			filter.maskBits = fixture.getFilterData().maskBits;
			fixture.setFilterData(filter);
			Gdx.app.debug(tag,"val group index = "+fixture.getFilterData().groupIndex);
		}
	}

	public void destroyBody(Body body){
		bodies4destroy.add(body);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		entities.add(entity);
	}
	
	public World getWorld(){
		return this.world;
	}
}
