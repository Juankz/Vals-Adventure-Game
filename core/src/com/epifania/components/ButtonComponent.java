package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * Created by juan on 6/10/16.
 */
public class ButtonComponent implements Component{
    public static final int UP= 0;
    public static final int DOWN= 1;
    public static final int RESISTANCE = 0;
    public boolean keepDown = true; //If false, it returns to state UP
    public int number = 0;
    public int state = 0;
    public Color color;
    public Target target = Target.BRIDGE;

    public enum Target{
        JOINT,BRIDGE
    }

    public enum Color{
        RED,YELLOW,GREEN,BLUE
    }
}
