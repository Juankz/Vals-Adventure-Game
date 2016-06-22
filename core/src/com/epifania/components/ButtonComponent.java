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
    public static final int RESISTANCE = 2;
    public int number = 0;
    public TiledMapTileLayer.Cell cell=null;
    public TiledMapTile down = null;
    public TiledMapTile up = null;
}
