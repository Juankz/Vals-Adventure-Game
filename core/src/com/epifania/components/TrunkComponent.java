package com.epifania.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by juan on 6/22/16.
 * It's similar to a pack component but this implements a open state and a key
 */
public class TrunkComponent implements Component {
    public static final int LOCKED = 0;
    public static final int OPEN = 1;
    public static final int EMPTY = 2;

    public String content = "";
    public int amount = 0;
    public String key = "NONE";
    public boolean open = false;
}
