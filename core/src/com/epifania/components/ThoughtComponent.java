package com.epifania.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by juan on 1/6/17.
 * Stores a key to show instant dialog helpers placed on the map
 */
public class ThoughtComponent implements Component {
    public String thoughtID = "DEFAULT";
    public String key="NONE";
    public String target="NONE";
}
