package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.epifania.utils.ConversationManager;

public class Val_Component implements Component{

	public static final int IDLE = 1;
	public static final int WALKR = 2;
	public static final int WALKL = 3;
	public static final int JUMP = 4;
	public static final int HURT = 5;
	public static final int CLIMB = 6;

	public static final float JUMP_VELOCITY = 5.25f;
	public static final float CLIMB_VELOCITY = 3;
	public static final float MOVE_VELOCITY = 4;
	public static final float SPRING_VELOCITY = 10;
	public static final float WIDTH = 0.8f;
	public static final float HEIGHT = 1.0f;
	public static final float BODY_HEIGHT = 0.8f;

	public final Array<Entity> objects = new Array<Entity>();
	public final Array<String> conversationKeys = new Array<String>();
	public int numberOfContacts = 0;
	public int invalidContacts = 0;

	public ConversationManager conversationManager;
}
