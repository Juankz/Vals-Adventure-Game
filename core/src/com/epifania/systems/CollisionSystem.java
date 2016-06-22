package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.epifania.components.*;

public class CollisionSystem extends EntitySystem {

	private static final String tag = "Collision System";
	private static final String NONE = "NONE";
	public boolean action;
	public Button actionButton;
	public Button lockedButton;
	private Vector3 tmp = new Vector3();
	
	private static final String TAG = "CollisionSystem";
	private ComponentMapper<BoundsComponent> bm;
	private ComponentMapper<StateComponent> sm;
	private ComponentMapper<TransformComponent> tm;
	
	private ImmutableArray<Entity> vals;
	private ImmutableArray<Entity> coins;
	private ImmutableArray<Entity> switches;
	private ImmutableArray<Entity> checkpoints;
	private ImmutableArray<Entity> deathZones;
	private ImmutableArray<Entity> collectables;
	private ImmutableArray<Entity> actionables;
	private ImmutableArray<Entity> ladders;
	private ImmutableArray<Entity> packs;

	public interface CollisionListener {
		void pickCoin ();
		void die();
		void checkpointReached(int number);
		void removeEntity(Entity entity);
		void pickObject(TextureRegion region,String key);
		void usedObject(String key);
		void pickPack(String content, int amount);
	}
	
	private CollisionListener listener;
	private Engine engine; 
	
	public CollisionSystem(CollisionListener listener){
		this.listener = listener;
		bm = ComponentMapper.getFor(BoundsComponent.class);
		sm = ComponentMapper.getFor(StateComponent.class);
		tm = ComponentMapper.getFor(TransformComponent.class);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		this.engine = engine;
		vals = this.engine.getEntitiesFor(Family.all(Val_Component.class,TransformComponent.class,BoundsComponent.class,StateComponent.class).get());
		coins = this.engine.getEntitiesFor(Family.all(CoinComponent.class,TransformComponent.class,BoundsComponent.class).get());
		switches = this.engine.getEntitiesFor(Family.all(SwitchComponent.class,AnimatedTileComponent.class,BoundsComponent.class).get());
		checkpoints = this.engine.getEntitiesFor(Family.all(CheckpointComponent.class,BoundsComponent.class,TransformComponent.class).get());
		deathZones = this.engine.getEntitiesFor(Family.all(DeathZoneComponent.class,BoundsComponent.class).get());
		collectables = this.engine.getEntitiesFor(Family.all(CollectableComponent.class,BoundsComponent.class,TextureComponent.class).get());
		actionables = this.engine.getEntitiesFor(Family.all(ActionableComponent.class,BoundsComponent.class).get());
		ladders = this.engine.getEntitiesFor(Family.all(LadderComponent.class,BoundsComponent.class).get());
		packs = this.engine.getEntitiesFor(Family.all(PackComponent.class,BoundsComponent.class).get());
	}

	@Override
	public void update(float deltaTime) {
		for(Entity val : vals){
			BoundsComponent valBounds = bm.get(val);
			
			for(Entity coin : coins){
				if(coin.flags!=val.flags)continue;
				BoundsComponent coinBounds = bm.get(coin);
				if(coinBounds.bounds.overlaps(valBounds.bounds)){
					engine.removeEntity(coin);
					listener.pickCoin();
				}
			}

			boolean canClimb = false;
			for(Entity ladder : ladders){
				if(ladder.flags!=val.flags)continue;
				BoundsComponent boundsComponent = bm.get(ladder);
				if(boundsComponent.bounds.overlaps(valBounds.bounds)){
					//TODO program ladder behavior
					canClimb=true;
				}
			}
			if(!canClimb) {
				if(engine.getSystem(Val_System.class).climbing){
					engine.getSystem(Val_System.class).climbing=false;
					engine.getSystem(Val_System.class).setState(val,Val_Component.WALKR);
				}
			}
			engine.getSystem(Val_System.class).canClimb = canClimb;

			boolean isOnSwitch = false;
			boolean isOnSwitchLocked = false;

			for(Entity sw : switches){
				if(sw.flags!=val.flags)continue;
				BoundsComponent boundsComponent = bm.get(sw);
				if(boundsComponent.bounds.overlaps(valBounds.bounds)){
					isOnSwitch = true;
					setDialogPosition(boundsComponent.bounds);
					if(action) {
						SwitchSystem switchSystem = engine.getSystem(SwitchSystem.class);
						switchSystem.actionate(sw);
						action = false;
					}
				}
			}
			setActionButtonVisibility(isOnSwitch);

			for(Entity entity : checkpoints){
				if(entity.flags!=val.flags)continue;
				BoundsComponent boundsComponent = bm.get(entity);
				CheckpointComponent checkpointComponent = entity.getComponent(CheckpointComponent.class);
				if(boundsComponent.bounds.overlaps(valBounds.bounds)){
					listener.checkpointReached(checkpointComponent.number);
				}
			}
			for(Entity entity : deathZones){
				if(entity.flags!=val.flags)continue;
				Val_System vs = this.getEngine().getSystem(Val_System.class);
				BoundsComponent boundsComponent = bm.get(entity);
				if(boundsComponent.bounds.overlaps(valBounds.bounds)){
					vs.deathCollision(val);
					listener.die();
				}
			}

			for(Entity entity : collectables){
				if(entity.flags!=val.flags)continue;
				if(!entity.getComponent(CollectableComponent.class).free)continue;
				BoundsComponent cb = bm.get(entity);
				if(valBounds.bounds.overlaps(cb.bounds)){
					isOnSwitch = true;
					setDialogPosition(cb.bounds);
					if(action) {
						listener.pickObject(entity.getComponent(TextureComponent.class).region,entity.getComponent(CollectableComponent.class).key);
						listener.removeEntity(entity);
						val.getComponent(Val_Component.class).objects.add(entity);
						String conversationKey = entity.getComponent(CollectableComponent.class).conversationKey;
						if(conversationKey != null){
							val.getComponent(Val_Component.class).conversationKeys.add(conversationKey);
//							val.getComponent(ConversationComponent.class).conditions.add(conversationKey);
						}
						action = false;
					}
				}
			}

			for(Entity entity : packs){
				if(entity.flags!=val.flags)continue;
				Rectangle otherBounds = bm.get(entity).bounds;
				if(valBounds.bounds.overlaps(otherBounds)){
					isOnSwitch = true;
					setDialogPosition(otherBounds);
					if(action) {
						PackComponent packComponent= entity.getComponent(PackComponent.class);
						listener.pickPack(packComponent.content,packComponent.amount);
						listener.removeEntity(entity);
					}
				}
			}

			//Prevent use two doors in different layer using break to label1
			label1: for(Entity entity : actionables){
				if(entity.flags!=val.flags)continue;
				Rectangle bounds = bm.get(entity).bounds;
				if(valBounds.bounds.overlaps(bounds)){
					if(entity.getComponent(ActionableComponent.class).key.equals(NONE)) {
						isOnSwitch = true;
						setDialogPosition(bounds);
						if (action) {
							entity.getComponent(ActionableComponent.class).actionable.action();
							break label1;
						}
					}else{
						for (Entity object : val.getComponent(Val_Component.class).objects) {
							if (object.getComponent(CollectableComponent.class).key.equals(entity.getComponent(ActionableComponent.class).key)) {
								isOnSwitch = true;
								setDialogPosition(bounds);
								if (action) {
									listener.usedObject(object.getComponent(CollectableComponent.class).key);
									entity.getComponent(ActionableComponent.class).key = NONE;
									entity.getComponent(ActionableComponent.class).actionable.action();
									val.getComponent(Val_Component.class).objects.removeValue(object, true);
									break label1;
								}
							}
						}
						if (!isOnSwitch) {
							isOnSwitchLocked = true;
							setDialogPosition(bounds);
							if(action){
								Gdx.app.debug(tag,"show thoughts");
								engine.getSystem(Val_System.class).showThoughts(val,
										entity.getComponent(ActionableComponent.class).key);
							}
						}
					}
				}
			}

			//Reset button and action
			if(action)action = false;
			setButtonVisibility(actionButton,isOnSwitch);
			setButtonVisibility(lockedButton,isOnSwitchLocked);
		}
	}

	private void setDialogPosition(Rectangle bounds){
		float offsetY = 0.0f;
		Camera camera = engine.getSystem(RenderingSystem.class).getCamera();
		tmp.set(bounds.getX()+bounds.getWidth()*0.5f,bounds.getY()+bounds.getHeight()+offsetY,0);
		camera.project(tmp);
		tmp.y = Gdx.graphics.getHeight() - tmp.y;
		actionButton.getStage().getCamera().unproject(tmp);
		actionButton.setPosition(tmp.x-actionButton.getWidth()*0.5f,tmp.y);
		lockedButton.setPosition(tmp.x - lockedButton.getWidth()*0.5f,tmp.y);
	}

	private void setActionButtonVisibility(boolean isOnSwitch){
		if(!isOnSwitch) {
			if (actionButton.isVisible())
				actionButton.setVisible(false);
		}
		else {
			if(!actionButton.isVisible())
				actionButton.setVisible(true);
		}
//			Gdx.app.debug(getClass().getName(),"actionButton.position = "+actionButton.getX()+","+actionButton.getY());
	}

	private void setButtonVisibility(Button button,boolean isOnSwitch){
		if(!isOnSwitch) {
			if (button.isVisible())
				button.setVisible(false);
		}
		else {
			if(!button.isVisible())
				button.setVisible(true);
		}
	}
}
