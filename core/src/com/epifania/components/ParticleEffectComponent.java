package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;

/**
 * Created by juan on 1/21/17.
 */
public class ParticleEffectComponent implements Component{
    public ParticleEffectPool.PooledEffect particleEffect = null;
}
