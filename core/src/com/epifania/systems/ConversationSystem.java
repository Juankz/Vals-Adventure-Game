package com.epifania.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.epifania.components.*;
import com.epifania.ui.ConversationDialog;
import com.epifania.utils.InputHandler;

/**
 * Created by juan on 5/24/16.
 */
public class ConversationSystem extends EntitySystem {

    private static final String tag = "ConversationSystem";
    public ConversationDialog dialog;
    private int conversationNumber=0;
    private int line=0;
    private Vector3 tmp = new Vector3();
    private Array<Array<XmlReader.Element>> dialogs;
    private Array<XmlReader.Element> conversations;
    private static final float offset = 1f;
    private Engine engine;
    private ImmutableArray<Entity> vals;
    private ImmutableArray<Entity> characters;
    private  ConversationState state = ConversationState.WAITING;
    public InputHandler inputHandler;
    public float minDistance = 3;


    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
        vals = this.engine.getEntitiesFor(Family.all(Val_Component.class,TransformComponent.class,BoundsComponent.class,StateComponent.class,ConversationComponent.class).get());
        characters = this.engine.getEntitiesFor(Family.all(TransformComponent.class,ConversationComponent.class).one(GomhComponent.class).get());
    }

    public ConversationSystem(FileHandle script) {
        dialogs = new Array<Array<XmlReader.Element>>();
        try {
            XmlReader reader = new XmlReader();
            XmlReader.Element root = reader.parse(script);
            conversations = root.getChildrenByNameRecursively("Conversation");
            for(XmlReader.Element conversation : conversations){
                Array<XmlReader.Element> dialogs = conversation.getChildrenByNameRecursively("Dialog");
                this.dialogs.add(dialogs);
            }
        }catch (Throwable e){
            Gdx.app.error(tag,"Error reading script: "+e);
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Entity val : vals){
            for(Entity character : characters){
                switch(state){
                    case WAITING:
                        waiting(val,character);
                        break;
                    case GOING_ON:
                        if(!show()){
                            inputHandler.setActive(true);
                            engine.getSystem(Val_System.class).canMove = true;
                            state=ConversationState.END;
                        }
                        break;
                    case END:
                        float distance = character.getComponent(TransformComponent.class).pos.dst(val.getComponent(TransformComponent.class).pos);
                        if(distance>minDistance){
                            state=ConversationState.WAITING;
                            if(conversationNumber+1< dialogs.size) {
                                conversationNumber++;
                                line = 0;
                            }else{
                                state=ConversationState.NO_MORE;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void waiting(Entity val, Entity character){
        ConversationComponent cc= character.getComponent(ConversationComponent.class);
        ConversationComponent vc = val.getComponent(ConversationComponent.class);
        float distance = character.getComponent(TransformComponent.class).pos.dst(val.getComponent(TransformComponent.class).pos);
        if(distance < minDistance) {
            for (String condition : vc.conditions) {
                String character1 = conversations.get(conversationNumber).get("character");
                String key = conversations.get(conversationNumber).get("key");

                if (condition.equals(character1 + key)) {
                    if (!dialog.isVisible())
                        dialog.setVisible(true);
                    show();
                    inputHandler.setActive(false);
                    engine.getSystem(Val_System.class).setMove(val, false);
                    state = ConversationState.GOING_ON;
                } else {
                    Gdx.app.debug(tag, "xml character = " + character1);
                    Gdx.app.debug(tag, "xml key = " + key);
                    Gdx.app.debug(tag, "condition =" + condition);
                }
            }
        } else{
            if(dialog.isVisible())dialog.setVisible(false);
        }
    }

    public boolean next(){
        if(line< dialogs.get(conversationNumber).size) {
            line++;
            return true;
        }
        return false;
    }

    public boolean show(){
        if(line< dialogs.get(conversationNumber).size) {
            XmlReader.Element element = dialogs.get(conversationNumber).get(line);
            dialog.setText(element.getText());
            getRelativePosition(element.get("character"),tmp);
            dialog.setPosition(tmp.x,tmp.y);
//            Gdx.app.debug("Conversation System",element.get("character")+" : "+element.getText());
            if(!dialog.isVisible())dialog.setVisible(true);
        }else{
            dialog.setVisible(false);
            return false;
        }
        return true;
    }

    private void getRelativePosition(String character,Vector3 out){
        if(character==null)return;
        Entity entity = null;
        if(character.equals("VAL")){
            entity = getEngine().getEntitiesFor(Family.all(Val_Component.class, BoundsComponent.class).get()).get(0);
        }else if (character.equals("GOMH")){
            entity = getEngine().getEntitiesFor(Family.all(GomhComponent.class, BoundsComponent.class).get()).get(0);
        }else {
            Gdx.app.error(tag,"No entity matched with "+character);
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

    private enum ConversationState{
        WAITING,GOING_ON,END,NO_MORE
    }
}
