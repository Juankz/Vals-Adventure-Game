package com.epifania.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by juan on 5/26/16.
 */
public class CollectableComponent implements Component {
    public String key;
    public String conversationKey;
    public boolean free = true;
    public String tag = "NONE";
}
