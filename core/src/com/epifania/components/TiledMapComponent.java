package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.IntMap;

public class TiledMapComponent implements Component {
	public final IntMap<TiledMapTile> tiledMaps = new IntMap<TiledMapTile>();
	public TiledMapTileLayer.Cell cell;
}
