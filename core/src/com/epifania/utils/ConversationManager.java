package com.epifania.utils;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.epifania.components.BoundsComponent;
import com.epifania.components.CharacterComponent;
import com.epifania.components.GomhComponent;
import com.epifania.components.Val_Component;
import com.epifania.systems.RenderingSystem;
import com.epifania.ui.ConversationDialog;

/**
 * Created by juan on 5/28/16.
 */
public class ConversationManager {

    private static final String tag = "ConversationManager";
    public static final String NONE = "NONE";

    //script vars
    private XmlReader.Element root;
    private Array<XmlReader.Element> conversations;
    private Array<Array<XmlReader.Element>> dialogs;
    private Array<XmlReader.Element> selfThoughts;
    public int conversationNumber=0;
    public int line=0;
    public float time;
    public boolean showingThoughts=false;
    public static final float timer1 = 2f;

    //Key Matching vars
    Array<String> keys = new Array<String>();

    //Engine vars
    public Engine engine;


    //Dialog box vars
    private Vector3 tmp = new Vector3();
    public ConversationDialog dialog;
    public Container<Label> thoughtDialog;
    private static final float offset = 1f;


    public ConversationManager(FileHandle script){

        dialogs = new Array<Array<XmlReader.Element>>();
        try {
            XmlReader reader = new XmlReader();
            root = reader.parse(script);
            conversations = root.getChildrenByNameRecursively("Conversation");
            for(XmlReader.Element conversation : conversations){
                Array<XmlReader.Element> dialogs = conversation.getChildrenByNameRecursively("Dialog");
                this.dialogs.add(dialogs);
            }
            /*
            System.out.println("CONVERSATIONS");
            for(XmlReader.Element conversation : conversations)
            {
                System.out.println("character: "+conversation.get("character") +
                        " id: "+conversation.get("ID") +
                        " keys: "+conversation.get("keys") +
                        " key: "+conversation.get("key") +
                        " secondary: "+conversation.get("secondary")
                );
            }
            */


            this.selfThoughts = new Array<XmlReader.Element>();
            XmlReader.Element selfThoughts = root.getChildByName("SelfToughts");
            Array<XmlReader.Element> dialogs = selfThoughts.getChildrenByNameRecursively("Dialog");
            this.selfThoughts.addAll(dialogs);

        }catch (Exception e){
            Gdx.app.error(tag,"Error reading script: "+e);
        }
    }

    private void getRelativePosition(String character,Vector3 out){
        if(character==null)return;
        Entity entity = null;
        if(character.equals("VAL")){
            entity = engine.getEntitiesFor(Family.all(Val_Component.class, BoundsComponent.class).get()).get(0);
        }else {
            ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(CharacterComponent.class, BoundsComponent.class).exclude(GomhComponent.class,Val_Component.class).get());
            for(Entity entity1 : entities){
                CharacterComponent characterComponent = entity1.getComponent(CharacterComponent.class);
                if(characterComponent.character.equals(CharacterComponent.Character.valueOf(character.toUpperCase()))){
                    entity = entity1;
                }
            }
            if(entity==null) {
                Gdx.app.error(tag, "No entity matched with " + character);
            }
        }
        if(entity!=null){
            Rectangle rectangle = entity.getComponent(BoundsComponent.class).bounds;
            out.x = rectangle.x + rectangle.width*0.5f;
            out.y = rectangle.y + rectangle.height + offset;

            Camera camera = engine.getSystem(RenderingSystem.class).getCamera();
            camera.project(out);
            out.y = Gdx.graphics.getHeight() - out.y;
            dialog.getStage().getCamera().unproject(out);
            //Now in stage coords we can modify the x position with the dialog width
            out.x -= dialog.getWidth()*0.5f;
        }
    }

    public boolean matches(String conversationName,Array<String> keys){
        XmlReader.Element conversation = getConversationByID(conversationName);
        //If conversation does not exit return
        if(conversation==null) {
            Gdx.app.debug(tag,"conversation null for id = " + conversationName);
            return false;
        }
        //If there is no keys return true
        String keysText = conversation.getAttribute("keys");
        if(keysText.equals(NONE))
            return true;

        //If there are keys split the text and check if all of them exist
        this.keys.clear();
        this.keys.addAll(keysText.split(","));
        boolean b = true;
        for(String key : this.keys){
            b &= keys.contains(key,false);
        }
        return b;
    }

    public boolean hasSecondary(String conversationName){
        XmlReader.Element conversation = getConversationByID(conversationName);
        if((conversation.getAttribute("secondary").equals("NONE"))){
            return  false;
        }
        return true;
    }

    public String getSecondaryOf(String conversationName){
        XmlReader.Element conversation = getConversationByID(conversationName);
        return conversation.getAttribute("secondary");
    }

    public XmlReader.Element getConversationByID(String id){
        for(XmlReader.Element conversation : conversations){
            String conversationId = conversation.get("ID");
            if(conversationId.equals(id)){
                return conversation;
            }
        }
        Gdx.app.debug(tag,"fail getConversationByID for id = "+id);
        return null;
    }

    public void startConversation(String conversationID){
        for(XmlReader.Element conversation : conversations){
            if(conversation.getAttribute("ID").equals(conversationID)){
                conversationNumber = conversations.indexOf(conversation,true);
                conversationEnded();
                dialog.setVisible(true);
            }
        }
    }

    public boolean conversationEnded(){
        if(line< dialogs.get(conversationNumber).size) {
            XmlReader.Element element = dialogs.get(conversationNumber).get(line);
            dialog.setText(element.getText());
            getRelativePosition(element.get("character"),tmp);
            dialog.setPosition(tmp.x,tmp.y);
            if(!dialog.isVisible())dialog.setVisible(true);
        }else{
            dialog.setVisible(false);
            return true;
        }
        return false;
    }

    public boolean next(){
        if(line< dialogs.get(conversationNumber).size) {
            line++;
            return true;
        }
        return false;
    }

    public void showThoughts(String key){
        String text=NONE;
        for(int i = 0 ; i < selfThoughts.size ; i++) {
            String k = selfThoughts.get(i).getAttribute("key",NONE);
            Gdx.app.debug(tag,"key = "+key);
            Gdx.app.debug(tag,"k = "+k);

            if(key.contains(k)){
                text = selfThoughts.get(i).getText();
            }

        }
        if(text!=NONE) {
            time=0;
            showingThoughts=true;
            dialog.setText(text);
            updateDialogPosition("VAL");
            dialog.setVisible(true,false);
        }
    }

    public void updateDialogPosition(String character){
        getRelativePosition(character,tmp);
        dialog.setPosition(tmp.x,tmp.y);
    }

    public void hideThoughts(){
        showingThoughts=false;
        dialog.setVisible(false);
    }

}
