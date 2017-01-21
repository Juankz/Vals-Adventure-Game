package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.epifania.utils.Settings;
import com.epifania.utils.SoundManager;

/**
 * Created by juan on 1/18/17.
 */
public class SettingsPanel extends Table {

    private String tag = "UI/SettingsPanel";
    private  I18NBundle bundle;

    private Table root;

    private Label language_label;
    private Button next_language_button;
    private Button previous_language_button;
    
    private Label control_label;
    private Button next_control_button;
    private Button previous_control_button;

    private Label reset_warning_label;
    private String original_language;

    int languageId = 0;

    public interface Listener{
        void applyChanges();
    }

    public SettingsPanel(Skin skin, I18NBundle bundle, Listener listener){
        init(skin,bundle,listener);

    }
    public SettingsPanel(Skin skin, I18NBundle bundle){
        init(skin, bundle, new Listener() {
            @Override
            public void applyChanges() {

            }
        });
    }

    private void init(Skin skin, final I18NBundle bundle, final Listener listener){
        this.bundle = bundle;
        root = new Table();
        root.setBackground(skin.getDrawable("pause_panel"));

        Label title = new Label(bundle.get("settings").toUpperCase(),skin,"header");
        title.setColor(Color.LIGHT_GRAY.BROWN);

        Table settingsTable = new Table();
        settingsTable.setBackground(skin.getDrawable("brown_panel"));
        
        int pad1 = 10;
        int pad2 = 50;

        final Slider sound_slider = new Slider(0,10,1,false,skin);
        sound_slider.setValue(Settings.instance.sfxVolume*10);
        final Label sound_label = new Label(String.valueOf((int)(Settings.instance.sfxVolume*10)),skin,"dialog");
        settingsTable.add(new Label(bundle.get("sound"),skin,"dialog")).pad(pad1).padRight(pad2).left().colspan(2);
        settingsTable.row();
        Table sound_group = new Table();
        sound_group.add(sound_slider).expandX().fillX().padRight(pad1);
        sound_group.add(sound_label);
        settingsTable.add(sound_group).expandX().colspan(2).fillX().pad(pad1);
        settingsTable.row();

        final Slider music_slider = new Slider(0,10,1,false,skin);
        music_slider.setValue(Settings.instance.musicVolume*10);
        final Label music_label = new Label(String.valueOf((int)(Settings.instance.musicVolume*10)),skin,"dialog");
        settingsTable.add(new Label(bundle.get("music"),skin,"dialog")).pad(pad1,pad1,pad1,pad2).left().colspan(2);
        settingsTable.row();
        Table music_group = new Table();
        music_group.add(music_slider).expandX().fillX().padRight(pad1);
        music_group.add(music_label);
        settingsTable.add(music_group).expandX().colspan(2).fillX().pad(pad1);
        settingsTable.row();

        Table language_table = new Table();
        next_language_button = new Button(skin,"next_white_button");
        previous_language_button = new Button(skin,"previous_white_button");
        language_label = new Label("",skin,"dialog");
        languageId = Settings.instance.languages.keys().toArray().indexOf(Settings.instance.language,true);
        original_language = Settings.instance.language;
        set_language_text();
        language_table.add(previous_language_button).pad(pad1);
        language_table.add(language_label).expandX().pad(pad1);
        language_table.add(next_language_button).pad(pad1);
        settingsTable.add(new Label(bundle.get("language"),skin,"dialog")).pad(pad1).padRight(pad2).left();
        settingsTable.add(language_table).fillX().pad(pad1);
        settingsTable.row();

        Table control_table = new Table();
        next_control_button = new Button(skin,"next_white_button");
        previous_control_button = new Button(skin,"previous_white_button");
        control_label = new Label("",skin,"dialog");
        set_control_text();
        control_table.add(previous_control_button).pad(pad1);
        control_table.add(control_label).expandX().pad(pad1);
        control_table.add(next_control_button).pad(pad1);
        settingsTable.add(new Label(bundle.get("control"),skin,"dialog")).pad(pad1).padRight(pad2).left();
        settingsTable.add(control_table).fillX().pad(pad1);
        settingsTable.pad(20,30,20,30);
        settingsTable.pack();

        reset_warning_label = new Label(bundle.get("reset_warning"),skin);
        reset_warning_label.setColor(Color.RED);
        reset_warning_label.setVisible(false);

        TextButton ok_button = new TextButton("OK",skin,"longBrown");

        float pad = 5;
        root.pad(60,50,20,50);
        root.add(title);
        root.row().pad(pad);
        root.add(settingsTable);
        root.row().pad(pad);
        root.add(reset_warning_label).pad(pad1);
        root.row().pad(pad);
        root.add(ok_button);
        root.pack();
        root.setTransform(true);
        root.setOrigin(Align.center);

        this.add(root).center();
        this.setBackground(skin.getDrawable("opaque_pixel"));
        this.setTouchable(Touchable.enabled);
        this.setFillParent(true);


        //Add listeners
        ok_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                listener.applyChanges();
                hide();
            }
        });

        sound_slider.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                Gdx.app.log("UITest", "slider: " + sound_slider.getValue());
                Settings.instance.sfxVolume = sound_slider.getValue()*0.1f;
                sound_label.setText(String.valueOf((int)sound_slider.getValue()));
                SoundManager.playSound("sounds/click1.ogg");
            }
        });

        music_slider.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                float value = music_slider.getValue()*0.1f;
                Settings.instance.musicVolume = value;
                music_label.setText(String.valueOf((int)music_slider.getValue()));
                for(Music music : SoundManager.musicFiles){
                    music.setVolume(value);
                }
            }
        });

        next_control_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                Settings.instance.controls=!Settings.instance.controls;
                set_control_text();
            }
        });

        previous_control_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                Settings.instance.controls=!Settings.instance.controls;
                set_control_text();
            }
        });

        next_language_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                languageId++;
                if(languageId > Settings.instance.languages.size - 1){
                    languageId = 0;
                }
                String key = Settings.instance.languages.keys().toArray().get(languageId);
                Settings.instance.language = key;
                set_language_text();
            }
        });

        previous_language_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                languageId--;
                if(languageId < 0){
                    languageId = Settings.instance.languages.size - 1;
                }
                String key = Settings.instance.languages.keys().toArray().get(languageId);
                Settings.instance.language = key;
                set_language_text();
            }
        });

        this.setVisible(false);
    }

    private void set_control_text(){
        if(Settings.instance.controls){
            control_label.setText(bundle.get("touch"));
        }else {
            control_label.setText(bundle.get("buttons"));
        }
    }

    private void set_language_text(){
        language_label.setText(Settings.instance.languages.get(Settings.instance.language));
        if(original_language!=Settings.instance.language){
            reset_warning_label.setVisible(true);
        }
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        super.setScale(scaleX, scaleY);
        root.setScale(scaleX, scaleY);
    }

    public void show(){
        float duration = 0.2f;
        this.setScale(1.1f,1.1f);
        this.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.show(),
                Actions.parallel(
                        Actions.fadeIn(duration),
                        Actions.scaleTo(1f,1f,duration*2,Interpolation.bounceOut))
        ));
    }
    public void hide(){
        float duration = 0.2f;
        this.addAction(Actions.sequence(
                Actions.alpha(1),
                Actions.parallel(
                    Actions.fadeOut(duration),
                    Actions.scaleTo(1.1f,1.1f,duration*2)),
                Actions.hide()
        ));
    }
}
