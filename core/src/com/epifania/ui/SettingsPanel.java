package com.epifania.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.epifania.utils.Settings;

/**
 * Created by juan on 1/18/17.
 */
public class SettingsPanel extends Table {

    private Table root;

    private Label language_label;
    private Button next_language_button;
    private Button previous_language_button;
    
    private Label control_label;
    private Button next_control_button;
    private Button previous_control_button;

    public SettingsPanel(Skin skin, I18NBundle bundle){
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
        final Label sound_label = new Label(String.valueOf((int)(Settings.instance.sfxVolume*10)),skin,"middle");
        settingsTable.add(new Label(bundle.get("sound"),skin,"middle")).pad(pad1).padRight(pad2).left();
        settingsTable.add(sound_slider).pad(pad1);
        settingsTable.add(sound_label).pad(pad1);
        settingsTable.row();
        final Slider music_slider = new Slider(0,10,1,false,skin);
        music_slider.setValue(Settings.instance.musicVolume*10);
        final Label music_label = new Label(String.valueOf((int)(Settings.instance.musicVolume*10)),skin,"middle");
        settingsTable.add(new Label(bundle.get("music"),skin,"middle")).pad(pad1,pad1,pad1,pad2).left();
        settingsTable.add(music_slider).pad(pad1);
        settingsTable.add(music_label).pad(pad1);
        settingsTable.row();
        Table language_table = new Table();
        next_language_button = new Button(skin,"next_white_button");
        previous_language_button = new Button(skin,"previous_white_button");
        language_label = new Label("Español",skin,"middle");
        language_table.add(previous_language_button).pad(pad1);
        language_table.add(language_label).expandX().pad(pad1);
        language_table.add(next_language_button).pad(pad1);
        settingsTable.add(new Label(bundle.get("language"),skin,"middle")).pad(pad1).padRight(pad2).left();
        settingsTable.add(language_table).fillX().pad(pad1).colspan(2);
        settingsTable.row();
        Table control_table = new Table();
        next_control_button = new Button(skin,"next_white_button");
        previous_control_button = new Button(skin,"previous_white_button");
        control_label = new Label("Touch",skin,"middle");
        control_table.add(previous_control_button).pad(pad1);
        control_table.add(control_label).expandX().pad(pad1);
        control_table.add(next_control_button).pad(pad1);
        settingsTable.add(new Label(bundle.get("control"),skin,"middle")).pad(pad1).padRight(pad2).left();
        settingsTable.add(control_table).fillX().pad(pad1).colspan(2);
        settingsTable.pad(50);
        settingsTable.pack();


        TextButton ok_button = new TextButton("OK",skin,"longBrown");

        float pad = 15;
        root.pad(50);
        root.add(title);
        root.row().pad(pad);
        root.add(settingsTable);
        root.row().pad(pad);
        root.add(ok_button);
        root.pack();

        this.add(root).center();
        this.setBackground(skin.getDrawable("opaque_pixel"));
        this.setTouchable(Touchable.enabled);
        this.setFillParent(true);

        //Add listeners
        ok_button.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y) {
                hide();
            }
        });

        sound_slider.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                Gdx.app.log("UITest", "slider: " + sound_slider.getValue());
                Settings.instance.sfxVolume = sound_slider.getValue()*0.1f;
                sound_label.setText(String.valueOf((int)sound_slider.getValue()));
            }
        });

        music_slider.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                Settings.instance.musicVolume = music_slider.getValue()*0.1f;
                music_label.setText(String.valueOf((int)music_slider.getValue()));

            }
        });

        this.setVisible(false);
    }

    public void show(){
        float duration = 0.25f;
        this.setScale(1.2f,1.2f);
        this.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.show(),
                Actions.parallel(
                        Actions.fadeIn(duration),
                        Actions.scaleTo(1f,1f))
        ));
    }
    public void hide(){
        float duration = 0.25f;
        this.addAction(Actions.sequence(
                Actions.alpha(1),
                Actions.parallel(
                    Actions.fadeOut(duration),
                    Actions.scaleTo(1.2f,1.2f)),
                Actions.hide()
        ));
    }
}
