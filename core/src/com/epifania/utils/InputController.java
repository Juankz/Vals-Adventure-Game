package com.epifania.utils;

/**
 * Created by juan on 8/29/16.
 * Abstract the input events to four basic actions
 */
public interface InputController {
    void left();
    void right();
    void stopHorizontal();
    void stopVertical();
    void up();
    void down();
}
