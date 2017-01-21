package com.epifania.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;

/**
 * Created by juan on 1/21/17.
 */
public class Panel extends Table {

    protected Table root;
    protected Skin skin;
    protected I18NBundle bundle;

    public Panel(Skin skin, I18NBundle bundle){
        this.bundle = bundle;
        this.skin = skin;
        this.setFillParent(true);
        this.setBackground(skin.getDrawable("opaque_pixel"));
        this.setTouchable(Touchable.enabled);
        this.setVisible(false);

        root = new Table();
        root.setBackground(skin.getDrawable("pause_panel"));
        root.setTransform(true);
        root.setOrigin(Align.center);
        this.add(root).center();
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
                        Actions.scaleTo(1f,1f,duration*2, Interpolation.bounceOut))
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
