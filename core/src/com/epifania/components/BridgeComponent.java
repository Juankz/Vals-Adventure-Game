package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by juan on 11/05/16.
 */
public class BridgeComponent implements Component {
    public int number = 0;
    public final Array<Vector2> targets = new Array<Vector2>();
    public float speed = 2.0f;
    public int targetIndex = 0;
    public final Vector2 target = new Vector2();
    public boolean moving = false;
}
