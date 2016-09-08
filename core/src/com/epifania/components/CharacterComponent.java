package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

/**
 * Created by juan on 5/28/16.
 */
public class CharacterComponent implements Component{
    //Animation keys
    public static final int IDLE = 0;
    public static final int TALKING = 1;
    public static final int SAD = 2;
    public static final int HAPPY = 3;
    public static final int LEFT = 4;
    public static final int RIGHT = 5;

    public final Array<String> conversationIDs = new Array<String>();
    public int current = 0;
    public Character character;
    public States state = States.WAITING_IN;

    public static enum Character{
        GOMH, PINKY, MOM, BLUE, BONNY
    }

    public enum States{
        WAITING_IN,CONVERSATING,WATING_OUT,DO_NOTHING
    }
}
