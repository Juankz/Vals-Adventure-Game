package com.epifania.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Application;
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

	private InputController inputController;

	public InputHandler(Engine engine,InputController controller) {
		this.engine = engine;
		this.inputController = controller;
		camera = engine.getSystem(RenderingSystem.class).getCamera();
		val=engine.getEntitiesFor(Family.all(Val_Component.class).get()).first();
	}

	public boolean keyDown (int keycode) {
		if(!active || Gdx.app.getType()!= Application.ApplicationType.Desktop) return false;
		if (keycode == Keys.RIGHT){
			inputController.right();
			rightKeyPressed = true;
		}else if (keycode ==Keys.LEFT){
			inputController.left();
			leftKeyPressed = true;
		}else if(keycode ==Keys.SPACE || keycode == Keys.UP){
			inputController.up();
			waiting4touchUp = true;
		}else if(keycode == Keys.DOWN){
			inputController.down();
		}
		return true;
	}

	public boolean keyUp (int keycode) {
		if(!active || Gdx.app.getType()!= Application.ApplicationType.Desktop) return false;
		if (keycode == Keys.RIGHT){
			inputController.stopHorizontal();
			rightKeyPressed = false;
		}else if (keycode ==Keys.LEFT){
			inputController.stopHorizontal();
			leftKeyPressed = false;
		}else if(keycode ==Keys.SPACE){
			waiting4touchUp = false;
		}
		else if(keycode == Keys.DOWN || keycode== Keys.UP){
			inputController.stopVertical();
		}
		if(!(rightKeyPressed|leftKeyPressed)){
			engine.getSystem(Val_System.class).setVelocity(0);
		}
		return false;
	}

	public boolean keyTyped (char character) {
		if(Gdx.app.getType()!= Application.ApplicationType.Desktop) return false;
		if(character == 'a' || character == 'A'){
			engine.getSystem(CollisionSystem.class).action=true;
			return true;
		}
		return false;
	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button){
		if(!active || (Gdx.app.getType()!= Application.ApplicationType.Android && Gdx.app.getType()!= Application.ApplicationType.iOS)) return false;
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
		if(!active || (Gdx.app.getType()!= Application.ApplicationType.Android && Gdx.app.getType()!= Application.ApplicationType.iOS)) return false;
		if(hMovPointer==pointer){
			hMovPointer = NOT_SET;
			inputController.stopHorizontal();
		}else if(vMovPointer == pointer){
			vMovPointer = NOT_SET;
			engine.getSystem(Val_System.class).climb(0);
			engine.getSystem(Val_System.class).setJump(false);
			waiting4touchUp = false;
		}
		return true;
	}

	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if(!active || (Gdx.app.getType()!= Application.ApplicationType.Android && Gdx.app.getType()!= Application.ApplicationType.iOS)) return false;
		//X Axis
		if(hMovPointer==pointer){
			screenCoords.set(screenX, screenY, 0);
			newCoordsH.set(screenCoords);
			float distance = newCoordsH.x - (previousCoordsH.x);
			if(Math.abs(distance)>DRAG_TOLERANCE){
				int d = distance<0? -1:1;
				if(d<0)
					inputController.left();
				else
					inputController.right();
			}
		}else if(vMovPointer==pointer){ //Y Axis
			screenCoords.set(screenX, screenY, 0);
			newCoordsV.set(screenCoords);
			float distance = newCoordsV.y - (previousCoordsV.y);
			distance*=-1; //Because of screen coords
			if(Math.abs(distance)>DRAG_TOLERANCE){
				if (distance>0)
					inputController.up();
				else
					inputController.down();
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
