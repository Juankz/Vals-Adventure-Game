package com.epifania.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.epifania.components.ActionableComponent;
import com.epifania.components.MapTileComponent;
import com.epifania.components.PasswordComponent;
import com.epifania.components.PasswordPieceComponent;

/**
 * Created by juan on 6/23/16.
 */
public class PasswordSystem extends EntitySystem {

    private Family family1 = Family.all(MapTileComponent.class, PasswordPieceComponent.class).get();
    private Family family2 = Family.all(PasswordComponent.class, ActionableComponent.class).get();
    private ComponentMapper<PasswordPieceComponent> mapperPPC;
    private ComponentMapper<PasswordComponent> mapperPC;
    private ComponentMapper<ActionableComponent> mapperAC;

    public PasswordSystem(){
        super();
        mapperPPC = ComponentMapper.getFor(PasswordPieceComponent.class);
        mapperPC = ComponentMapper.getFor(PasswordComponent.class);
        mapperAC = ComponentMapper.getFor(ActionableComponent.class);
    }

    public void nextKey(Entity entity){
        if(!family1.matches(entity))return;
        PasswordPieceComponent passwordPieceComponent = mapperPPC.get(entity);

        if(++passwordPieceComponent.actualValue > PasswordPieceComponent.maxValue){
            passwordPieceComponent.actualValue=1;
        }
        getEngine().getSystem(TiledMapSystem.class).setTile(entity,passwordPieceComponent.actualValue);
        checkPassword();
    }

    private void checkPassword(){
        ImmutableArray<Entity> passwords = getEngine().getEntitiesFor(family2);
        for(Entity entity : passwords){
            PasswordComponent passwordComponent = mapperPC.get(entity);
            if(passwordComponent.unlocked)continue;
            boolean match = true;
            ActionableComponent actionableComponent = mapperAC.get(entity);
            for(PasswordPieceComponent passwordPieceComponent : passwordComponent.keys){
                match &= (passwordPieceComponent.actualValue == passwordPieceComponent.key);
            }
            if(match){
                actionableComponent.actionable.action();
                passwordComponent.unlocked = true;
            }
        }
    }
}
