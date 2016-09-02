package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import com.epifania.components.BodyComponent;
import com.epifania.components.MovementComponent;
import com.epifania.components.StateComponent;
import com.epifania.components.TransformComponent;
import com.epifania.components.Val_Component;
import com.epifania.utils.ConversationManager;

public class Val_System extends IteratingSystem {
	
	private static final String TAG = Val_System.class.getName();
	
	@SuppressWarnings("unchecked")
	private static final Family family = Family.all(Val_Component.class, TransformComponent.class,MovementComponent.class,StateComponent.class,BodyComponent.class).get();
	
	private ComponentMapper<Val_Component> val_mapper;
	private ComponentMapper<TransformComponent> transform_mapper;
	private ComponentMapper<MovementComponent> mov_mapper;
	private ComponentMapper<StateComponent> state_mapper;
	private ComponentMapper<BodyComponent> bom;
	
	private int directionX;
	private int directionY;
	boolean jump;
    boolean canJump;
	public boolean canClimb = false;
	public boolean climbing = false;
	public boolean canMove = true;

	public Val_System() {
		super(family);
		
		val_mapper = ComponentMapper.getFor(Val_Component.class);
		transform_mapper = ComponentMapper.getFor(TransformComponent.class);
		mov_mapper = ComponentMapper.getFor(MovementComponent.class);
		state_mapper = ComponentMapper.getFor(StateComponent.class);
		bom = ComponentMapper.getFor(BodyComponent.class);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Val_Component val = val_mapper.get(entity);
		MovementComponent movement = mov_mapper.get(entity);
		StateComponent state = state_mapper.get(entity);
		BodyComponent body = bom.get(entity);

		if(state.get()!=Val_Component.HURT &&canMove) {
			if (directionX != 0) {
				if (state.get() != Val_Component.JUMP && state.get() != Val_Component.CLIMB) {
					if (directionX > 0) {
						if(state.get()!=Val_Component.WALKR)state.set(Val_Component.WALKR);
					} else {
						if(state.get()!=Val_Component.WALKL)state.set(Val_Component.WALKL);
					}
				}

			} else {
				if (state.get() != Val_Component.IDLE && state.get() != Val_Component.JUMP && state.get() != Val_Component.CLIMB)
					state.set(Val_Component.IDLE);

				movement.velocity.x = 0;
			}
			if (jump && canJump) {
				state.set(Val_Component.JUMP);
				movement.velocity.y = body.body.getMass() * Val_Component.JUMP_VELOCITY;
				canJump = false;
			} else {
				if (state.get() == Val_Component.JUMP && canJump) {
					state.set(Val_Component.IDLE);
				}
			}
			movement.velocity.x = directionX * Val_Component.MOVE_VELOCITY;
			if(climbing)
				if (directionY != 0) {
					movement.velocity.y = directionY * Val_Component.CLIMB_VELOCITY;
				}else{
					if(directionX==0) {
						state.time-=deltaTime; //pause state.time
					}
					movement.velocity.y=0;
				}
		}else {
//			movement.velocity.x = directionX * Val_Component.MOVE_VELOCITY*0.2f;
		}
		//Update self thoughs
		ConversationManager conversationManager = val.conversationManager;
		if(conversationManager.showingThoughts) {
			if (conversationManager.time > ConversationManager.timer1) {
				conversationManager.hideThoughts();
			} else {
				conversationManager.time += deltaTime;
				conversationManager.updateDialogPosition("VAL");
			}
		}
	}
	
	public void setVelocity(int direction){
		this.directionX = 0;
//		if(direction == 0) this.directionX = 0;
		if(direction > 0) this.directionX = 1;
		else if(direction < 0)this.directionX = -1;
	}

	public void climb(int direction){
		if(direction == 0) this.directionY = 0;
		else if(direction > 0) this.directionY = 1;
		else if(direction < 0)this.directionY = -1;
	}

	public void setJump(boolean b) {
		jump = b;
	}

	public boolean isJumping(){
		return jump;
	}

    public void endJump() {
        jump = false;
        canJump = true;
    }

	public void springCollision(Entity entity){
		if (!family.matches(entity)) return;

		MovementComponent movement = mov_mapper.get(entity);
		StateComponent state = state_mapper.get(entity);
		BodyComponent body = bom.get(entity);

		state.set(Val_Component.JUMP);
		movement.velocity.y = body.body.getMass()*Val_Component.SPRING_VELOCITY;
		movement.traslation.set(0,0.5f);
		jump = false;
		canJump = true;
	}

	public void deathCollision(Entity entity){
		if(!family.matches(entity))return;
		MovementComponent movement = mov_mapper.get(entity);
		StateComponent stateComponent = state_mapper.get(entity);

		stateComponent.set(Val_Component.HURT);
//		movement.traslation.setZero();
//		movement.velocity.setZero();
//		movement.accel.setZero();
//		setVelocity(0);
//
//		getEngine().getSystem(PhysicsSystem.class).getWorld().clearForces();
	}

	public void showThoughts(Entity entity,String key){
		if(!getFamily().matches(entity))return;
		ConversationManager conversationManager = val_mapper.get(entity).conversationManager;
		conversationManager.showThoughts(key);
	}

	public void setMove(Entity val, boolean b){
		if(!family.matches(val))return;
		canMove = b;
		setVelocity(0);
		val.getComponent(StateComponent.class).set(Val_Component.IDLE);
	}

	public void setState(Entity val, int state){
		StateComponent stateComponent = state_mapper.get(val);
		MovementComponent movementComponent = mov_mapper.get(val);
		BodyComponent bodyComponent = bom.get(val);

		switch (state){
			case Val_Component.CLIMB:
				if(stateComponent.get()!=state)stateComponent.set(state);
				climbing=true;
				bodyComponent.body.setGravityScale(0);
				movementComponent.climbing=true;
				break;
			case Val_Component.WALKR:
				bodyComponent.body.setGravityScale(1);
				stateComponent.set(state);
				climbing=false;
				movementComponent.climbing=false;
				break;
			case Val_Component.JUMP:
				bodyComponent.body.setGravityScale(1);
				stateComponent.set(state);
				climbing=false;
				movementComponent.climbing = false;
			default:
				break;
		}
	}
}
