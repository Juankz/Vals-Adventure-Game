package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.utils.IntMap;

/**
 * Created by juan on 10/05/16.
 */
public class AnimatedTileComponent implements Component {
    public IntMap<TiledMapTile> animations = new IntMap<TiledMapTile>();
}
