package com.epifania.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

/**
 * Created by juan on 5/29/16.
 */
public class SoundManager {
    public static final Array<Music> musicFiles = new Array<Music>();

    public static void playSound(Sound sound){
        sound.play(Settings.instance.sfxVolume);

    }

    public static long playLoopSound(String path){
        Sound sound = Assets.instance.get(path,Sound.class);
        long id = sound.loop(Settings.instance.sfxVolume);
        sound.setLooping(id,true);
        return id;
    }


    public static void playSound(String path){
        Assets.instance.get(path,Sound.class).play(Settings.instance.sfxVolume);
    }

    public static void stopSound(Sound sound){
        sound.stop();
    }

    public static void stopSound(String path,long id){
        Assets.instance.get(path,Sound.class).stop(id);
    }

    public static void pauseSound(Sound sound){
        sound.pause();
    }

    public static void pauseSound(String path){
        Assets.instance.get(path,Sound.class).pause();
    }

    public static void playMusic(String path,boolean loop){
        Music music = Assets.instance.get(path, Music.class);
        playMusic(music,loop);
    }

    public static void playMusic(Music music, boolean loop){
        music.setLooping(loop);
        music.setVolume(Settings.instance.musicVolume);
        if(!music.isPlaying()) {
            music.play();
            musicFiles.add(music);
        }
    }

    public static void stopMusic(String path){
        Music music = Assets.instance.get(path, Music.class);
        music.stop();
        musicFiles.removeValue(music,true);
    }

    public void updateVolume(){
        Assets.instance.getAll(Music.class,musicFiles);
        for(Music musicFile : musicFiles){
            musicFile.setVolume(Settings.instance.musicVolume);
        }
    }
}
