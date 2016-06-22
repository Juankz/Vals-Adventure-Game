package com.epifania.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by juan on 5/26/16.
 */
public class ActionableComponent implements Component{
    public String key = "NONE";
    public CollectableComponent target = null;
    public Actionable actionable = null;

    public interface Actionable{
        void action();
    }
}
