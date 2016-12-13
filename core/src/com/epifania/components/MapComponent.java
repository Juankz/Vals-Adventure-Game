package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;

/**
 * Created by juan on 12/12/16.
 */
public class MapComponent implements Component{
    public static float DURATION = 0.4f;
    public TiledMap map=null;
    public int alpha=1;
    public boolean transition=false;
    public float elapsedt = 0;
}
