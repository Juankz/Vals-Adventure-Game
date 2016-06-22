package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

/**
 * Created by juan on 27/04/16.
 */
public class AnimatedWaterCellComponent implements Component{
    public Array<TiledMapTileLayer.Cell> animation = new Array<TiledMapTileLayer.Cell> ();
    public TiledMapTileLayer.Cell currentCell = null;
    public static final float frameTime = 0.35f; //seconds
}
