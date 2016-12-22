package com.epifania.utils;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.*;
import com.epifania.components.*;
import com.epifania.systems.*;

/**
 * Created by juan on 6/4/16.
 *
 * Based on map data create and add entities to the game engine
 */
public class LevelBuilder {

    private static final String tag = "LevelBuilder";
    private static final String NONE = "NONE";
    public interface Listener{
        void exit();
        void destroyJoint(int id);
        void addJoint(int id,Joint joint);
    }

    private Engine engine;
    private TiledMap levelMap;
    private Listener listener;
    public int totalCoins;

    private ObjectMap<Integer,Body> bodiesA;
    private ObjectMap<Integer,Body> bodiesB;


    public LevelBuilder(Engine engine,Listener listener){
        this.listener = listener;
        this.engine = engine;
        bodiesA = new ObjectMap<Integer, Body>();
        bodiesB = new ObjectMap<Integer, Body>();
    }

    public void loadLevel(TiledMap levelMap){
        this.levelMap = levelMap;

        setMap();
        createWorldBounds();
        createCoins();

        for(TiledMapTileSet set : levelMap.getTileSets()){
            Gdx.app.debug(tag,"tileset name : "+set.getName());
        }

        for(int flag = 0;flag<Constants.objectsLayersNames.length;flag++) {
            createSpring(flag);
            createFlags(flag);
            createItems(flag);
            createJoints(flag);
            createPostItems(flag);
        }
        setTilesAnimation("GroundBack");
        setTilesAnimation("GroundMid");
        createBackground();
    }

    private  void createWorldBounds(){
        int[] vertices = {
                0,
                0,
                (Integer)levelMap.getProperties().get("width"),
                (Integer)levelMap.getProperties().get("height")
        };

        Body body;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        EdgeShape shape = new EdgeShape();
        shape.set(vertices[0],vertices[1],vertices[2],vertices[1]); //h1
        EdgeShape shape1 = new EdgeShape();
        shape1.set(vertices[0],vertices[3],vertices[2],vertices[3]); //h2
        EdgeShape shape2 = new EdgeShape();
        shape2.set(vertices[0],vertices[1],vertices[0],vertices[3]); //v1
        EdgeShape shape3 = new EdgeShape();
        shape3.set(vertices[2],vertices[1],vertices[2],vertices[3]); //v2
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        FixtureDef fix1 = new FixtureDef();
        fix1.shape = shape1;
        FixtureDef fix2 = new FixtureDef();
        fix2.shape = shape2;
        FixtureDef fix3 = new FixtureDef();
        fix3.shape = shape3;
        body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        body.createFixture(fix);
        body.createFixture(fix1);
        body.createFixture(fix2);
        body.createFixture(fix3);
        shape.dispose();
        shape1.dispose();
        shape2.dispose();
        shape3.dispose();
    }

    private void createJoints(int flag){
        MapObjects objects = levelMap.getLayers().get(Constants.objectsLayersNames[flag]).getObjects();
        if(objects==null)return;
        for(MapObject object : objects){
            Object property = object.getProperties().get("type");
            if(property==null)
                continue;
            String type = (String)property;
            if(type.equals("distance joint")){
                int id = Integer.parseInt((String)object.getProperties().get("jointID"));

                float lineVertices[] = ((PolylineMapObject)object).getPolyline().getTransformedVertices();

                float anchorAX = bodiesA.get(id).getPosition().x - lineVertices[0]*Constants.PixelsPerUnit;
                float anchorAY = bodiesA.get(id).getPosition().y - lineVertices[1]*Constants.PixelsPerUnit;
                float anchorBX = bodiesB.get(id).getPosition().x - lineVertices[2]*Constants.PixelsPerUnit;
                float anchorBY = bodiesB.get(id).getPosition().y - lineVertices[3]*Constants.PixelsPerUnit;

                DistanceJointDef jointDef = new DistanceJointDef();
                jointDef.bodyA = bodiesA.get(id);
                jointDef.bodyB = bodiesB.get(id);
                jointDef.collideConnected = false;
                jointDef.length = ((PolylineMapObject)object).getPolyline().getLength()*Constants.PixelsPerUnit;
                jointDef.localAnchorA.set(-anchorAX,0);
                jointDef.localAnchorB.set(anchorBX,0);
                Joint joint = engine.getSystem(PhysicsSystem.class).getWorld().createJoint(jointDef);
                listener.addJoint(id,joint);
            }else if(type.equals("revolution joint")){
                //If there are other types of joints
            }
        }
    }

    private void createItems(int flag){
        MapObjects objects = levelMap.getLayers().get(Constants.objectsLayersNames[flag]).getObjects();
        if(objects==null)return;
        for(MapObject object : objects){
            Object property = object.getProperties().get("type");
            if(property==null)
                continue;
            String type = (String)property;
            if(type.equals("switch")){
                createSwitch(object,flag);
            }else
            if(type.equals("trigger")){
                String action = (String)object.getProperties().get("action");
                if(action.equals("checkpoint"))
                    createCheckpoint(object,flag);
                if(action.equals("die"))
                    createDeathZone(object,flag);
            }else
            if(type.equals("character")){
                String name = (String)object.getProperties().get("name");
                if(name.equals("GOMH")||name.equals("MOM")||name.equals("PINKY")||name.equals("BLUE")){
                    createCharacter(object,flag,name);
                }else if(name.equals("VAL")){
                    //Create the main character and make the camera follow it
                    Entity val = createVal(object.getProperties(),flag);
                    createCamera(val);
                    engine.addEntity(val);
                }else {
                    Gdx.app.error(tag,"character not found : "+name);
                }
            }else
            if(type.equals("collectable")){
                createCollectable(object,flag);
            }else if(type.equals("bridge")){
                createBridge(object,flag);
            }else if(type.equals("rope")){
                createRope(object,flag);
            }else if(type.equals("ground")){
                createGround(object,flag);
            }else if(type.equals("wall")){
                createWall(object,flag);
            }else if(type.equals("ladder")){
                createLadder(object,flag);
            }else if(type.equals("door")){
                createDoor(object,flag);
            }else if(type.equals("indoor")){
                createIndoor(object,flag);
            }else if(type.equals("pack")){
                createPack(object,flag);
            }else if(type.equals("trunk")){
                createTrunk(object,flag);
            }else if(type.equals("button")){
                createButton(object,flag);
            }else if(type.equals("box")){
                createBox(object,flag);
            }else if(type.equals("platform")){
                createPlatform(object,flag);
            }else if(type.equals("block")){
                createBlock(object,flag);
            }else if(type.equals("password")){
                createPassword(object,flag);
            }else if(type.equals("exit")){
                createExit(object.getProperties());
            }
        }
    }

    /**
     * This is for the items which depends form others which had been
     * already created and thus cannot be create before the dependencies
     * @param flag layer ID
     */
    private void createPostItems(int flag){
        MapObjects objects = levelMap.getLayers().get(Constants.objectsLayersNames[flag]).getObjects();
        if(objects==null)return;
        for(MapObject object : objects){
            Object property = object.getProperties().get("type");
            if(property==null)
                continue;

            String type = (String)property;

            if(type.equals("thread")){
                createThread(object,flag);
            }
        }
    }

    private void createBlock(final MapObject object , int flag){
        final ActionableComponent actionableComponent = new ActionableComponent();
        TextureComponent textureComponent = new TextureComponent();

        final Entity entity = createRectangle(object,"",
                Constants.groupsIndexes[flag],
                Constants.layerCategoryBits[flag],
                Constants.layerMaskBits[flag]);

        textureComponent.region = captureTile((TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]),object);

        actionableComponent.key = (String)object.getProperties().get("key");
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                engine.getSystem(PhysicsSystem.class).destroyBody(entity.getComponent(BodyComponent.class).body);
                entity.remove(BodyComponent.class);
                entity.remove(ActionableComponent.class);
                engine.getSystem(TextureManipulationSystem.class).decreaseAlpha(entity,0);
            }
        };

        BoundsComponent boundsComponent = entity.getComponent(BoundsComponent.class);
        boundsComponent.bounds.width+=0.5f;
        boundsComponent.bounds.x -= 0.25f;

        entity.add(textureComponent);
        entity.add(actionableComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private IntMap<TiledMapTile> getButtonTiles(String color){
        IntMap<TiledMapTile> map = new IntMap<TiledMapTile>();

        for(TiledMapTile tile : levelMap.getTileSets().getTileSet("Items")){
            Object property = tile.getProperties().get("tag");
            if(property == null)continue;
            String tag = (String)property;
            if(tag.equals("button")){
                String color1 = (String)tile.getProperties().get("color");
                if(color1.equals(color)){
                    String state = (String)tile.getProperties().get("state");
                    if(state.equals("up")){
                        map.put(ButtonComponent.UP,tile);
                    }else if(state.equals("down")){
                        map.put(ButtonComponent.DOWN,tile);
                    }
                }
            }
        }
        return map;
    }

    private void createButton(final MapObject object, int flag){
        final Entity entity = createRectangle(object,"button",Constants.groupsIndexes[flag],
                Constants.layerCategoryBits[flag],
                Constants.layerMaskBits[flag]);
        entity.remove(BoundsComponent.class);
        entity.flags = flag;
        entity.getComponent(BodyComponent.class).body.setUserData(entity);

        Rectangle rectangle = new Rectangle();
        MapRecToWorldRec(object,rectangle);

        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        Gdx.app.debug(tag,"(int)rectangle.getX(),(int)rectangle.getY() : "+(int)rectangle.getX()+" - "+(int)rectangle.getY());
        TiledMapTileLayer.Cell cell = layer.getCell((int)rectangle.getX(),(int)rectangle.getY());
        TiledMapTile tile = cell.getTile();
        Object property = tile.getProperties().get("color");
        String color = (String)property;

        final ButtonComponent buttonComponent = new ButtonComponent();
        buttonComponent.number = Integer.parseInt((String)object.getProperties().get("number"));
        buttonComponent.color = ButtonComponent.Color.valueOf(color.toUpperCase());

        final MapTileComponent mapTileComponent = new MapTileComponent();
        mapTileComponent.tiledMaps.putAll(getButtonTiles(color));
        mapTileComponent.cell = cell;

        final ActionableComponent actionableComponent = new ActionableComponent();
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                if(object.getProperties().get("object").equals("bridge")) {
                    int number = Integer.parseInt((String)object.getProperties().get("number"));
                    for(Entity bridge : engine.getEntitiesFor(Family.all(BridgeComponent.class).get())){
                        BridgeComponent bridgeComponent = bridge.getComponent(BridgeComponent.class);
                        if(bridgeComponent.number==number){
                            engine.getSystem(BridgeSystem.class).moveBy(bridge,bridgeComponent.targets.get(bridgeComponent.targetIndex));
                            bridgeComponent.targetIndex++;
                        }
                    }
                }
                if(object.getProperties().get("object").equals("joint")) {
                    int number = Integer.parseInt((String)object.getProperties().get("number"));
                    listener.destroyJoint(number);
                }
                buttonComponent.state = ButtonComponent.DOWN;
                mapTileComponent.cell.setTile(mapTileComponent.tiledMaps.get(buttonComponent.state));
            }
        };

        entity.add(mapTileComponent);
        entity.add(buttonComponent);
        entity.add(actionableComponent);
        engine.addEntity(entity);
    }

    private void createThread(final MapObject object, int flag){

        final Entity thread = new Entity();

        BoundsComponent boundsComponent = new BoundsComponent();
        MapRecToWorldRec(object,boundsComponent.bounds);

        TagComponent tagComponent = new TagComponent();

        final ActionableComponent actionableComponent = new ActionableComponent();

        Object property = object.getProperties().get("tag");
        if(property!=null) {
            String tag = (String)property;
            tagComponent.tag = tag;
            for (Entity entity1 : engine.getEntitiesFor(Family.all(CollectableComponent.class).get())) {
                CollectableComponent collectableComponent = entity1.getComponent(CollectableComponent.class);
                if (collectableComponent.tag.equals(tag)){
                    actionableComponent.target = collectableComponent;
                }
            }
        }

        actionableComponent.key = "KNIFE";
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                if(object.getProperties().get("object").equals("joint")) {
                    int number = Integer.parseInt((String)object.getProperties().get("number"));
                    listener.destroyJoint(number);
//                        engine.removeEntity(entity);
                    if(actionableComponent.target!=null) {
                        actionableComponent.target.free = true;
                    }
                    for(Entity entity1 : engine.getEntitiesFor(Family.all(TagComponent.class).get())){
                        if(entity1.getComponent(TagComponent.class).tag.equals(thread.getComponent(TagComponent.class).tag)){
                            TextureComponent textureComponent = entity1.getComponent(TextureComponent.class);
                            if(textureComponent!= null){
                                engine.getSystem(TextureManipulationSystem.class).decreaseAlpha(entity1,0);
                            }
//                            engine.removeEntity(entity1);
                        }
                    }
                    engine.removeEntity(thread);
                }
            }
        };

        thread.add(boundsComponent);
        thread.add(tagComponent);
        thread.add(actionableComponent);
        thread.flags = flag;
        engine.addEntity(thread);


        //Create Entities with the texture and a tag

        float unit = Constants.PixelsPerUnit;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        TiledMapTileLayer tileLayer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        for(int i = 0;i<(int)h;i++){

            final Entity entity = new Entity();

            TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x,(int)y+i);
            if(cell==null)continue;

            //Get texture regions
            TextureRegion region = new TextureRegion(cell.getTile().getTextureRegion());
            region.flip(cell.getFlipHorizontally(),cell.getFlipVertically());
            cell.setTile(null);

            //Create Entity
            TransformComponent transform = new TransformComponent();
            TextureComponent textureComponent = new TextureComponent();

            transform.pos.set((int)x,(int)y+i,0);
            transform.origin.set(0.0f,0.0f);
            transform.rotation = cell.getRotation();
            textureComponent.region = region;

            entity.add(transform);
            entity.add(textureComponent);
            entity.add(tagComponent);
            entity.flags = flag;
            engine.addEntity(entity);
        }
    }

    private void createBox(MapObject object, int flag){
        Entity entity = new Entity();
        BoundsComponent boundsComponent = new BoundsComponent();
        TextureComponent textureComponent = new TextureComponent();
        TransformComponent transformComponent = new TransformComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);
        transformComponent.pos.set(boundsComponent.bounds.x,boundsComponent.bounds.y,flag);

        boolean captureTile = Boolean.parseBoolean((String)object.getProperties().get("captureTile"));
        if(captureTile){
            textureComponent.region=captureTile((TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]),object);
        }

        float density = 1;

        Object property = object.getProperties().get("density");
        if(property!=null){
            density = Float.parseFloat((String)property);
        }

        boolean rotate = true;
        property = object.getProperties().get("rotate");
        if(property!=null){
            rotate = Boolean.parseBoolean((String)property);
        }

        BodyDef.BodyType type = BodyDef.BodyType.StaticBody;
        property=object.getProperties().get("dynamic");
        if(property!=null){
            boolean isDynamic = Boolean.parseBoolean((String)property);
            if(isDynamic){
                type = BodyDef.BodyType.DynamicBody;
            }
        }

        property=object.getProperties().get("playerCollide");
        short maskBits = Constants.layerMaskBits[flag];
        short groupindex = Constants.groupsIndexes[flag];
        if(property!=null){
            boolean playerCollide = Boolean.parseBoolean((String)property);
            if(!playerCollide){
                Gdx.app.debug(tag,"Box does not collide with player");
                groupindex = 0;
                maskBits = (short)(Constants.layerCategoryBits[flag]|Constants.BOUNDS);
            }
        }

        BodyComponent bodyComponent = new BodyComponent();
        BodyDef def = new BodyDef();
        def.type = type;
        def.fixedRotation = !rotate;
        def.position.set(transformComponent.pos.x+boundsComponent.bounds.width*0.5f, transformComponent.pos.y+boundsComponent.bounds.height*0.5f);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boundsComponent.bounds.width*0.5f, boundsComponent.bounds.height*0.5f);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = density;
        fix.friction =0.5f;
        fix.filter.groupIndex = groupindex;
        fix.filter.categoryBits = Constants.layerCategoryBits[flag];
        fix.filter.maskBits = maskBits;
        bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        bodyComponent.body.createFixture(fix);
        bodyComponent.body.setUserData(entity);
        shape.dispose();
        entity.add(bodyComponent);
        checkForJoints(object,bodyComponent.body);

        BoxComponent boxComponent = new BoxComponent();

        entity.add(transformComponent);
        entity.add(boundsComponent);
        entity.add(textureComponent);
        entity.add(boxComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createPlatform(MapObject object, int flag){
        Entity entity = new Entity();
        BoundsComponent boundsComponent = new BoundsComponent();
        TextureComponent textureComponent = new TextureComponent();
        TransformComponent transformComponent = new TransformComponent();
        BodyComponent bodyComponent = new BodyComponent();
        PlatformComponent platformComponent = new PlatformComponent();

        Object property = object.getProperties().get("breakable");
        if(property != null) {
            platformComponent.breakable = Boolean.parseBoolean((String)property);
            platformComponent.breakingTime = PlatformComponent.BREAKING_TIME_MEDIUM;
        }

        property = object.getProperties().get("time");
        if(property != null) {
            platformComponent.breakingTime = Float.parseFloat((String)property);
        }

        MapRecToWorldRec(object,boundsComponent.bounds);
        transformComponent.pos.set(boundsComponent.bounds.x,boundsComponent.bounds.y,flag);

        boolean captureTile = Boolean.parseBoolean((String)object.getProperties().get("captureTile"));
        if(captureTile){
            Gdx.app.debug(tag,"captureTile platform");
            textureComponent.region=captureTile((TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]),object);
        }else{
            String textureName = (String)object.getProperties().get("texture");
            textureComponent.region =
            Assets.instance.get("game_objects/items.atlas",TextureAtlas.class).findRegion(textureName);
        }

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(transformComponent.pos.x+boundsComponent.bounds.width*0.5f, transformComponent.pos.y+boundsComponent.bounds.height*0.5f);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boundsComponent.bounds.width*0.5f, boundsComponent.bounds.height*0.5f);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.filter.groupIndex = Constants.groupsIndexes[flag];
        fix.filter.categoryBits = Constants.layerCategoryBits[flag];
        fix.filter.maskBits = Constants.layerMaskBits[flag];
        bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        bodyComponent.body.createFixture(fix);
        bodyComponent.body.setUserData(entity);
        shape.dispose();
        checkForJoints(object,bodyComponent.body);

        platformComponent.originalPosition.set(bodyComponent.body.getWorldCenter().x,bodyComponent.body.getWorldCenter().y);

        entity.add(transformComponent);
        entity.add(boundsComponent);
        entity.add(textureComponent);
        entity.add(bodyComponent);
        entity.add(platformComponent);

        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createPack(MapObject object, int flag){
        Entity entity = new Entity();
        BoundsComponent boundsComponent = new BoundsComponent();
        TextureComponent textureComponent = new TextureComponent();
        TransformComponent transformComponent = new TransformComponent();
        PackComponent packComponent = new PackComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);
        transformComponent.pos.set(boundsComponent.bounds.x,boundsComponent.bounds.y,flag);

        packComponent.content = (String)object.getProperties().get("content");
        packComponent.amount = Integer.parseInt((String)object.getProperties().get("amount"));

        if(packComponent.content.equals("coins")){
            totalCoins+=packComponent.amount;
        }

        boolean captureTile = Boolean.parseBoolean((String)object.getProperties().get("captureTile"));
        if(captureTile){
            textureComponent.region=captureTile((TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]),object);
        }
        boolean dynamic = Boolean.parseBoolean((String)object.getProperties().get("dynamic"));
        if(dynamic){
            BodyComponent bodyComponent = new BodyComponent();
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;
            def.position.set(transformComponent.pos.x+boundsComponent.bounds.width*0.5f, transformComponent.pos.y+boundsComponent.bounds.height*0.5f);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(boundsComponent.bounds.width*0.5f, boundsComponent.bounds.height*0.5f);
            FixtureDef fix = new FixtureDef();
            fix.shape = shape;
            fix.density = 1;
            fix.friction =0.1f;
            fix.filter.groupIndex = 0;
            fix.filter.categoryBits = Constants.layerCategoryBits[flag];
            fix.filter.maskBits = (short)(Constants.BOUNDS|Constants.layerCategoryBits[flag]);
            bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
            bodyComponent.body.createFixture(fix);
            shape.dispose();
            entity.add(bodyComponent);
            checkForJoints(object,bodyComponent.body);
        }

        entity.add(transformComponent);
        entity.add(boundsComponent);
        entity.add(textureComponent);
        entity.add(packComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createTrunk(MapObject object, int flag){
        Entity entity = new Entity();
        BoundsComponent boundsComponent = new BoundsComponent();
        TrunkComponent trunkComponent = new TrunkComponent();
        MapTileComponent mapTileComponent = new MapTileComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);

        trunkComponent.content = (String)object.getProperties().get("content");
        trunkComponent.amount = Integer.parseInt((String)object.getProperties().get("amount"));

        if(trunkComponent.content.equals("coins")){
            totalCoins+=trunkComponent.amount;
        }

        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        mapTileComponent.cell = layer.getCell((int)boundsComponent.bounds.getX(),(int)boundsComponent.bounds.getY());

        TiledMapTileSet tileSet= levelMap.getTileSets().getTileSet("naval");
        for(TiledMapTile tile:tileSet) {
            Object property = tile.getProperties().get("tag");
            if (property != null) {
                String tag = (String) property;
                if (tag.equals("trunk")) {
                    property = tile.getProperties().get("state");
                    if(property==null)continue;
                    String state = (String) property;
                    if (state.equals("locked")) {
                        mapTileComponent.tiledMaps.put(TrunkComponent.LOCKED,tile);
                    } else if (state.equals("open")) {
                        mapTileComponent.tiledMaps.put(TrunkComponent.OPEN,tile);
                    }else if (state.equals("empty")) {
                        mapTileComponent.tiledMaps.put(TrunkComponent.EMPTY,tile);
                    }else{
                        Gdx.app.debug(tag,"No state match: "+state);
                    }
                }
            }
        }

        boolean dynamic = Boolean.parseBoolean((String)object.getProperties().get("dynamic"));
        if(dynamic){
            BodyComponent bodyComponent = new BodyComponent();
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;
            def.position.set(boundsComponent.bounds.x+boundsComponent.bounds.width*0.5f, boundsComponent.bounds.y+boundsComponent.bounds.height*0.5f);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(boundsComponent.bounds.width*0.5f, boundsComponent.bounds.height*0.5f);
            FixtureDef fix = new FixtureDef();
            fix.shape = shape;
            fix.density = 1;
            fix.friction =0.1f;
            fix.filter.groupIndex = 0;
            fix.filter.categoryBits = Constants.layerCategoryBits[flag];
            fix.filter.maskBits = (short)(Constants.BOUNDS|Constants.layerCategoryBits[flag]);
            bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
            bodyComponent.body.createFixture(fix);
            shape.dispose();
            entity.add(bodyComponent);
            checkForJoints(object,bodyComponent.body);
        }

        entity.add(boundsComponent);
        entity.add(trunkComponent);
        entity.add(mapTileComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void checkForJoints(MapObject object, Body body){
        if(object.getProperties().get("hasJoint")!=null){
            String bod = (String)object.getProperties().get("body");
            int jointId = Integer.parseInt((String)object.getProperties().get("jointID"));
//            Gdx.app.debug(tag,"(String)object.getProperties().get(\"body\") : "+(String)object.getProperties().get("body")+" ID: "+jointId);
            if(bod.contains("BodyA")){
                bodiesA.put(jointId,body);
            }else{
                bodiesB.put(jointId,body);
            }
        }
    }

    private void createExit(MapProperties properties){
        BoundsComponent boundsComponent = new BoundsComponent();
        ActionableComponent actionableComponent = new ActionableComponent();

        boundsComponent.bounds.x = (Float)properties.get("x") * Constants.PixelsPerUnit;
        boundsComponent.bounds.y = (Float)properties.get("y") * Constants.PixelsPerUnit;
        boundsComponent.bounds.width = (Float)properties.get("width") * Constants.PixelsPerUnit;
        boundsComponent.bounds.height = (Float)properties.get("height") * Constants.PixelsPerUnit;

        actionableComponent.key = (String)properties.get("key");
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                listener.exit();
            }
        };

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(actionableComponent);
        engine.addEntity(entity);
    }

    private void createLadder(MapObject object,int flag){
        BoundsComponent boundsComponent = new BoundsComponent();
        LadderComponent ladderComponent = new LadderComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(ladderComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createCollectable(MapObject object,int flag){
        MapProperties properties = object.getProperties();
        String key = (String)properties.get("key");

        Entity entity = new Entity();
        TextureComponent textureComponent = new TextureComponent();
        TransformComponent transformComponent = new TransformComponent();
        BoundsComponent boundsComponent = new BoundsComponent();
        CollectableComponent collectableComponent = new CollectableComponent();

        if(Boolean.parseBoolean((String)properties.get("captureTile"))){
            //CaptureTile
            textureComponent.region = captureTile((TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]),object);
            if(textureComponent.region==null){
                Gdx.app.debug(tag,"Region null for tileLayer :"+Constants.itemsLayersNames[flag]);
            }
        }else {
            String atlasRegion = (String)properties.get("texture");
            TextureRegion region = Assets.instance.get("game_objects/items.atlas", TextureAtlas.class).findRegion(atlasRegion);
            if(region!=null){
                textureComponent.region = region;
            }else{
                Gdx.app.debug(tag, "No texture found: "+atlasRegion);
            }
        }

        MapRecToWorldRec(object,boundsComponent.bounds);
        transformComponent.pos.set(boundsComponent.bounds.x,boundsComponent.bounds.y,flag);

        Object property = object.getProperties().get("dynamic");
        if(property!=null){
            if(Boolean.parseBoolean((String)property)){
                BodyComponent bodyComponent = new BodyComponent();

                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                bodyDef.position.set(transformComponent.pos.x+boundsComponent.bounds.width*0.5f,
                        transformComponent.pos.y+boundsComponent.bounds.height*0.5f);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(boundsComponent.bounds.width*0.5f,boundsComponent.bounds.height*0.5f);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.filter.groupIndex = 0;
                fixtureDef.filter.categoryBits = Constants.layerCategoryBits[flag];
                fixtureDef.filter.maskBits = (short)(Constants.BOUNDS|Constants.layerCategoryBits[flag]);
                bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(bodyDef);
                bodyComponent.body.createFixture(fixtureDef);
                bodyComponent.body.setUserData(entity);
                shape.dispose();
                checkForJoints(object,bodyComponent.body);
                entity.add(bodyComponent);
            }
        }else{
            transformComponent.origin.setZero();
        }

        property = object.getProperties().get("free");
        if(property!=null){
            collectableComponent.free=Boolean.parseBoolean((String)property);
        }
        property = object.getProperties().get("tag");
        if(property!=null){
            collectableComponent.tag=(String)property;
        }

        collectableComponent.key = (String)properties.get("key");
        collectableComponent.conversationKey = (String)properties.get("conversationKey");

        entity.add(transformComponent);
        entity.add(textureComponent);
        entity.add(boundsComponent);
        entity.add(collectableComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createCheckpoint(MapObject object,int flag){
        BoundsComponent boundsComponent = new BoundsComponent();
        TransformComponent transformComponent = new TransformComponent();
        CheckpointComponent checkpointComponent = new CheckpointComponent();

        float unit = 1/70f;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        boundsComponent.bounds.set(x,y,w,h);
        transformComponent.pos.set(x,y,0);
        transformComponent.origin.setZero();
        checkpointComponent.number = Integer.parseInt((String)object.getProperties().get("number"));

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(transformComponent);
        entity.add(checkpointComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private void createDeathZone(MapObject object,int flag){
        BoundsComponent boundsComponent = new BoundsComponent();
        TransformComponent transformComponent = new TransformComponent();
        DeathZoneComponent deathZoneComponent = new DeathZoneComponent();

        float unit = 1/70f;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        boundsComponent.bounds.set(x,y,w,h);
        transformComponent.pos.set(x,y,0);
        transformComponent.origin.setZero();

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(transformComponent);
        entity.add(deathZoneComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private  void createSwitch(MapObject object,int flag){
        float unit = 1/70f;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        //Create entity
        AnimatedTileComponent animatedTileComponent = new AnimatedTileComponent();
        SwitchComponent switchComponent = new SwitchComponent();
        BoundsComponent boundsComponent = new BoundsComponent();

        TiledMapTileLayer tileLayer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x,(int)y);

        //Select tiles
        Gdx.app.debug(tag,"flag: "+ flag);
        TiledMapTileSet tileset =  levelMap.getTileSets().getTileSet("Items");
        for(TiledMapTile tile:tileset){

            Object property = tile.getProperties().get("tag");

            if(property == null) {
                continue;
            }

            String type = (String)property;
            if(type.equals("switch")){
                String state = (String)tile.getProperties().get("state");
                if(state.equals("left")){
                    animatedTileComponent.animations.put(SwitchComponent.LEFT,tile);
                }else if(state.equals("right")){
                    animatedTileComponent.animations.put(SwitchComponent.RIGHT,tile);
                }else  if(state.equals("center")){
                    animatedTileComponent.animations.put(SwitchComponent.CENTER,tile);
                }
            }
        }

        switchComponent.number = Integer.parseInt((String)object.getProperties().get("number"));
        switchComponent.cell = cell;

        boundsComponent.bounds.set(x,y,w,h);

        Entity entity = new Entity();
        entity.add(animatedTileComponent);
        entity.add(switchComponent);
        entity.add(boundsComponent);
        entity.flags = flag;
        engine.addEntity(entity);

    }

    private IntMap<TiledMapTile> getRuneMapTiles() {
        IntMap<TiledMapTile> intMap = new IntMap<TiledMapTile>();

        TiledMapTileSet tileSet = levelMap.getTileSets().getTileSet("rune");
        for(TiledMapTile tile : tileSet){
            String k= (String)tile.getProperties().get("key");
            if(k==null)continue;
            int key = Integer.parseInt(k);
            intMap.put(key,tile);
        }

        return intMap;
    }

    private  void createPassword(MapObject object, int flag){
        final Entity entity = new Entity();
        BoundsComponent boundsComponent = new BoundsComponent();
        PasswordComponent passwordComponent = new PasswordComponent();
        ActionableComponent actionableComponent = new ActionableComponent();
        MapRecToWorldRec(object,boundsComponent.bounds);

        IntMap<TiledMapTile> mapTiles = getRuneMapTiles();

        String password = (String)object.getProperties().get("password");
        String[] keys = password.split(",");

        int x = (int)boundsComponent.bounds.getX();
        int y = (int)boundsComponent.bounds.getY();
        int w = (int)boundsComponent.bounds.getWidth();

        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);

        for(int i = 0; i < w; i++){
            int key = Integer.parseInt(keys[i]);
            Entity piece = createPasswordPiece(x + i, y, layer, key, mapTiles);
            passwordComponent.keys.add(piece.getComponent(PasswordPieceComponent.class));
            engine.addEntity(piece);
        }

        Object property = object.getProperties().get("object");
        if(property!=null){
            String obj = (String)property;
            if(obj.equals("bridge")){
                property = object.getProperties().get("id");
                final int id = Integer.parseInt((String)property);
                actionableComponent.actionable = new ActionableComponent.Actionable() {
                    @Override
                    public void action() {
                        for(Entity bridge : engine.getEntitiesFor(Family.all(BridgeComponent.class).get())){
                            BridgeComponent bc = bridge.getComponent(BridgeComponent.class);
                            if(bc.number == id){
                                engine.getSystem(BridgeSystem.class).moveBy(bridge,bc.targets.get(bc.targetIndex));
                            }
                        }
                    }
                };
            }
        }


        entity.add(actionableComponent);
        entity.add(passwordComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private Entity createPasswordPiece(int x, int y, TiledMapTileLayer layer , int key, IntMap<TiledMapTile> mapTiles){
        final Entity entity = new Entity();
        TiledMapTileLayer.Cell cell = layer.getCell(x,y);

        PasswordPieceComponent passwordPieceComponent = new PasswordPieceComponent();
        passwordPieceComponent.key = key;
        Object property = cell.getTile().getProperties().get("key");
        passwordPieceComponent.actualValue = Integer.parseInt((String)property);
        MapTileComponent mapTileComponent = new MapTileComponent();
        mapTileComponent.cell = cell;
        mapTileComponent.tiledMaps.putAll(mapTiles);
        BoundsComponent boundsComponent = new BoundsComponent();
        boundsComponent.bounds.set(x+0.35f,y-2,0.1f,3);
        ActionableComponent actionableComponent = new ActionableComponent();
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                engine.getSystem(PasswordSystem.class).nextKey(entity);
            }
        };

        entity.add(passwordPieceComponent);
        entity.add(mapTileComponent);
        entity.add(boundsComponent);
        entity.add(actionableComponent);
        return  entity;
    }

    private void createDoor(MapObject object,int flag){
        BoundsComponent boundsComponent = new BoundsComponent();
        ActionableComponent actionableComponent = new ActionableComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);

        actionableComponent.key = (String)object.getProperties().get("key");
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                Gdx.app.debug("Level builder","Door Actionated");
                Gdx.app.debug("Level builder","val Flags = 1");
                MapLayerSystem mapLayerSystem = engine.getSystem(MapLayerSystem.class);
                mapLayerSystem.shuffleAlpha(
                        engine.getEntitiesFor(Family.all(MapComponent.class).get()).first());
                engine.getSystem(TextureManipulationSystem.class).shuffleAlpha();

            }
        };

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(actionableComponent);
        entity.flags=flag;
        engine.addEntity(entity);
    }

    private void createIndoor(MapObject object,int flag){
        BoundsComponent boundsComponent = new BoundsComponent();
        ActionableComponent actionableComponent = new ActionableComponent();

        MapRecToWorldRec(object,boundsComponent.bounds);

        actionableComponent.key = (String)object.getProperties().get("key");
        actionableComponent.actionable = new ActionableComponent.Actionable() {
            @Override
            public void action() {
                Gdx.app.debug("Level builder","Indoor Actionated");
                Gdx.app.debug("Level builder","val Flags = 0");
                engine.getEntitiesFor(Family.all(Val_Component.class).get()).first().flags=0;
                engine.getSystem(PhysicsSystem.class).setActiveObjects();
                engine.getSystem(MapLayerSystem.class).shuffleAlpha(
                        engine.getEntitiesFor(Family.all(MapComponent.class).get()).first());
                engine.getSystem(TextureManipulationSystem.class).shuffleAlpha();
            }
        };

        Entity entity = new Entity();
        entity.add(boundsComponent);
        entity.add(actionableComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    private  void createRope(MapObject object,int flag){
        //Get tiles
        //Get coordinates
        float unit = 1/70f;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        TiledMapTileLayer tileLayer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        for(int i = 0;i<(int)h;i++){
            TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x,(int)y+i);
            if(cell==null)continue;

            //Get texture regions
            TextureRegion region = new TextureRegion(cell.getTile().getTextureRegion());
//            TextureRegion region = new TextureRegion(r,0,(int)Constants.inversePPU-(int)h,r.getRegionWidth(),(int)h);
            region.flip(cell.getFlipHorizontally(),cell.getFlipVertically());
            cell.setTile(null);

            //Create Entity
            TransformComponent transform = new TransformComponent();
            BridgeComponent bridgeComponent = new BridgeComponent();
            BodyComponent bodyComponent = new BodyComponent();
            TextureComponent textureComponent = new TextureComponent();
            MovementComponent movementComponent = new MovementComponent();

            transform.pos.set((int)x,(int)y+i,0);
            transform.origin.set(0.0f,0.0f);
            transform.rotation = cell.getRotation();
            textureComponent.region = region;
            bridgeComponent.number = Integer.parseInt((String)object.getProperties().get("number"));

            //Get targets from object property
            String s = (String)object.getProperties().get("target");
            String[] ss = s.split(",");
            float[] coords = new float[ss.length];
            for(int m = 0;m<coords.length;m++){
                coords[m]=Float.parseFloat(ss[m]);
            }
            for(int m = 0;m<coords.length;m+=2){
                bridgeComponent.targets.add(new Vector2(coords[m],coords[m+1]));
            }

            //Create body
            Body body;
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.KinematicBody;
            def.position.set(x+w*0.5f, y+h*0.5f);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(w*0.5f, h*0.5f);
            FixtureDef fix = new FixtureDef();
            fix.isSensor = true;
            fix.shape = shape;
            body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
            body.createFixture(fix);
            body.setUserData(null);
            shape.dispose();

            bodyComponent.body = body;
            bodyComponent.offsetPosition.set(-0.5f,(h*-0.5f)+i);

            Entity entity = new Entity();
            entity.add(transform);
            entity.add(textureComponent);
            entity.add(bridgeComponent);
            entity.add(movementComponent);
            entity.add(bodyComponent);
            entity.flags = flag;
            engine.addEntity(entity);
        }
    }

    private  void createBridge(MapObject object,int flag){
        //Get tiles
        //Get coordinates
        float unit = 1/70f;
        float x,y,w,h;

        Rectangle rectangle =  ((RectangleMapObject)object).getRectangle();
        x = rectangle.x*unit;
        y=rectangle.y*unit;
        w=rectangle.width*unit;
        h=rectangle.height*unit;

        //Create body
        Body body;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.KinematicBody;
        def.position.set(x+w*0.5f, y+h*0.5f);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w*0.5f, h*0.5f);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.friction = 0.5f;
        body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        body.createFixture(fix);
        shape.dispose();
        Rectangle bounds = new Rectangle(0,0,w,h);

        TiledMapTileLayer tileLayer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        for(int i = 0;i<(int)w;i++) {
            TiledMapTileLayer.Cell cell = tileLayer.getCell((int) x + i, (int) y);
            if (cell == null) continue;
            //Get texture regions
            TextureRegion r = new TextureRegion(cell.getTile().getTextureRegion());
            float height = h*Constants.inversePPU;
            TextureRegion region = new TextureRegion(r,0,(int)Constants.inversePPU-(int)height,r.getRegionWidth(),(int)height);
            region.flip(cell.getFlipHorizontally(), cell.getFlipVertically());
            cell.setTile(null);

            //Create Entity
            Entity entity = new Entity();

            TransformComponent transform = new TransformComponent();
            BridgeComponent bridgeComponent = new BridgeComponent();
            BoundsComponent boundsComponent = new BoundsComponent();
            BodyComponent bodyComponent = new BodyComponent();
            TextureComponent textureComponent = new TextureComponent();
            MovementComponent movementComponent = new MovementComponent();

            transform.pos.set( x + i,  y , 0);
            transform.origin.set(0.0f, 0.5f);
            textureComponent.region = region;
            bodyComponent.body = body;
            bodyComponent.body.setUserData(entity);
            bodyComponent.offsetPosition.set((-w * 0.5f) + i, -0.0f);
            bridgeComponent.number = Integer.parseInt((String) object.getProperties().get("number"));
            boundsComponent.bounds.set(bounds);

            //Get targets from object property
            transform.rotation = cell.getRotation();
            String s = (String)object.getProperties().get("target");
            String[] ss = s.split(",");
            float[] coords = new float[ss.length];
            for(int m = 0;m<coords.length;m++){
                coords[m]=Float.parseFloat(ss[m]);
            }
            for(int m = 0;m<coords.length;m+=2){
                bridgeComponent.targets.add(new Vector2(coords[m],coords[m+1]));
            }

            Object property = object.getProperties().get("continuous");
            if(property!=null){
                bridgeComponent.continuous = Boolean.parseBoolean((String)property);
            }

            property = object.getProperties().get("moving");
            if(property!=null){
                bridgeComponent.moving = Boolean.parseBoolean((String)property);
            }


            entity.add(transform);
            entity.add(textureComponent);
            entity.add(bridgeComponent);
            entity.add(boundsComponent);
            entity.add(movementComponent);
            entity.add(bodyComponent);
            entity.flags = flag;
            engine.addEntity(entity);

        }
    }

    private void createFlags(int flag){
        float speed = 0.4f;
        Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();
        Array<StaticTiledMapTile> frames = new Array<StaticTiledMapTile>();
        Array<TiledMapTile> tiles = new Array<TiledMapTile>();

        TiledMapTileSet tileset =  levelMap.getTileSets().getTileSet(Constants.itemsLayersNames[flag]);
        if(tileset==null)return;

        //Select tiles
        for(TiledMapTile tile:tileset){

            Object property = tile.getProperties().get("tag");

            if(property == null) {
                continue;
            }

            String tag = (String)property;
            if(tag.equals("flag")){
                tiles.add(tile);
            }
        }

        //Sort array in a loop ping pong
        int l = (tiles.size*2) - 2;
        TiledMapTile[] tempArray = new TiledMapTile[l];
        for(TiledMapTile tile : tiles){
            int index = Integer.parseInt((String) tile.getProperties().get("frame")) - 1;
            tempArray[index] = tile;
            if(index != 0 && index != tiles.size-1){
                tempArray[l-index]=tile;
            }
        }
        tiles.clear();
        tiles.addAll(tempArray);

        //Add tiles in the correct order to frames
        for(TiledMapTile tile : tiles){
            frames.add(new StaticTiledMapTile(tile.getTextureRegion()));
        }

        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        for(int x = 0; x < layer.getWidth();x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x,y);

                if(cell == null){
                    continue;
                }

                Object property = cell.getTile().getProperties().get("tag");

                if(property == null) {
                    continue;
                }

                String tag = (String)property;

                if(tag.equals("flag")){
                    cell.setTile(new AnimatedTiledMapTile(speed,frames));
                }
            }
        }
    }

    private void setTilesAnimation(String layerName){
        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(layerName);

        for(int x = 0; x < layer.getWidth();x++){
            for(int y = 0; y < layer.getHeight();y++){
                TiledMapTileLayer.Cell cell = layer.getCell(x,y);

                if(cell == null)
                    continue;

                Object property = cell.getTile().getProperties().get("waterTileFrame");

                if(property == null) {
                    continue;
                }

                AnimatedWaterCellComponent component = new AnimatedWaterCellComponent();
                StateComponent state = new StateComponent();

                component.currentCell = cell;

                Entity entity = new Entity();
                entity.add(component);
                entity.add(state);
                engine.addEntity(entity);
            }
        }
    }

    private void createSpring(int flag){
        //Set tile animation
        TiledMapTileSet tileset =  levelMap.getTileSets().getTileSet(Constants.itemsLayersNames[flag]);
        if(tileset==null)return;

        TiledMapTile springNormal = null;
        TiledMapTile springExpanded = null;

        //Select tiles
        for(TiledMapTile tile:tileset){

            Object property = tile.getProperties().get("tag");

            if(property == null) {
                continue;
            }

            String tag = (String)property;
            if(tag.equals("spring")){
                String state = (String)tile.getProperties().get("state");
                if(state.equals("normal")){
                    springNormal = tile;
                }else if(state.equals("expanded")){
                    springExpanded = tile;
                }
            }
        }

        TiledMapTileLayer layer = (TiledMapTileLayer)levelMap.getLayers().get(Constants.itemsLayersNames[flag]);
        for(int x = 0; x < layer.getWidth();x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x,y);

                if(cell == null){
                    continue;
                }

                Object property = cell.getTile().getProperties().get("tag");

                if(property == null) {
                    continue;
                }

                String tag = (String)property;
                if(tag.equals("spring")){
                    SpringComponent springComponent = new SpringComponent();
                    springComponent.normal = springNormal;
                    springComponent.expanded = springExpanded;
                    springComponent.cell=cell;
                    BodyComponent bodyComponent = new BodyComponent();
                    Entity entity = new Entity();

                    //Create body
                    float hw = 0.5f;
                    float hh = 0.25f;

                    BodyDef bodyDef = new BodyDef();
                    bodyDef.position.set(x+hw, y+hh);
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    FixtureDef fixtureDef = new FixtureDef();
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(hw, hh);
                    fixtureDef.shape = shape;
                    fixtureDef.filter.groupIndex = Constants.groupsIndexes[flag];
                    fixtureDef.filter.maskBits = Constants.layerMaskBits[flag];
                    fixtureDef.filter.categoryBits = Constants.layerCategoryBits[flag];

                    bodyComponent.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(bodyDef);
                    bodyComponent.body.createFixture(fixtureDef);
                    bodyComponent.body.setUserData(entity); //Add entity to body user data
                    shape.dispose();


                    entity.flags=flag;
                    entity.add(springComponent);
                    entity.add(bodyComponent);
                    engine.addEntity(entity);
                }
            }
        }
    }

    private void createWall(MapObject object,int flag){
        Entity wall = createRectangle(object,"wall",
                Constants.groupsIndexes[flag],
                Constants.layerCategoryBits[flag],
                Constants.layerMaskBits[flag]);
        wall.flags = flag;
        engine.addEntity(wall);
    }

    private void createGround(MapObject object,int flag) {
        Object shape = object.getProperties().get("shape");
        if (shape==null){
            Gdx.app.error(tag, "Shape is null, entity has not been created");
            return;
        }
        Entity entity = null;
        if(Integer.parseInt((String)(shape))==GroundComponent.POLYGON){
            entity = createPolygon(object,"Ground",Constants.groupsIndexes[flag],
                    Constants.layerCategoryBits[flag],
                    Constants.layerMaskBits[flag]);
            GroundComponent gc = new GroundComponent();
            gc.shape = GroundComponent.POLYGON;
            entity.add(gc);
        }else if(Integer.parseInt((String)(shape))==GroundComponent.RECTANGLE){
            entity = createRectangle(object,"Ground",Constants.groupsIndexes[flag],
                    Constants.layerCategoryBits[flag],
                    Constants.layerMaskBits[flag]);
            GroundComponent gc = new GroundComponent();
            gc.shape = GroundComponent.RECTANGLE;
            entity.add(gc);
        }else{
            entity=new Entity();
            Gdx.app.error(tag, "No shape match");
        }
        entity.flags = flag;
        engine.addEntity(entity);
    }

    /**
     * IMPORTANT Be sure that all rectangles are integer units.
     * Example: [RIGHT] x= 11.0 , y=80.00 , w = 8 , h = 3
     * [WRONG] x = 11.23 , y=80.01 , w = 7.96 , h=3.000001
     * @param object levelMap Oject
     * @return A new entity with a physic component, bounds component and transform component
     */
    private Entity createRectangle(MapObject object,String userData, short groupIndex, short categoryBits, short maskBits) {
        RectangleMapObject rectangle = (RectangleMapObject)object;
        Entity entity = new Entity();
        BoundsComponent bounds = new BoundsComponent();
        TransformComponent transform = new TransformComponent();
        BodyComponent body = new BodyComponent();
        bounds.bounds.set(rectangle.getRectangle());
        bounds.bounds.x *= Constants.PixelsPerUnit;
        bounds.bounds.y *= Constants.PixelsPerUnit;
        bounds.bounds.width *= Constants.PixelsPerUnit;
        bounds.bounds.height *= Constants.PixelsPerUnit;
        transform.pos.set(bounds.bounds.x, bounds.bounds.y, 0);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(transform.pos.x+bounds.bounds.width*0.5f, transform.pos.y+bounds.bounds.height*0.5f);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(bounds.bounds.width*0.5f, bounds.bounds.height*0.5f);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.filter.groupIndex = groupIndex;
        fix.filter.categoryBits = categoryBits;
        fix.filter.maskBits = maskBits;
        body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        body.body.createFixture(fix);
        shape.dispose();
        checkForJoints(object,body.body);

        body.body.setUserData(entity);
        entity.add(bounds);
        entity.add(transform);
        entity.add(body);
        return entity;
    }

    private Entity createRectangle(MapObject object,String userData){
        return createRectangle(object,userData,(short)0,(short)0,(short)0);
    }

    private Entity createPolygon(MapObject object,String userData){
        return createPolygon(object,userData,(short)0,(short)0,(short)0);
    }

    private Entity createPolygon(MapObject object,String userData,short groupIndex,short categoryBits, short maskBits) {
        float unit = Constants.PixelsPerUnit;
        PolygonMapObject poly = (PolygonMapObject)object;
        Entity entity = new Entity();
        PolygonComponent polygon = new PolygonComponent();
        TransformComponent transform = new TransformComponent();
        BodyComponent body = new BodyComponent();

        polygon.bounds.setVertices(poly.getPolygon().getTransformedVertices());
        poly.getPolygon().getTransformedVertices();
        transform.origin.setZero();
        polygon.bounds.setScale(unit,unit);
        polygon.bounds.setPosition(transform.pos.x, transform.pos.y);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(transform.pos.x, transform.pos.y);
        PolygonShape shape = new PolygonShape();
        float[] vertices = new float[polygon.bounds.getVertices().length];
        for(int i = 0; i< vertices.length;i++){
            vertices[i]=polygon.bounds.getVertices()[i]*unit;
        }
        shape.set(vertices);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.filter.groupIndex = groupIndex;
        fix.filter.maskBits = maskBits;
        fix.filter.categoryBits = categoryBits;
        body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        body.body.createFixture(fix);
        shape.dispose();
        body.body.setUserData(entity);
        entity.add(polygon);
        entity.add(transform);
//        entity.add(body);
        return entity;
    }

    private void createCoins() {
        Array<TiledMapTileLayer.Cell> items = new Array<TiledMapTileLayer.Cell>();
        TiledMapTileLayer itemsLayer = (TiledMapTileLayer)levelMap.getLayers().get("Coins");
        for(int x = 0; x < itemsLayer.getWidth();x++){
            for(int y = 0; y < itemsLayer.getHeight();y++){
                TiledMapTileLayer.Cell cell = itemsLayer.getCell(x, y);
                if(cell == null){
                    continue;
                }
                Object property = cell.getTile().getProperties().get("coin");
                if(property!=null){
                    Entity entity = new Entity();
                    CoinComponent coin = new CoinComponent();
                    BoundsComponent bounds = new BoundsComponent();
                    TransformComponent transform = new TransformComponent();
                    AnimationComponent animation = new AnimationComponent();
                    StateComponent stateComponent =  new StateComponent();
                    TextureComponent textureComponent = new TextureComponent();

                    if(Integer.parseInt((String)property) == 1)
                        animation.animations.put(0,Assets.instance.items.coin_gold);
                    else if(Integer.parseInt((String)property) == 2)
                        animation.animations.put(0,Assets.instance.items.coin_silver);
                    else
                        animation.animations.put(0,Assets.instance.items.coin_bronze);

                    stateComponent.set(0);

                    transform.pos.set(x+0.5f, y+0.5f, 0);
                    transform.origin.set(0.5f,0.5f);
                    transform.scale.set(0.4f,0.4f);
                    bounds.bounds.set(x+0.5f, y+0.5f, 0.5f, 0.5f);
                    coin.value = Integer.valueOf((String)property);

                    entity.add(transform);
                    entity.add(bounds);
                    entity.add(coin);
                    entity.add(animation);
                    entity.add(stateComponent);
                    entity.add(textureComponent);
                    engine.addEntity(entity);

                    totalCoins++;
                }
            }
        }
    }

    private void setMap() {
        levelMap.getLayers().get("Coins").setVisible(false);
        engine.getSystem(RenderingSystem.class).loadMap(levelMap);
        Entity entity = new Entity();
        MapComponent mapComponent = new MapComponent();
        mapComponent.map = levelMap;
        entity.add(mapComponent);
        engine.addEntity(entity);
    }

    private void createCamera(Entity target) {
        Entity entity = new Entity();

        CameraComponent camera = new CameraComponent();
        camera.camera = engine.getSystem(RenderingSystem.class).getCamera();
        camera.target = target;
        camera.bounds.set(0,0,(Integer)levelMap.getProperties().get("width"),(Integer)levelMap.getProperties().get("height"));
        entity.add(camera);
        camera.camera.position.set(5,15f,0);
        engine.addEntity(entity);
    }

    private void createCharacter(MapObject object,int flag, String name){
        CharacterComponent.Character character = CharacterComponent.Character.valueOf(name);

        Entity entity = new Entity();
        TextureComponent textureComponent = new TextureComponent();
        AnimationComponent animationComponent = new AnimationComponent();
        StateComponent stateComponent = new StateComponent();
        BoundsComponent boundsComponent = new BoundsComponent();
        TransformComponent transformComponent = new TransformComponent();
        CharacterComponent characterComponent = new CharacterComponent();
        characterComponent.character = character;

        if(object.getProperties().get("conversations")!=null) {
            characterComponent.conversationIDs.addAll(((String) object.getProperties().get("conversations")).split(","));
        }

        Object pX = object.getProperties().get("x");
        Object pY = object.getProperties().get("y");

        String sX = pX.toString();
        String sY = pY.toString();

        float x = Float.parseFloat(sX);
        float y = Float.parseFloat(sY);
        x *= Constants.PixelsPerUnit;
        y *= Constants.PixelsPerUnit;

        transformComponent.pos.set(x,y,1);
        transformComponent.origin.set(0.5f,0);
        getCharacterAnimations(character,animationComponent.animations);
        stateComponent.set(animationComponent.animations.keys().next());

        MapRecToWorldRec(object,boundsComponent.bounds);

        entity.add(transformComponent);
        entity.add(textureComponent);
        entity.add(animationComponent);
        entity.add(stateComponent);
        entity.add(boundsComponent);
        entity.add(characterComponent);
        entity.flags = flag;
        engine.addEntity(entity);
    }

    /**
     * Choose animations for each character
     * @param character
     * @param out Animation ArrayList to avoid new objects
     */
    private void getCharacterAnimations(CharacterComponent.Character character, IntMap<Animation> out){
        switch(character){
            case GOMH:
                out.put(CharacterComponent.LEFT,Assets.instance.gomhAnimations.left);
                out.put(CharacterComponent.RIGHT,Assets.instance.gomhAnimations.right);
                out.put(CharacterComponent.IDLE,Assets.instance.gomhAnimations.center);
                break;
            case PINKY:
                out.put(CharacterComponent.IDLE,Assets.instance.pinkyAnimations.idle);
                out.put(CharacterComponent.TALKING,Assets.instance.pinkyAnimations.despective);
                break;
            case MOM:
                out.put(CharacterComponent.SAD,Assets.instance.momAnimations.sad);
                out.put(CharacterComponent.HAPPY,Assets.instance.momAnimations.happy);
                break;
            case BLUE:
                out.put(CharacterComponent.IDLE,Assets.instance.blueAnimations.idle);
                out.put(CharacterComponent.TALKING,Assets.instance.blueAnimations.talk);
                break;
            case BONNY:
                out.put(CharacterComponent.IDLE,Assets.instance.bonnyAnimations.idle);
                break;
            default:
                Gdx.app.error(tag,"No character found under the name of: "+character);
        }
    }

    private Entity createVal(MapProperties properties,int flag) {
        float x = (Float)properties.get("x") * Constants.PixelsPerUnit;
        float y = (Float)properties.get("y") * Constants.PixelsPerUnit;
        Entity entity = new Entity();
        TextureComponent texc = new TextureComponent();
        Val_Component valc = new Val_Component();
        TransformComponent transc = new TransformComponent();
        MovementComponent mov = new MovementComponent();
        StateComponent state = new StateComponent();
        AnimationComponent anim = new AnimationComponent();
        BoundsComponent bounds = new BoundsComponent();
        BodyComponent body = new BodyComponent();

        transc.pos.set(x,y+0.5f,-1);
        transc.scale.set(0.5f, 0.5f);
        mov.accel.set(0,0);
        mov.velocity.set(0, 0);
        state.set(Val_Component.IDLE);
        bounds.bounds.set(transc.pos.x,transc.pos.y,Val_Component.WIDTH*0.8f,Val_Component.HEIGHT*0.9f);
        bounds.posOffset.y=Val_Component.HEIGHT*0.1f;

        //Body
        BodyDef def = new BodyDef();
        def.fixedRotation = true;
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping=1;
        def.position.set(transc.pos.x, transc.pos.y);
        PolygonShape shape = new PolygonShape();
        float w=0.8f;
        float[] vertices = {0,0,0.2f,-0.2f,0.8f,-0.2f,1,0,1,0.5f,0.8f,0.7f,0.2f,0.7f,0,0.5f};
        for(int i = 0;i<vertices.length;i++){
            float value=vertices[i];
            if(i%2==0){
                value*=w;
                value-=0.5f*w;
            }else{
                value-=0.27f;
            }
            vertices[i]=value;
        }
        shape.set(vertices);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 2.4f;
        fix.friction = 0f;
        fix.filter.groupIndex = Constants.groupsIndexes[flag];
        fix.filter.categoryBits = Constants.PLAYER;
        fix.filter.maskBits = Constants.BOUNDS;

        //Feet
        PolygonShape shape3 = new PolygonShape();
        shape3.setAsBox(Val_Component.WIDTH*0.3f, Val_Component.HEIGHT*0.1f,new Vector2(0,-(Val_Component.HEIGHT * 0.5f) - 0.005f),0);
        FixtureDef fix3 = new FixtureDef();
        fix3.shape = shape3;
        fix3.isSensor=true;
        fix3.density = 0f;
        fix3.friction = 0f;
        fix3.filter.groupIndex = Constants.groupsIndexes[flag];
        fix3.filter.categoryBits = Constants.PLAYER;
        fix3.filter.maskBits = Constants.BOUNDS;

        body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
        Fixture bodyFixture = body.body.createFixture(fix);
        Fixture feetFixture = body.body.createFixture(fix3);
        bodyFixture.setUserData("bodyFixture");
        feetFixture.setUserData("feetFixture");
        body.body.setUserData(entity);
        shape.dispose();

        anim.animations.put(Val_Component.IDLE, Assets.instance.valAssets.idle);
        anim.animations.put(Val_Component.JUMP, Assets.instance.valAssets.jump);
        anim.animations.put(Val_Component.WALKR, Assets.instance.valAssets.walkRight);
        anim.animations.put(Val_Component.WALKL, Assets.instance.valAssets.walkLeft);
        anim.animations.put(Val_Component.HURT, Assets.instance.valAssets.hurt);
        anim.animations.put(Val_Component.CLIMB, Assets.instance.valAssets.climb);

        entity.add(texc);
        entity.add(valc);
        entity.add(transc);
        entity.add(mov);
        entity.add(state);
        entity.add(anim);
        entity.add(bounds);
        entity.add(body);
        entity.flags = flag;
        return entity;
    }

    private void createBackground(){
        Object property = levelMap.getProperties().get("background");
        if(property!=null){
            Json json = new Json();
            String name = (String)property;
            BackgroundLayers backgroundLayers = json.fromJson(BackgroundLayers.class,Gdx.files.internal("backgrounds/"+name));
            for(BackgroundLayers.Layer layer : backgroundLayers.layers){
                TextureComponent textureComponent = new TextureComponent();
                ParallaxComponent parallaxComponent = new ParallaxComponent();
                TransformComponent transformComponent = new TransformComponent();

                Texture texture = Assets.instance.get("backgrounds/"+layer.name, Texture.class);
                float textureWidth = texture.getWidth()*1/70;
                float textureHeight = texture.getHeight()*1/70;
                int mapWidth = (Integer) levelMap.getProperties().get("width");
                int repeatNumber = (int)(mapWidth/textureWidth);
                textureComponent.region = new TextureRegion(texture,texture.getWidth()*repeatNumber,texture.getHeight());
                parallaxComponent.scrollingFactorX = layer.scrollX;
                parallaxComponent.scrollingFactorY = layer.scrollY;

                transformComponent.pos.x = layer.scrollX*(textureWidth+48);
                transformComponent.pos.y = layer.scrollY*(textureHeight+1.8f);
                transformComponent.pos.z = backgroundLayers.layers.size-layer.layer;
                Entity entity = new Entity();
                entity.add(textureComponent);
                entity.add(transformComponent);
                entity.add(parallaxComponent);
                engine.addEntity(entity);
            }
        }
    }

    private TextureRegion captureTile(TiledMapTileLayer tileLayer, MapObject object){
        float x = (Float)object.getProperties().get("x") * Constants.PixelsPerUnit;
        float y = (Float)object.getProperties().get("y") * Constants.PixelsPerUnit;
        float height = (Float) object.getProperties().get("height");

        TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x, (int)y);
        if (cell == null) return null;
        //Get texture regions
        float ty = 0;
        if(cell.getFlipVertically()){
            ty = Constants.inversePPU-height;
        }
        Gdx.app.debug(tag,"ty = "+ty);
        TextureRegion r = cell.getTile().getTextureRegion();
//        TextureRegion region = new TextureRegion(r,0,(int)Constants.inversePPU-(int)height,r.getRegionWidth(),(int)height);
//        r.flip(cell.getFlipHorizontally(), cell.getFlipVertically());
        TextureRegion region = new TextureRegion(r,0,(int)ty,r.getRegionWidth(),(int)height);

        cell.setTile(null);
        return region;
    }

    //Transform a tiled map object in to world coordinates and save it in out
    private void MapRecToWorldRec(MapObject object,Rectangle out){
        RectangleMapObject rectangle = (RectangleMapObject)object;
        out.set(rectangle.getRectangle());
        out.x *= Constants.PixelsPerUnit;
        out.y *= Constants.PixelsPerUnit;
        out.width *= Constants.PixelsPerUnit;
        out.height *= Constants.PixelsPerUnit;
    }

    static class BackgroundLayers{
        public Array<BackgroundLayers.Layer>layers;

        static public class Layer{
            public String name;
            public int layer;
            public float scrollX;
            public float scrollY;
        }
    }

}
