package com.epifania.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.epifania.components.StateComponent;
import com.epifania.components.Val_Component;
import com.epifania.systems.CollisionSystem;
import com.epifania.systems.RenderingSystem;
import com.epifania.systems.Val_System;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class InputHandler extends InputAdapter {
	
	private static final int NOT_SET = -1;
	private static final float DRAG_TOLERANCE = Gdx.graphics.getWidth()*0.01f;
	private static final float DRAG_TOLERANCEY = Gdx.graphics.getWidth()*0.2f;
	private Engine engine;
	private OrthographicCamera camera;
	private boolean leftKeyPressed = false;
	private boolean rightKeyPressed = false;
	private Vector3 screenCoords = new Vector3();
	private Vector3 previousCoordsH = new Vector3();
	private Vector3 newCoordsH = new Vector3();
	private Vector3 previousCoordsV = new Vector3();
	private Vector3 newCoordsV = new Vector3();
	private boolean active = true;
	private boolean waiting4touchUp = false;
	private Entity val;
	
	private int hMovPointer = -1; //-1 equals to not set
	private int vMovPointer = -1;
	public InputHandler(Engine engine) {
		this.engine = engine;
		camera = engine.getSystem(RenderingSystem.class).getCamera();
		val=engine.getEntitiesFor(Family.all(Val_Component.class).get()).first();
	}

	public boolean keyDown (int keycode) {
		if(!active) return false;
		if (keycode == Keys.RIGHT){
			engine.getSystem(Val_System.class).setVelocity(1);
			rightKeyPressed = true;
		}else if (keycode ==Keys.LEFT){
			engine.getSystem(Val_System.class).setVelocity(-1);
			leftKeyPressed = true;
		}else if(keycode ==Keys.SPACE){
			engine.getSystem(Val_System.class).setJump(true);
			waiting4touchUp = true;
		}
		return true;
	}

	public boolean keyUp (int keycode) {
		if(!active) return false;
		if (keycode == Keys.RIGHT){
			rightKeyPressed = false;
		}else if (keycode ==Keys.LEFT){
			leftKeyPressed = false;
		}else if(keycode ==Keys.SPACE){
			waiting4touchUp = false;
		}
		if(!(rightKeyPressed|leftKeyPressed)){
			engine.getSystem(Val_System.class).setVelocity(0);
		}
		return false;
	}

	public boolean keyTyped (char character) {
		if(character == 'a' || character == 'A'){
			engine.getSystem(CollisionSystem.class).action=true;
			return true;
		}
		return false;
	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button){
		if(!active) return false;
		screenCoords.set(screenX, screenY, 0);

		if(screenX < Gdx.graphics.getWidth()/2f && hMovPointer==NOT_SET){
			hMovPointer = pointer;
			previousCoordsH.set(screenCoords);
		}else{
			vMovPointer = pointer;
			previousCoordsV.set(screenCoords);
		}
			return true;
	}

	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if(!active) return false;
		if(hMovPointer==pointer){
			hMovPointer = NOT_SET;
			engine.getSystem(Val_System.class).setVelocity(0);
		}else if(vMovPointer == pointer){
			vMovPointer = NOT_SET;
			engine.getSystem(Val_System.class).climb(0);
			engine.getSystem(Val_System.class).setJump(false);
			waiting4touchUp = false;
		}
		return true;
	}

	public boolean touchDragged (int screenX, int screenY, int pointer) {
		Val_System val_system = engine.getSystem(Val_System.class);
		if(!active) return false;
		if(hMovPointer==pointer){
			screenCoords.set(screenX, screenY, 0);
			newCoordsH.set(screenCoords);
			float distance = newCoordsH.x - (previousCoordsH.x);
			if(Math.abs(distance)>DRAG_TOLERANCE){
				int d = distance<0? -1:1;
				val_system.setVelocity(d);
			}else{
				val_system.setVelocity(0);
			}
		}else if(vMovPointer==pointer){
			screenCoords.set(screenX, screenY, 0);
			newCoordsV.set(screenCoords);
			float distance = newCoordsV.y - (previousCoordsV.y);
			distance*=-1; //Because of screen coords
			if(val_system.canClimb && !val_system.climbing){
				if(Math.abs(distance)>DRAG_TOLERANCE){
					if(val.getComponent(StateComponent.class).get()!=Val_Component.CLIMB){
						val_system.setState(val,Val_Component.CLIMB);
					}
				}
			}else if (val_system.climbing){
				if(Math.abs(distance)>DRAG_TOLERANCE){
					int d = distance<0? -1:1;
					val_system.climb(d);
				}else{
					val_system.climb(0);
				}
			}else if(distance>DRAG_TOLERANCE && !val_system.isJumping() && !waiting4touchUp){
				engine.getSystem(Val_System.class).setJump(true);
				waiting4touchUp = true;
			}
		}
		return true;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}

	public void setActive(boolean b){
		active = b;
		leftKeyPressed = false;
		rightKeyPressed = false;
		screenCoords.setZero();
		newCoordsV.setZero();
		previousCoordsV.setZero();
		newCoordsH.setZero();
		previousCoordsH.setZero();
		hMovPointer = NOT_SET;
		vMovPointer = NOT_SET;
		waiting4touchUp = false;
	}

	public boolean isActive(){
		return active;
	}
}
