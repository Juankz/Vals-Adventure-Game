package com.epifania.screens;

import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epifania.components.*;
import com.epifania.systems.*;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.epifania.ui.ConversationDialog;
import com.epifania.utils.Assets;
import com.epifania.utils.Constants;
import com.epifania.utils.ConversationManager;
import com.epifania.utils.InputHandler;

public class Test_GamePlay extends ScreenAdapter {
	
	private static final String tag = "TestGameplay";
	SpriteBatch batch;
	BitmapFont debugFont;
	Engine engine;
	TiledMap map;
	Viewport viewport;
	World world;
	GameStates gameState;
	GameStates previousGameState;
	ObjectMap<String,ShaderProgram> shaders;
	private float radius = 0;
	private float[] resolution;
	private InputHandler inputHandler;
	private Stage stage;
	private Skin skin;
	public Button actionButton;
	private ConversationDialog dialog;
	private Array<Entity> entities2Bremoved;
	private ConversationManager conversationManager;

	private float stateTime = 0.0f;

	private int lastCheckpoint = 0;
	
	public Test_GamePlay(SpriteBatch batch) {
		//TODO Use only one cycle for each layer
		//initialize batch and box2d world
		this.batch = batch;
		world = new World(new Vector2(0,-10),true);
		shaders = new ObjectMap<String, ShaderProgram>();
		shaders.put("transition shader",new ShaderProgram(Gdx.files.internal("shaders/vignette.vert"),Gdx.files.internal("shaders/vignette.frag")));
		resolution = new float[2];
		gameState = gameState.RUN;

		entities2Bremoved = new Array<Entity>();

		//Create engine and add com.epifania.systems
		engine = new Engine();
		engine.addSystem(new MovementSystem());
		engine.addSystem(new PhysicsSystem(world));
		engine.addSystem(new Val_System());
		engine.addSystem(new SpringSystem());
		engine.addSystem(new BridgeSystem());
		engine.addSystem(new SwitchSystem());
		engine.addSystem(new CoinSystem());
		engine.addSystem(new AnimationSystem());
		engine.addSystem(new AnimatedTileSystem());
		engine.addSystem(new BoundsSystem());
		engine.addSystem(new AnimatedWaterCellSystem());
		engine.addSystem(new CameraSystem());
		engine.addSystem(new CharacterSystem());
//		engine.addSystem(new ConversationSystem(Gdx.files.internal("scripts/script1.xml")));
//		engine.addSystem(new CollisionSystem(new CollisionSystem.CollisionListener() {
//			@Override
//			public void removeEntity(Entity entity) {
//				entities2Bremoved.add(entity);
//			}
//
//			@Override
//			public void pickObject(TextureRegion region, String key) {
//
//			}
//
//			@Override
//			public void usedObject(String key) {
//
//			}
//
//			@Override
//			public void pickCoin() {
//			}
//
//			@Override
//			public void die() {
//				gameState = GameStates.DEATH;
//				inputHandler.setActive(false);
//				stateTime = 0;
//				engine.getSystem(CollisionSystem.class).setProcessing(false);
//			}
//
//			@Override
//			public void checkpointReached(int number) {
//				if(number>lastCheckpoint)
//					lastCheckpoint = number;
//			}
//		}));
		engine.addSystem(new RenderingSystem(this.batch));
		OrthographicCamera camera = engine.getSystem(RenderingSystem.class).getCamera();
		engine.addSystem(new DebugSystem(camera));
		engine.addSystem(new PhysicsDebugSystem(world,camera));

		//Create viewport
//		float defaultHeight = Gdx.graphics.getHeight();
//		viewport = new FillViewport(defaultHeight*16/9,defaultHeight);

		//Load the tilemap and create game objects based in collected information from the map
		createMap("adventure_maps/prototype.tmx");

		//Create the main character and make the camera follow it
//		Entity val = createVal();
//		createCamera(val);
//		engine.addEntity(val);

		createWorldBounds();
		createCoins();
		createGround();
		createSpring();
		createFlags();
		createBridge();
		createItems();
		setTilesAnimation();
		createBackground();

		//Set defaults for debug com.epifania.systems
		engine.getSystem(DebugSystem.class).setProcessing(false);
		engine.getSystem(PhysicsDebugSystem.class).setProcessing(false);
		if(Gdx.app.getType()== Application.ApplicationType.Android){
			engine.getSystem(DebugSystem.class).setProcessing(false);
			engine.getSystem(PhysicsDebugSystem.class).setProcessing(false);
		}

		//Script Management
		CharacterSystem characterSystem = engine.getSystem(CharacterSystem.class);
		conversationManager = new ConversationManager(Gdx.files.internal("scripts/script1.xml"));
		conversationManager.engine = this.engine;
		characterSystem.manager = conversationManager;

		//Create an input handler with full access to the engine
		inputHandler = new InputHandler(engine);
		characterSystem.inputHandler = inputHandler;
//		engine.getSystem(ConversationSystem.class).inputHandler = inputHandler;
	}

	private  void createWorldBounds(){
		int[] vertices = {
				0,
				0,
				(Integer)map.getProperties().get("width"),
				(Integer)map.getProperties().get("height")
		};

		Body body;
		BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
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

	private void createItems(){
		MapObjects objects = map.getLayers().get("physicLayer").getObjects();
		for(MapObject object : objects){
			Object property = object.getProperties().get("type");
			if(property==null)
				continue;
			String type = (String)property;
			if(type.equals("switch")){
				createSwitch(object);
			}else
			if(type.equals("trigger")){
				String action = (String)object.getProperties().get("action");
				if(action.equals("checkpoint"))
					createCheckpoint(object);
				if(action.equals("die"))
					createDeathZone(object);
			}else
			if(type.equals("character")){
				String name = (String)object.getProperties().get("name");
				if(name.equals("GOMH")){
					createGomh(object);
				}else if(name.equals("VAL")){
					//Create the main character and make the camera follow it
					Entity val = createVal(object.getProperties());;
					createCamera(val);
					engine.addEntity(val);
				}
			}else
			if(type.equals("collectable")){
				createCollectable(object);
			}
			if(type.equals("exit")){
				createExit(object.getProperties());
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
				setState(GameStates.GAMEOVER);
			}
		};

		Entity entity = new Entity();
		entity.add(boundsComponent);
		entity.add(actionableComponent);
		engine.addEntity(entity);
	}

	private void createCollectable(MapObject object){
		MapProperties properties = object.getProperties();
		String key = (String)properties.get("key");

		TextureComponent textureComponent = new TextureComponent();
		TransformComponent transformComponent = new TransformComponent();
		BoundsComponent boundsComponent = new BoundsComponent();
		CollectableComponent collectableComponent = new CollectableComponent();

		if(Boolean.parseBoolean((String)properties.get("captureTile"))){
			//CaptureTile
		}else {
			String atlasRegion = (String)properties.get("texture");
			TextureRegion region = Assets.instance.get("game_objects/items.atlas", TextureAtlas.class).findRegion(atlasRegion);
			if(region!=null){
				textureComponent.region = region;
			}else{
				Gdx.app.debug(tag, "No texture found: "+atlasRegion);
			}
		}

		transformComponent.pos.x = (Float)properties.get("x")*Constants.PixelsPerUnit;
		transformComponent.pos.y = (Float)properties.get("y")*Constants.PixelsPerUnit;
		transformComponent.origin.setZero();

		boundsComponent.bounds.setPosition(transformComponent.pos.x,transformComponent.pos.y);
		boundsComponent.bounds.width = (Float)properties.get("width")*Constants.PixelsPerUnit;
		boundsComponent.bounds.height = (Float)properties.get("height")*Constants.PixelsPerUnit;

		collectableComponent.key = (String)properties.get("key");
		collectableComponent.conversationKey = (String)properties.get("conversationKey");

		Entity entity = new Entity();
		entity.add(transformComponent);
		entity.add(textureComponent);
		entity.add(boundsComponent);
		entity.add(collectableComponent);
		engine.addEntity(entity);
	}

	private void createCheckpoint(MapObject object){
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
		engine.addEntity(entity);
	}

	private void createDeathZone(MapObject object){
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
		engine.addEntity(entity);
	}

	private  void createSwitch(MapObject object){
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

		TiledMapTileLayer tileLayer = (TiledMapTileLayer)map.getLayers().get("Items");
		TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x,(int)y);

		//Select tiles
		TiledMapTileSet tileset =  map.getTileSets().getTileSet("Items");
		for(TiledMapTile tile:tileset){

			Object property = tile.getProperties().get("type");

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
		engine.addEntity(entity);

	}

	private  void createBridge(){
		MapObjects objects = map.getLayers().get("physicLayer").getObjects();
		for(MapObject object : objects){
			Object property = object.getProperties().get("type");
			if(property==null)
				continue;
			String type = (String)property;
			if(type.equals("bridge")){
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
				def.type = BodyType.KinematicBody;
				def.position.set(x+w*0.5f, y+h*0.5f);
				PolygonShape shape = new PolygonShape();
				shape.setAsBox(w*0.5f, h*0.5f);
				FixtureDef fix = new FixtureDef();
				fix.shape = shape;
				body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
				body.createFixture(fix);
				body.setUserData("bridge");
				shape.dispose();

				TiledMapTileLayer tileLayer = (TiledMapTileLayer)map.getLayers().get("Items");
				for(int i = 0;i<(int)w;i++){
					TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x+i,(int)y);
					if(cell==null)continue;
					//Get texture regions
					TextureRegion region = new TextureRegion(cell.getTile().getTextureRegion());
					region.flip(cell.getFlipHorizontally(),cell.getFlipVertically());
					cell.setTile(null);

					//Create Entity
					TransformComponent transform = new TransformComponent();
					BridgeComponent bridgeComponent = new BridgeComponent();
					BodyComponent bodyComponent = new BodyComponent();
					TextureComponent textureComponent = new TextureComponent();
					MovementComponent movementComponent = new MovementComponent();

					transform.pos.set((int)x+i,(int)y,0);
					transform.origin.set(0.0f,0.0f);
					transform.rotation = cell.getRotation();
					textureComponent.region = region;
					bodyComponent.body = body;
					bodyComponent.offsetPosition.set((-w*0.5f)+i,-0.5f);
					bridgeComponent.number = Integer.parseInt((String)object.getProperties().get("number"));

					Entity entity = new Entity();
					entity.add(transform);
					entity.add(textureComponent);
					entity.add(bridgeComponent);
					entity.add(movementComponent);
					entity.add(bodyComponent);
					engine.addEntity(entity);

				}
			}
			if(type.equals("rope")){
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
				def.type = BodyType.KinematicBody;
				def.position.set(x+w*0.5f, y+h*0.5f);
				PolygonShape shape = new PolygonShape();
				shape.setAsBox(w*0.5f, h*0.5f);
				FixtureDef fix = new FixtureDef();
				fix.isSensor = true;
				fix.shape = shape;
				body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
				body.createFixture(fix);
				body.setUserData("rope");
				shape.dispose();

				TiledMapTileLayer tileLayer = (TiledMapTileLayer)map.getLayers().get("Items");
				for(int i = 0;i<(int)h;i++){
					TiledMapTileLayer.Cell cell = tileLayer.getCell((int)x,(int)y+i);
					if(cell==null)continue;

					//Get texture regions
					TextureRegion region = new TextureRegion(cell.getTile().getTextureRegion());
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
					bodyComponent.body = body;
					bodyComponent.offsetPosition.set(-0.5f,(h*-0.5f)+i);
					bridgeComponent.number = Integer.parseInt((String)object.getProperties().get("number"));

					Entity entity = new Entity();
					entity.add(transform);
					entity.add(textureComponent);
					entity.add(bridgeComponent);
					entity.add(movementComponent);
					entity.add(bodyComponent);
					engine.addEntity(entity);

				}
			}
		}
	}

	private void createFlags(){
		float speed = 0.4f;
		Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();
		Array<StaticTiledMapTile> frames = new Array<StaticTiledMapTile>();
		Array<TiledMapTile> tiles = new Array<TiledMapTile>();

		TiledMapTileSet tileset =  map.getTileSets().getTileSet("Items");

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

		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("Items");
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

	private void setTilesAnimation(){
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("GroundBack");

		for(int x = 0; x < layer.getWidth();x++){
			for(int y = 0; y < layer.getHeight();y++){
				TiledMapTileLayer.Cell cell = layer.getCell(x,y);

				if(cell == null)
					continue;

				Object property = cell.getTile().getProperties().get("WaterTileFrame");

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

	private void createSpring(){
		//Set tile animation
		TiledMapTileSet tileset =  map.getTileSets().getTileSet("Items");

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

		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("Items");
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
					Entity entity = new Entity();
					entity.add(springComponent);
					engine.addEntity(entity);

					//Create body
					float hw = 0.5f;
					float hh = 0.25f;

					BodyDef bodyDef = new BodyDef();
					bodyDef.position.set(x+hw, y+hh);
					bodyDef.type = BodyType.StaticBody;
					FixtureDef fixtureDef = new FixtureDef();
					PolygonShape shape = new PolygonShape();
					shape.setAsBox(hw, hh);
					fixtureDef.shape = shape;

					Body body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(bodyDef);
					body.createFixture(fixtureDef);
					body.setUserData(entity); //Add entity to body user data
					shape.dispose();
				}
			}
		}
	}

	private void createGround() {
		MapObjects mapObjects = map.getLayers().get("physicLayer").getObjects();
		for(MapObject object : mapObjects){
			Object property = object.getProperties().get("type");
			if(property == null){
				continue;
			}
			String type = (String)(property);
			if(type.compareTo("ground")!=0){
				continue;
			}
			Object shape = object.getProperties().get("shape");
			Entity entity = null;
			if(Integer.parseInt((String)(shape))==GroundComponent.POLYGON){
				entity = createPolygon(object);
				GroundComponent gc = new GroundComponent();
				gc.shape = GroundComponent.POLYGON;
			}else if(Integer.parseInt((String)(shape))==GroundComponent.RECTANGLE){
				entity = createRectangle(object);
				GroundComponent gc = new GroundComponent();
				gc.shape = GroundComponent.RECTANGLE;
			}else{
				entity=new Entity();
				Gdx.app.error(tag, "No shape match");
			}
			engine.addEntity(entity);
		}
	}

	/**
	 * IMPORTANT Be sure that all rectangles are integer units.
	 * Example: [RIGHT] x= 11.0 , y=80.00 , w = 8 , h = 3
	 * [WRONG] x = 11.23 , y=80.01 , w = 7.96 , h=3.000001
	 * @param object Map Oject
	 * @return A new entity with a physic component, bounds component,ground component and transform component
	 */
	private Entity createRectangle(MapObject object) {
		//Problem maybe that in Tiled the position 0 is the first row and libGDX is the last row
		RectangleMapObject rectangle = (RectangleMapObject)object;
		Entity entity = new Entity();
		BoundsComponent bounds = new BoundsComponent();
		TransformComponent transform = new TransformComponent();
		BodyComponent body = new BodyComponent();
		bounds.bounds.set(rectangle.getRectangle());
		bounds.bounds.x *= 1/70f;
		bounds.bounds.y = (int)(bounds.bounds.y*1/70f);
		bounds.bounds.width *= 1/70f;
		bounds.bounds.height *= 1/70f;
		transform.pos.set(bounds.bounds.x, bounds.bounds.y, 0);
		
		BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		def.position.set(transform.pos.x+bounds.bounds.width*0.5f, transform.pos.y+bounds.bounds.height*0.5f);
//		def.position.set(bounds.bounds.x, bounds.bounds.y);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(bounds.bounds.width*0.5f, bounds.bounds.height*0.5f);
		FixtureDef fix = new FixtureDef();
		fix.shape = shape;
		body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
		body.body.createFixture(fix);
		shape.dispose();

		body.body.setUserData("Ground");
		entity.add(bounds);
		entity.add(transform);
		entity.add(body);
		return entity;
	}

	private Entity createPolygon(MapObject object) {
		float unit = Math.nextUp( 1/70f); // just for the sake of curiosity
		PolygonMapObject poly = (PolygonMapObject)object;
		Entity entity = new Entity();
		PolygonComponent polygon = new PolygonComponent();
		TransformComponent transform = new TransformComponent();
		BodyComponent body = new BodyComponent();
		
		polygon.bounds.setVertices(poly.getPolygon().getVertices());
		transform.pos.x = poly.getPolygon().getBoundingRectangle().x*unit;
		transform.pos.y = poly.getPolygon().getBoundingRectangle().y*unit;
		polygon.bounds.setScale(unit,unit);
		polygon.bounds.setPosition(transform.pos.x, transform.pos.y);
		
		
		BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		def.position.set(transform.pos.x, transform.pos.y);
		PolygonShape shape = new PolygonShape();
		float[] vertices = new float[polygon.bounds.getVertices().length];
		for(int i = 0; i< vertices.length;i++){
			vertices[i]=polygon.bounds.getVertices()[i]*unit;
		}
		shape.set(vertices);
		FixtureDef fix = new FixtureDef();
		fix.shape = shape;
		body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
		body.body.createFixture(fix);
		shape.dispose();
		body.body.setUserData("Ground");
		entity.add(polygon);
		entity.add(transform);
		entity.add(body);
		return entity;
	}

	private void createCoins() {
		Array<TiledMapTileLayer.Cell> items = new Array<TiledMapTileLayer.Cell>();
		TiledMapTileLayer itemsLayer = (TiledMapTileLayer)map.getLayers().get("Coins");
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
	            		bounds.posOffset.set(0.25f,0.20f);
						bounds.posOffset.setZero();
	            		bounds.bounds.set(x+0.5f, y+0.5f, 0.35f, 0.35f);
	            		coin.value = Integer.valueOf((String)property);
	            		
	            		entity.add(transform);
	            		entity.add(bounds);
	            		entity.add(coin);
						entity.add(animation);
						entity.add(stateComponent);
						entity.add(textureComponent);
	            		engine.addEntity(entity);
	            	}
	            }
		 }
	}

	private void createMap(String string) {
		map = new TmxMapLoader().load(string);
		map.getLayers().get("Coins").setVisible(false);
		engine.getSystem(RenderingSystem.class).loadMap(map);
	}

	private void createCamera(Entity target) {
		Entity entity = new Entity();
		
		CameraComponent camera = new CameraComponent();
		camera.camera = engine.getSystem(RenderingSystem.class).getCamera();
		camera.target = target;
		camera.bounds.set(0,0,(Integer)map.getProperties().get("width"),(Integer)map.getProperties().get("height"));
		entity.add(camera);
		
		camera.camera.position.set(5,15f,0);

		viewport = new FillViewport(Constants.ViewportWidth,Constants.ViewportHeight,camera.camera);
		engine.addEntity(entity);
	}

	private void createGomh(MapObject object){
		Entity entity = new Entity();
		TextureComponent textureComponent = new TextureComponent();
		AnimationComponent animationComponent = new AnimationComponent();
		StateComponent stateComponent = new StateComponent();
		BoundsComponent boundsComponent = new BoundsComponent();
		TransformComponent transformComponent = new TransformComponent();
		GomhComponent gomhComponent = new GomhComponent();
		ConversationComponent conversationComponent = new ConversationComponent();
		CharacterComponent characterComponent = new CharacterComponent();

		characterComponent.conversationIDs.addAll(((String)object.getProperties().get("conversations")).split(","));
//		characterComponent.conversationIDs.addAll(
//				getCharacterConversations((String)object.getProperties().get("conversations")));

		conversationComponent.currentCondition = 0;
		conversationComponent.character = "Gomh";

		Object pX = object.getProperties().get("x");
		Object pY = object.getProperties().get("y");

		String sX = pX.toString();
		String sY = pY.toString();

		float x = Float.parseFloat(sX);
		float y = Float.parseFloat(sY);
		x *= Constants.PixelsPerUnit;
		y *= Constants.PixelsPerUnit;

		transformComponent.pos.set(x,y,1);
		transformComponent.scale.set(1f,1f);
		transformComponent.origin.set(0.5f,0);
		animationComponent.animations.put(GomhComponent.LEFT,Assets.instance.gomhAnimations.left);
		animationComponent.animations.put(GomhComponent.RIGHT,Assets.instance.gomhAnimations.right);
		animationComponent.animations.put(GomhComponent.CENTER,Assets.instance.gomhAnimations.center);
		stateComponent.set(GomhComponent.LEFT);

		boundsComponent.bounds.set(x,y,1,1);

		entity.add(transformComponent);
		entity.add(textureComponent);
		entity.add(animationComponent);
		entity.add(stateComponent);
		entity.add(boundsComponent);
		entity.add(gomhComponent);
		entity.add(characterComponent);
		engine.addEntity(entity);

		/*Object properties
		TestGameplay: width:70.0
		TestGameplay: name:Gomh
		TestGameplay: height:70.0
		TestGameplay: x:2730.0
		TestGameplay: y:420.0
		 */
	}

	private Entity createVal(MapProperties properties) {
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
		ConversationComponent conversationComponent = new ConversationComponent();

		conversationComponent.conditions.add("GOMH0");

		transc.pos.set(x,y,-1);
		transc.scale.set(0.5f, 0.5f);
		mov.accel.set(0,0);
		mov.velocity.set(0, 0);
		state.set(Val_Component.IDLE);
		bounds.bounds.set(transc.pos.x,transc.pos.y,Val_Component.WIDTH,Val_Component.HEIGHT);

		BodyDef def = new BodyDef();
		def.fixedRotation = true;
		def.type = BodyType.DynamicBody;
		def.linearDamping=1;
		def.position.set(transc.pos.x, transc.pos.y);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(Val_Component.WIDTH*0.25f, Val_Component.HEIGHT*0.25f,new Vector2(0,Val_Component.HEIGHT*0.25f),0);
		FixtureDef fix = new FixtureDef();
		fix.shape = shape;
		fix.density = 2.5f;
		fix.friction = 0;

		CircleShape shape2 = new CircleShape();
		shape2.setRadius(Val_Component.WIDTH*0.5f);
		shape2.setPosition(new Vector2(0,-Val_Component.WIDTH*0.25f));
		FixtureDef fix2 = new FixtureDef();
		fix2.density = 3f;
		fix2.friction = 0;
		fix2.shape = shape2;

		body.body = engine.getSystem(PhysicsSystem.class).getWorld().createBody(def);
//		body.body.createFixture(fix);
		body.body.createFixture(fix2);
		body.body.setUserData("Val");
		shape.dispose();

		anim.animations.put(Val_Component.IDLE, Assets.instance.valAssets.idle);
		anim.animations.put(Val_Component.JUMP, Assets.instance.valAssets.jump);
		anim.animations.put(Val_Component.WALKR, Assets.instance.valAssets.walkRight);
		anim.animations.put(Val_Component.HURT, Assets.instance.valAssets.hurt);

		entity.add(texc);
		entity.add(valc);
		entity.add(transc);
		entity.add(mov);
		entity.add(state);
		entity.add(anim);
		entity.add(bounds);
		entity.add(body);
		entity.add(conversationComponent);
		return entity;
	}

	private void createBackground(){
		Object property = map.getProperties().get("background");
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
				int mapWidth = (Integer) map.getProperties().get("width");
				int repeatNumber = (int)(mapWidth/textureWidth);
				textureComponent.region = new TextureRegion(texture,texture.getWidth()*repeatNumber,texture.getHeight());
				parallaxComponent.scrollingFactorX = layer.scrollX;
				parallaxComponent.scrollingFactorY = layer.scrollY;

				Gdx.app.debug(tag,"texture width = "+textureWidth*repeatNumber);

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

	private void goToLastCheckpoint(){
		for(Entity checkpoint : engine.getEntitiesFor(Family.all(CheckpointComponent.class,TransformComponent.class).get())) {
			CheckpointComponent checkpointComponent = checkpoint.getComponent(CheckpointComponent.class);
			if(checkpointComponent.number == lastCheckpoint) {
				TransformComponent checkpointTransform = checkpoint.getComponent(TransformComponent.class);
				for (Entity entity : engine.getEntitiesFor(Family.all(Val_Component.class, TransformComponent.class, BodyComponent.class).get())) {
					BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
					MovementComponent movementComponent = entity.getComponent(MovementComponent.class);

					movementComponent.traslation.x = checkpointTransform.pos.x - bodyComponent.body.getPosition().x;
					movementComponent.traslation.y = checkpointTransform.pos.y - bodyComponent.body.getPosition().y;
				}
			}
		}
	}

	private enum GameStates{
		START,RUN,PAUSE,GAMEOVER,DEATH,TRANSITIONIN,TRANSITIONOUT,REARRANGE,EXIT
	}

	@Override
	public void render(float deltaTime) {
		update(deltaTime);
		if(Gdx.input.isKeyJustPressed(Keys.D)&&!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
			boolean debug = !engine.getSystem(DebugSystem.class).checkProcessing();
			engine.getSystem(DebugSystem.class).setProcessing(debug);
		}
		if(Gdx.input.isKeyJustPressed(Keys.D)&&Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
			boolean debug = !engine.getSystem(PhysicsDebugSystem.class).checkProcessing();
			engine.getSystem(PhysicsDebugSystem.class).setProcessing(debug);
		}
		
		engine.update(deltaTime);
		for(Entity entity : entities2Bremoved){
			engine.removeEntity(entity);
		}
		entities2Bremoved.clear();
		
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		//Set shader params
		radius = MathUtils.clamp(radius,0,1);
		shaders.get("transition shader").setUniform2fv("resolution", resolution , 0, 2);
		shaders.get("transition shader").setUniformf("radius", radius);

		//Set debugfont parameters
		int fps = Gdx.graphics.getFramesPerSecond();
		if(fps>55)
			debugFont.setColor(Color.GREEN);
		else if(fps>50)
			debugFont.setColor(Color.YELLOW);
		else
			debugFont.setColor(Color.RED);
		debugFont.draw(batch, Gdx.graphics.getFramesPerSecond()+" FPS", 30, 30);
		batch.end();

		stage.act();
		stage.draw();
	}

	public void update(float deltaTime){
		switch (gameState){
			case START:
				break;
			case RUN:
				break;
			case PAUSE:
				break;
			case GAMEOVER:
				break;
			case DEATH:
				if(stateTime>0.5f){
					setState(GameStates.TRANSITIONOUT);
				}
				break;
			case TRANSITIONIN:
				float transitionInTime = 0.5f;
				radius = stateTime / transitionInTime;
				if (stateTime > transitionInTime) {
					setState(GameStates.RUN);
					batch.setShader(null);//Remove the shader
				}
				break;
			case TRANSITIONOUT:
				float transitionOutTime = 0.5f;
				radius = 1.0f - stateTime / transitionOutTime;

				if (stateTime > transitionOutTime) {
					if(previousGameState.equals(GameStates.GAMEOVER)){
						gameState=GameStates.EXIT;
					}else {
						setState(GameStates.REARRANGE);
					}
				}
				break;
			case REARRANGE:
				float rearrangeTime = 0.5f;
				if(stateTime>rearrangeTime){
					setState(GameStates.TRANSITIONIN);
				}
				break;
			default:
				break;
		}
		stateTime+=deltaTime;
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width,height);
		stage.getViewport().update(width,height);
		resolution[0] = width;
		resolution[1] = height;
	}

	@Override
	public void show() {
		debugFont = new BitmapFont();
		stage = new Stage(new FillViewport(Constants.UIViewportWidth,Constants.UIViewportHeight),batch);
//		stage = new Stage(new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()),batch);
		skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
		//Load UI
		actionButton = new Button(skin,"action");
		actionButton.addListener(new ClickListener(){
			@Override
			public void clicked (InputEvent event, float x, float y) {
				Gdx.app.debug("Stage","Action button Clicked");
				engine.getSystem(CollisionSystem.class).action = true;
			}
		});
		actionButton.setSize(70,70);
		actionButton.setScale(1);
		actionButton.addAction(Actions.forever(Actions.sequence(Actions.scaleBy(0.2f,0.2f,1),Actions.scaleBy(-0.2f,-0.2f,1))));
		actionButton.setVisible(false);
		actionButton.setPosition(0,0);
		actionButton.setOrigin(Align.center);
		engine.getSystem(CollisionSystem.class).actionButton = actionButton;
		stage.addActor(actionButton);
		dialog = new ConversationDialog(skin);
//		engine.getSystem(ConversationSystem.class).dialog = dialog;
		conversationManager.dialog = dialog;
//		dialog.listener = new ConversationDialog.Listener() {
//			@Override
//			public boolean action() {
//				return engine.getSystem(ConversationSystem.class).next();
//			}
//		};
		dialog.listener = new ConversationDialog.Listener() {
			@Override
			public boolean action() {
				return conversationManager.next();
			}
		};
		dialog.setVisible(false);
		stage.addActor(dialog);
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage);
		inputMultiplexer.addProcessor(inputHandler);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {

	}

	@Override
	public  void dispose(){
		while (shaders.iterator().hasNext()){
			shaders.iterator().next().value.dispose();
		}
	}

	static class BackgroundLayers{
		public Array<Layer>layers;

		static public class Layer{
			public String name;
			public int layer;
			public float scrollX;
			public float scrollY;
		}
	}

	public void setState(GameStates state){
		stateTime = 0.0f;
		previousGameState = gameState;
		gameState=state;
		switch (gameState){
			case START:
				break;
			case RUN:
				inputHandler.setActive(true);
				engine.getSystem(CollisionSystem.class).setProcessing(true);
				break;
			case PAUSE:
				break;
			case GAMEOVER:
				setState(GameStates.TRANSITIONOUT);
				break;
			case DEATH:
				break;
			case TRANSITIONIN:
				break;
			case TRANSITIONOUT:
				batch.setShader(shaders.get("transition shader"));
				break;
			case REARRANGE:
				goToLastCheckpoint();
				for (Entity entity : engine.getEntitiesFor(Family.all(Val_Component.class, StateComponent.class).get())) {
					entity.getComponent(StateComponent.class).set(Val_Component.IDLE);
					entity.getComponent(MovementComponent.class).accel.setZero();
				}
				engine.getSystem(Val_System.class).setVelocity(0);
				break;
			default:
				break;
		}
	}

}
