package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * Created by juan on 12/05/16.
 */
public class SwitchComponent implements Component {
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int CENTER = 2;
    public int number = 0;
    public TiledMapTileLayer.Cell cell=null;
}
