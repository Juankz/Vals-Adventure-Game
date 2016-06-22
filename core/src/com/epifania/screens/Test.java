package com.epifania.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.ui.ConversationDialog;
import com.epifania.ui.PauseMenu;
import com.epifania.utils.Assets;


/**
 * Created by juan on 19/05/16.
 */
public class Test extends ScreenAdapter {

    private static final String tag = "Test";

    int conversationNumber=0;
    int line=0;
    Array<String> characters;
    Array<String> text;
    Array<Array<XmlReader.Element>> convs;
    Stage stage;
    Skin skin;
    ConversationDialog dialog;
    private Label label;
    private Container<Label> container;
    private Button next;
    PauseMenu pauseMenu;

    @Override
    public void show(){
        stage = new Stage(new FillViewport(1280,720));
        skin = Assets.instance.get("user interface/uiskin.json",Skin.class);

        Button actionButton = new Button(skin,"action");
        dialog= new ConversationDialog(skin);

        label = new Label("bla bla",skin,"dialog");
        container = new Container<Label>(label);
//        container.setBackground(skin.getDrawable("dialogPanel"));
        container.padBottom(50);
        next = new Button(skin,"next");

        container.pack();
        container.setPosition(600,100);

        actionButton.setPosition(0,0);
        dialog.setPosition(300,300);
        stage.addActor(actionButton);
        stage.addActor(dialog);
        stage.addActor(container);

        pauseMenu = new PauseMenu(new PauseMenu.Listener() {
            @Override
            public void exit() {

            }

            @Override
            public void resume1() {

            }
        },Assets.instance.get("i18n/strings_ui", I18NBundle.class));

        pauseMenu.setPosition(100,100);

        stage.addActor(pauseMenu);
        stage.setDebugAll(true);

        convs = new Array<Array<XmlReader.Element>>();
        characters = new Array<String>();
        text = new Array<String>();

        try {
            XmlReader reader = new XmlReader();
            XmlReader.Element script = reader.parse(Gdx.files.internal("scripts/script1.xml"));
            Array<XmlReader.Element> conversations = script.getChildrenByNameRecursively("Conversation");
            for(XmlReader.Element conversation : conversations){
                Array<XmlReader.Element> dialogs = conversation.getChildrenByNameRecursively("Dialog");
                convs.add(dialogs);
            }
        }catch (Throwable e){
            Gdx.app.error(tag,"Error reading script: "+e);
        }
        dialog.setText(convs.get(0).get(0).getText());
    }

    private String getCharater(String line){
        String[] args = line.split(":");
        return args[0];
    }

    private String getText(String line){
        String[] args = line.split(":");
        return args[1];
    }

    @Override
    public void render(float deltaTime){
        if(Gdx.input.isKeyJustPressed(Input.Keys.N)){
            conversationNumber++;
            line=0;
            System.out.println("Conversation Number = "+conversationNumber);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.B)){
            dialog.setVisible(!dialog.isVisible());
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            dialog.moveBy(-2,0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            dialog.moveBy(2,0);
        }
        if(Gdx.input.justTouched()||Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            if(line<convs.get(conversationNumber).size) {
                XmlReader.Element element = convs.get(conversationNumber).get(line);
                System.out.println(line+" "+element.get("character")+" : "+element.getText());
                dialog.setText(element.getText());
                label.setText(element.getText());
                container.pack();
            }else{
                if(conversationNumber+1<convs.size){
                    conversationNumber++;
                    line=0;
                    System.out.println("Conversation Number = "+conversationNumber);
                    XmlReader.Element element = convs.get(conversationNumber).get(line);
                    System.out.println(line+" "+element.get("character")+" : "+element.getText());
                }
            }
            line++;
        }
        stage.act();
        stage.draw();
    }
}
