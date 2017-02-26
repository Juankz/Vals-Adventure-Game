package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by juan on 6/13/16.
 */
public class PlatformComponent implements Component {
    public static final float BREAKING_TIME_EASY = 2;
    public static final float BREAKING_TIME_MEDIUM = 1.25f;
    public static final float BREAKING_TIME_HARD = 0.75f;
    public boolean breakable = false;
    public boolean breaking = false;
    public boolean broke = false;
    public float breakingTime = BREAKING_TIME_EASY;
    public float time = BREAKING_TIME_EASY;
    public final Vector2 originalPosition = new Vector2();
}
