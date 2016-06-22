package com.epifania.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by juan on 6/16/16.
 */
public class LevelsData {
    private static LevelsData ourInstance = new LevelsData();

    public static LevelsData getInstance() {
        return ourInstance;
    }
    private Levels levels;
    private static final String tag = "Levels Data";

    private LevelsData() {
//        createInfo();
    }

    public static class LevelData{
        public int level;
        public boolean medal;
        public boolean locked;
    }

    public static class Levels{
        public Array<LevelData> levels;
    }

    public LevelData getLevelDataOf(int level){
        if(level<levels.levels.size) {
            return levels.levels.get(level);
        }
        return  null;
    }

    private void createInfo(){
        try {
            Gdx.app.debug(tag,"creating JSON");
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            FileHandle source = Gdx.files.internal("levelsData1.json");
            String string = source.readString();
            levels = json.fromJson(Levels.class,string);
            System.out.println(json.prettyPrint(levels));

            FileHandle file = Gdx.files.local("levelsData.json");
            String text = json.toJson(levels);
            System.out.println(text);
            file.writeString(json.toJson(levels), false);
        }catch (Throwable t){
            Gdx.app.debug(tag,"failed to create info"+t);
        }
    }

    public void loadInfo(){
        try {
            Json json = new Json();
            FileHandle fileHandle = Gdx.files.local("levelsData.json");
            String text = fileHandle.readString();
            text = Base64Coder.decodeString(text);
            System.out.println(text);
            levels = json.fromJson(Levels.class,text);
        }catch (Throwable t){
            Gdx.app.debug(tag,"failed to load info: "+t);
            createInfo();
        }
    }

    public void saveInfo(){
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.local("levelsData.json");
        file.writeString(Base64Coder.encodeString(json.prettyPrint(levels)),false);
    }

}
