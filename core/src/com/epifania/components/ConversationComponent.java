package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.epifania.systems.ConversationSystem;

/**
 * Created by juan on 5/25/16.
 */
public class ConversationComponent implements Component {
    public final Array<String> conditions = new Array<String>();
    public int currentCondition = 0;
    public String character;
}
