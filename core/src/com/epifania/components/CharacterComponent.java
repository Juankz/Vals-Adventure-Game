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

    public final Array<String> conversationIDs = new Array<String>();
    public int current = 0;
}
