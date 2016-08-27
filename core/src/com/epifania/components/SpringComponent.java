package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * Created by juan on 27/04/16.
 */
public class SpringComponent implements Component {
    public static final float RESISTANCE = 1;
    public static final float RESTITUTION_TIME = 0.2f;
    public TiledMapTileLayer.Cell cell = null;
    public TiledMapTile normal = null;
    public TiledMapTile expanded = null;
    public float time = 0.0f;
}
