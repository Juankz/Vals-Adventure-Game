package com.epifania.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.epifania.components.CharacterComponent;
import com.epifania.components.TransformComponent;
import com.epifania.components.Val_Component;
import com.epifania.utils.ConversationManager;
import com.epifania.utils.InputHandler;

/**
 * Created by juan on 5/28/16.
 */
public class CharacterSystem extends IteratingSystem {

    public interface CharacterListener{
        void setActive(boolean b);
        void gameOver();
    }

    private static final String tag = "Character System";
    private static final float dstXM = 2f;
    private static final float dstYM = 4;
    private static final float dstYm = -1;

    private Entity val;

    public ConversationManager manager;
    private boolean isSecondary = false;
    public CharacterListener characterListener;
    private boolean exitAfterConversation = false;

    public CharacterSystem() {
        super(Family.all(CharacterComponent.class,TransformComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        for(Entity val : getEngine().getEntitiesFor(Family.all(Val_Component.class,TransformComponent.class).get())) {
            this.val = val;
            CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
            CharacterComponent.States state = characterComponent.state;
            switch (state) {
                case WAITING_IN:
                    String conversationID = characterComponent.conversationIDs.get(characterComponent.current);
                    Array<String> valKeys = val.getComponent(Val_Component.class).conversationKeys;

                    if (isEntityClose(val, entity)) {
                        if (manager.matches(conversationID, valKeys)) {
                            //Some level may and with a conversation, just one conversation key with
                            // id "EXIT" is allowed. If this is the case, then use enable a flag for notification
                            if(conversationID.equals("EXIT"))
                                exitAfterConversation=true;
                            characterComponent.state = CharacterComponent.States.CONVERSATING;
                            Gdx.app.debug(tag,"Start Conversation with "+characterComponent.character);
                            isSecondary = false;
                            manager.line = 0;
                            getEngine().getSystem(Val_System.class).setMove(val,false);
                            characterListener.setActive(false);
                            manager.startConversation(conversationID);
                        }else if(manager.hasSecondary(conversationID)){
                            characterComponent.state = CharacterComponent.States.CONVERSATING;
                            Gdx.app.debug(tag,"Start Secondary Conversation with "+characterComponent.character);
                            isSecondary = true;
                            manager.line = 0;
                            getEngine().getSystem(Val_System.class).setMove(val,false);
                            characterListener.setActive(false);
                            manager.startConversation(manager.getSecondaryOf(conversationID));
                        }
                    }
                    break;
                case CONVERSATING:
                    if (manager.conversationEnded()) {
                        characterListener.setActive(true);
                        getEngine().getSystem(Val_System.class).canMove = true;
                        characterComponent.state = CharacterComponent.States.WATING_OUT;
                        Gdx.app.debug(tag,"Waiting out for "+characterComponent.character);
                        if(exitAfterConversation){
                            characterListener.gameOver();
                        }
                    }
                    break;
                case WATING_OUT:
                    if (!isEntityClose(val, entity
                    )) {
                        if(characterComponent.current+1<characterComponent.conversationIDs.size) {
                            if(!isSecondary) {
                                conversationID = characterComponent.conversationIDs.get(characterComponent.current);
                                val.getComponent(Val_Component.class).conversationKeys.add(conversationID);
                                characterComponent.current++;
                            }
                            manager.line=0;
                            characterComponent.state = CharacterComponent.States.WAITING_IN;
                        }else{
                            if(!isSecondary) {
                                characterComponent.state = CharacterComponent.States.DO_NOTHING;
                            }else{
                                manager.line=0;
                                characterComponent.state = CharacterComponent.States.WAITING_IN;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isEntityClose(Entity entity, Entity other){
        if(entity.flags==other.flags) {
            Vector3 entityPosition = entity.getComponent(TransformComponent.class).pos;
            Vector3 otherPosition = other.getComponent(TransformComponent.class).pos;

            float dstX = Math.abs(entityPosition.x - otherPosition.x);
            float dstY = entityPosition.y - otherPosition.y;

            if (dstX < dstXM && (dstY < dstYM && dstY > dstYm))
                return true;
        }
        return false;
    }
}
