package com.epifania.utils;

import com.badlogic.gdx.audio.Sound;

/**
 * Created by juan on 5/29/16.
 */
public class SoundManager {
    public static void playSound(Sound sound){
        sound.play(Settings.instance.sfxVolume);
    }

    public static void stopSound(Sound sound){
        sound.stop();
    }

    public static void pauseSound(Sound sound){
        sound.pause();
    }
}
