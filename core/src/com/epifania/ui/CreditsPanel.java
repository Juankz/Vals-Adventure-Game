package com.epifania.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;

/**
 * Created by juan on 1/21/17.
 */
public class CreditsPanel extends Panel {

    public CreditsPanel(Skin skin,I18NBundle bundle){
        super(skin,bundle);

        root.pad(50,50,20,50);
        float pad=5;
        root.add(new Label(bundle.get("credits").toUpperCase(),skin,"header_brown")).pad(pad).colspan(2);
        root.row();
        root.add(new Label(bundle.get("design_and_programming"),skin,"credits")).pad(pad).padTop(pad*3);
        root.add(new Label(bundle.get("audio_edit"),skin,"credits")).pad(pad).padTop(pad*3);
        root.row();
        root.add(new Label("Juan Velandia",skin,"credits_text")).pad(pad);
        root.add(new Label("Sergio Díaz",skin,"credits_text")).pad(pad);
        root.row().pad(pad);
        root.add(new Label(bundle.get("extra_graphics"),skin,"credits")).pad(pad).colspan(2).padTop(pad*3);;
        root.row();
        root.add(new Label("Hernando Méndez",skin,"credits_text")).pad(pad);
        root.add(new Label("Juan Velandia",skin,"credits_text")).pad(pad);
        root.row();
        root.add(new Label(bundle.get("made_with"),skin,"credits_small_brown")).pad(pad).padTop(pad*3).colspan(2);
        root.row();
        root.add(new Label("Libgdx - Inkscape - GIMP - Audacity",skin,"credits_small")).pad(pad).colspan(2);
        root.row();
        root.add(new Label(bundle.get("based_on")+" Kenney.nl",skin,"credits_small")).pad(pad).colspan(2);
        root.row();

        TextButton button = new TextButton("OK",skin,"longBrown");
        root.add(button).padTop(40).colspan(2);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                hide();
            }
        });
    }
}
