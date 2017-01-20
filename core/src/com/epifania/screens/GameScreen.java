package com.epifania.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.*;
import com.epifania.components.*;
import com.epifania.systems.*;
import com.epifania.ui.*;
import com.epifania.utils.*;

public class GameScreen extends ScreenAdapter{

    private static final String tag = "Game Screen";
	//Core
	private SpriteBatch batch;
	private TiledMap levelMap;
	private LevelBuilder levelBuilder;
	private Viewport viewport;
	private Engine engine;
	private World world;
	private Array<Entity> entities2Bremoved;
	private Array<Joint> joints2Bdestroyed;
	private ObjectMap<Integer,Joint> joints;
	private ConversationManager conversationManager;

	//UI
	private Stage stage;
	private StageHUD stageHUD;
	private Skin skin;
	private PauseMenu pauseMenu;
	private Button actionButton;
	private Button lockedButton;
	private Button pauseButton;
	private Table coinsIndicator;
	private Image coinImage;
	private Label coinsLabel;
	private ConversationDialog dialog;
	private Container<Image> itemAction;
	private BitmapFont debugFont;
	private HorizontalGroup cargo;
	private boolean stageDebug = true;

	//Music and SFX
	private Music music;

	//i18n
	private I18NBundle bundle_ui;

	//Shadders
	private ObjectMap<String,ShaderProgram> shaders;
	private float radius = 0;
	private float[] resolution;

	//Input
	private InputHandler inputHandler;
	private InputMultiplexer multiplexer;
    private InputController inputController;
    private boolean activeInput = true;

	//GameState vars
	private GameStates gameState;
	private GameStates previousGameState;
	private float stateTime;

	//Game vars
	private int lastCheckpoint = 0;
	private int coinsCollected = 0;
	private int level = 0;
	private int totalCoins = 0;

    //Easy access systems
    private Val_System val_system;

	public GameScreen(SpriteBatch batch, TiledMap map, FileHandle script, int level){
		this.batch = batch;
		this.levelMap = map;
		this.conversationManager = new ConversationManager(script);
		this.level = level;
		init();
	}

	private void init(){

		gameState = GameStates.START;

		//shadders
		shaders = new ObjectMap<String, ShaderProgram>();
		shaders.put("transition shader",new ShaderProgram(Gdx.files.internal("shaders/vignette.vert"),Gdx.files.internal("shaders/vignette.frag")));
		resolution = new float[2];

		//Engine
		engine = new Engine();
		world = new World(new Vector2(0,-10),true);
		addSystemsToEngine();
		engine.getSystem(PhysicsSystem.class).setContactListener(engine.getSystem(CollisionSystem.class));
		entities2Bremoved = new Array<Entity>();
		joints2Bdestroyed = new Array<Joint>();
		joints = new ObjectMap<Integer,Joint>();
		//load level
		levelBuilder = new LevelBuilder(engine,levelListener());
		levelBuilder.loadLevel(levelMap);

		// Other engine settings
		//Debug Settings
		engine.getSystem(DebugSystem.class).setProcessing(false);
		engine.getSystem(PhysicsDebugSystem.class).setProcessing(false);
		if(Gdx.app.getType()== Application.ApplicationType.Android){
			engine.getSystem(DebugSystem.class).setProcessing(false);
			engine.getSystem(PhysicsDebugSystem.class).setProcessing(false);
		}
		engine.getSystem(RenderingSystem.class).setProcessing(true);

		engine.getSystem(BridgeSystem.class).start();

		//Set box2d active objects
		engine.getSystem(PhysicsSystem.class).setActiveObjects();

		//Script Management
		CharacterSystem characterSystem = engine.getSystem(CharacterSystem.class);
		conversationManager.engine = this.engine;
		characterSystem.manager = conversationManager;
		for(Entity val : engine.getEntitiesFor(Family.all(Val_Component.class).get())){
			val.getComponent(Val_Component.class).conversationManager = conversationManager;
		}

		//Initialize InputController
        inputController = new InputController() {
            @Override
            public void left() {
                val_system.setVelocity(-1);
            }

            @Override
            public void right() {
                val_system.setVelocity(1);
            }

            @Override
            public void stopHorizontal() {
                val_system.setVelocity(0);
            }

			@Override
			public void stopVertical() {
				if (val_system.climbing){
					val_system.climb(0);
				}
			}

			@Override
            public void up() {
				Entity val = engine.getEntitiesFor(Family.all(Val_Component.class).get()).first();
				if(val_system.canClimb && !val_system.climbing){
					if(val.getComponent(StateComponent.class).get()!=Val_Component.CLIMB){
						val_system.setState(val,Val_Component.CLIMB);
						val_system.climb(1);
					}
				}else if (val_system.climbing){
					val_system.climb(1);
				}else if(!val_system.isJumping()){
					engine.getSystem(Val_System.class).setJump(true);
				}
            }

            @Override
            public void down() {
				Entity val = engine.getEntitiesFor(Family.all(Val_Component.class).get()).first();
				if(val_system.canClimb && !val_system.climbing){
					if(val.getComponent(StateComponent.class).get()!=Val_Component.CLIMB){
						val_system.setState(val,Val_Component.CLIMB);
						val_system.climb(-1);
					}
				}else if (val_system.climbing){
					val_system.climb(-1);
				}
            }
        };

		//Create an input handler with full access to the engine
        if(Settings.instance.controls){
            inputHandler = new InputHandler(engine,inputController);
        }
        characterSystem.characterListener = new CharacterSystem.CharacterListener() {
			@Override
			public void setActive(boolean b) {
				if(Settings.instance.controls){
					inputHandler.setActive(b);
				}else{
					stageHUD.setActive(b);
				}
			}

			@Override
			public void gameOver() {
				setState(GameStates.GAMEOVER);
			}
		};

		//Add viewport to camera
		OrthographicCamera cam = engine.getSystem(RenderingSystem.class).getCamera();
		viewport = new FillViewport(Constants.ViewportWidth,Constants.ViewportHeight,cam);
	}

	private LevelBuilder.Listener levelListener(){
		return new LevelBuilder.Listener() {
			@Override
			public void exit() {
				setState(GameStates.GAMEOVER);
			}

			@Override
			public void destroyJoint(int id) {
				if(joints.get(id)!=null) {
					joints2Bdestroyed.add(joints.get(id));
					joints.remove(id);
				}
			}

			@Override
			public void addJoint(int id, Joint joint) {
				joints.put(id,joint);
			}
		};
	}

	private void addSystemsToEngine(){
		if(engine==null) engine = new Engine();

		engine.addSystem(new PhysicsSystem(world));
		engine.addSystem(new MovementSystem());
		engine.addSystem(new PlatformSystem());
		engine.addSystem(new TextureManipulationSystem());
		engine.addSystem(val_system = new Val_System());
		engine.addSystem(new SpringSystem());
		engine.addSystem(new BridgeSystem());
		engine.addSystem(new SwitchSystem());
		engine.addSystem(new CoinSystem());
		engine.addSystem(new AnimationSystem());
		engine.addSystem(new AnimatedTileSystem());
		engine.addSystem(new BoundsSystem());
		engine.addSystem(new AnimatedWaterCellSystem());
		engine.addSystem(new TiledMapSystem());
		engine.addSystem(new PasswordSystem());
		engine.addSystem(new CameraSystem());
		engine.addSystem(new CharacterSystem());
        engine.addSystem(new CollisionSystem(new CollisionSystem.CollisionListener() {
        	@Override
			public Image getItemImage(String key){
        		Item item = cargo.findActor(key);
        		return new Image(item.getActor().getDrawable());
			}
            @Override
            public void removeEntity(Entity entity) {
                entities2Bremoved.add(entity);
            }

            @Override
            public void pickCoin() {
				coinsCollected++;
				coinsLabel.setText(String.valueOf(coinsCollected));
				float size = 1.3f;
				float time = 0.04f;
				coinImage.addAction(Actions.sequence(Actions.scaleTo(size,size,time),Actions.delay(time),Actions.scaleTo(1,1,time)));
				SoundManager.playSound("sounds/pickup_coin.ogg");
			}

            @Override
			public void pickObject(TextureRegion region, String key){
				Item item = new Item(skin,region);
				item.key = key;
				item.setName(key);
				cargo.addActor(item);
				cargo.pack();
				cargo.setPosition(
						stageHUD.getWidth() - cargo.getWidth() - 30,
						stageHUD.getHeight() - cargo.getHeight() -30
				);
				SoundManager.playSound("sounds/pickup_object.ogg");
			}

			@Override
			public void usedObject(String key){
				for(int i = 0; i<cargo.getChildren().size;i++){
					Item item = (Item)cargo.getChildren().get(i);
					if(item.key==key){
						cargo.removeActor(cargo.getChildren().get(i));
						break;
					}
				}
				cargo.pack();
				cargo.setPosition(
						stageHUD.getWidth() - cargo.getWidth() - 30,
						stageHUD.getHeight() - cargo.getHeight() -30
				);
			}

			@Override
			public void pickPack(String content, int amount) {
				if(content.equals("coins")){
//					coinsCollected+=amount;
//					coinsLabel.setText(String.valueOf(coinsCollected));
					float size = 1.3f;
					float time = 0.04f;
					float delay = 0.01f;
					coinImage.addAction(Actions.repeat(amount,Actions.sequence(
							Actions.scaleTo(size,size,time),
							Actions.run(new Runnable() {
								@Override
								public void run() {
									coinsCollected++;
									coinsLabel.setText(String.valueOf(coinsCollected));
								}
							}),
							Actions.delay(delay),
							Actions.scaleTo(1,1,time))));
				}
			}

			@Override
            public void die() {
                setState(GameStates.DEATH);
//                inputHandler.setActive(false);
                activeInput = true;
                engine.getSystem(CollisionSystem.class).setProcessing(false);
				SoundManager.playSound("sounds/lose.ogg");
			}

            @Override
            public void checkpointReached(int number) {
                if(number>lastCheckpoint)
                    lastCheckpoint = number;
            }
        }));
		engine.addSystem(new RenderingSystem(this.batch));
		OrthographicCamera camera = engine.getSystem(RenderingSystem.class).getCamera();
		engine.addSystem(new DebugSystem(camera));
		engine.addSystem(new PhysicsDebugSystem(world,camera));
		engine.addSystem(new MapLayerSystem());
	}

	@Override
	public void render(float deltaTime) {
		update(deltaTime);
		viewport.apply();
		engine.update(deltaTime);
		//remove entities
		for(Entity entity : entities2Bremoved){
			if(entity.getComponent(BodyComponent.class)!=null){
				world.destroyBody(entity.getComponent(BodyComponent.class).body);
			}
			engine.removeEntity(entity);
		}
		entities2Bremoved.clear();
		//destroy joints
		for(Joint joint : joints2Bdestroyed){
			if(joint!=null)
				world.destroyJoint(joint);
		}
		joints2Bdestroyed.clear();
		stage.getViewport().apply();
		stage.act();
		stage.draw();
		stageHUD.getViewport().apply();
		stageHUD.act();
		stageHUD.draw();
		drawDebugFont();
		batch.begin();
		//Update shader params
		radius = MathUtils.clamp(radius,0,1);
		shaders.get("transition shader").setUniform2fv("resolution", resolution , 0, 2);
		shaders.get("transition shader").setUniformf("radius", radius);
		batch.end();

		if(gameState.equals(GameStates.EXIT)){
			ScreenManager.getInstance().setScreen(new GameOverScreen(batch,coinsCollected,levelBuilder.totalCoins,level));
		}else if(gameState.equals(GameStates.EXITGAME)){
			ScreenManager.getInstance().setScreen(new MainMenuScreen(batch));
		}
	}

	public void drawDebugFont(){
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		int fps = Gdx.graphics.getFramesPerSecond();
		if(fps>55)
			debugFont.setColor(Color.GREEN);
		else if(fps>50)
			debugFont.setColor(Color.YELLOW);
		else
			debugFont.setColor(Color.RED);
//		debugFont.draw(batch, Gdx.graphics.getFramesPerSecond()+" FPS", Gdx.graphics.getWidth()-60 , 30);
		batch.end();
	}

	public void update(float deltaTime){
		//Check Input for debug purposes
		if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
				boolean debug = !engine.getSystem(DebugSystem.class).checkProcessing();
				engine.getSystem(DebugSystem.class).setProcessing(debug);
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
				boolean debug = !engine.getSystem(PhysicsDebugSystem.class).checkProcessing();
				engine.getSystem(PhysicsDebugSystem.class).setProcessing(debug);
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
				stageDebug = !stageDebug;
				stageHUD.setDebugAll(stageDebug);
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.G) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
				boolean debug = !engine.getSystem(RenderingSystem.class).checkProcessing();
				engine.getSystem(RenderingSystem.class).setProcessing(debug);
			}
		}

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
						gameState = GameStates.EXIT;
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
		stage.getViewport().update(width,height);
		stageHUD.getViewport().update(width,height);
		viewport.update(width,height);
		resolution[0] = width;
		resolution[1] = height;
	}

	@Override
	public void show() {
		bundle_ui = Assets.instance.get("i18n/strings_ui");
		buildUI();
	}

	public void buildUI(){
		debugFont = new BitmapFont();
		stage = new Stage(new FillViewport(Constants.UIViewportWidth,Constants.UIViewportHeight),batch);
//		stage.setDebugAll(true);
        if(!Settings.instance.controls)
            stageHUD = new StageHUD(new ExtendViewport(Constants.UIViewportWidth,Constants.UIViewportHeight),batch,inputController);
        else
            stageHUD = new StageHUD(new ExtendViewport(Constants.UIViewportWidth,Constants.UIViewportHeight),batch);
//		stage = new Stage(new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()),batch);
		skin = Assets.instance.get("user interface/uiskin.json",Skin.class);
		//Load UI
		actionButton = new Button(skin,"action");
		actionButton.addListener(new ClickListener(){
			@Override
			public void clicked (InputEvent event, float x, float y) {
				engine.getSystem(CollisionSystem.class).action = true;
			}
		});
		actionButton.setScale(1);
		actionButton.addAction(Actions.forever(Actions.sequence(Actions.scaleBy(0.2f,0.2f,1),Actions.scaleBy(-0.2f,-0.2f,1))));
		actionButton.setVisible(false);
		actionButton.setPosition(0,0);
		actionButton.setOrigin(Align.center);
		actionButton.setZIndex(1);
		
		lockedButton = new Button(skin,"locked");
		lockedButton.addListener(new ClickListener(){
			@Override
			public void clicked (InputEvent event, float x, float y) {
			}
		});
		lockedButton.setSize(120,120);
		lockedButton.setScale(1);
		lockedButton.addAction(Actions.forever(Actions.sequence(Actions.scaleBy(0.2f,0.2f,1),Actions.scaleBy(-0.2f,-0.2f,1))));
		lockedButton.setVisible(false);
		lockedButton.setPosition(0,0);
		lockedButton.setOrigin(Align.center);
		lockedButton.addListener(new ClickListener(){
			@Override
			public void clicked (InputEvent event, float x, float y) {
				Gdx.app.debug("Stage","Locked button Clicked");
				engine.getSystem(CollisionSystem.class).action = true;
			}
		});

		itemAction = new Container<Image>(new Image());
		itemAction.setVisible(false);
//		itemAction.size(50);
		itemAction.setTransform(true);
		itemAction.setBackground(skin.getDrawable("panel_brown2"));
		itemAction.pack();

		engine.getSystem(CollisionSystem.class).actionButton = actionButton;
		engine.getSystem(CollisionSystem.class).lockedButton = lockedButton;
		engine.getSystem(CollisionSystem.class).itemImage = itemAction;
		stage.addActor(itemAction);
		stage.addActor(actionButton);
		stage.addActor(lockedButton);
		dialog = new ConversationDialog(skin);
		conversationManager.dialog = dialog;
		dialog.listener = new ConversationDialog.Listener() {
			@Override
			public boolean action() {
				return conversationManager.next();
			}
		};
		dialog.setVisible(false);
		stage.addActor(dialog);

		//Build HUD
		cargo = new HorizontalGroup();
		cargo.space(10);

		pauseButton = new Button(skin,"pause");
		pauseButton.setPosition(
				30,
				stageHUD.getHeight() - 30 - pauseButton.getHeight()
		);
		pauseButton.addListener(UI_Utils.clickSound());
		pauseButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				pause();
			}
		});


		coinsLabel = new Label("0",skin,"numeric");
		coinsLabel.setColor(Color.GOLD);

		coinImage = new Image(skin.getDrawable("gold"));
		coinImage.setOrigin(25,25);

		coinsIndicator = new Table();
		coinsIndicator.add(coinImage).size(50).grow().center();
		coinsIndicator.add(coinsLabel).padLeft(20);
		coinsIndicator.pack();
		coinsIndicator.setSize(coinsIndicator.getWidth()+20,coinsIndicator.getHeight()+20);
        coinsIndicator.setPosition(
                stageHUD.getWidth()*0.5f - coinsIndicator.getWidth()*0.5f,
                stageHUD.getHeight() - 50 - coinsIndicator.getHeight()
        );

		pauseMenu = new PauseMenu(new PauseMenu.Listener() {
			@Override
			public void exit() {
				setState(GameStates.EXITGAME);
			}

			@Override
			public void resume1() {
				unpause();
			}
		},bundle_ui);

		pauseMenu.setPosition(stageHUD.getWidth()*0.5f - pauseMenu.getWidth()*0.5f,
				stageHUD.getHeight()*0.5f - pauseMenu.getHeight()*0.5f);
		pauseMenu.setVisible(false);

		if(!Settings.instance.setted){
			final Dialog dialog1 = new Dialog("",skin,"dialog");
			TextButton dialogButton = new TextButton("OK", skin , "longBrown");
			dialogButton.addListener(UI_Utils.clickSound());
			dialogButton.addListener(new ClickListener(){
				 public void clicked (InputEvent event, float x, float y) {
					 dialog1.hide(Actions.fadeOut(0.7f));
				 }
			});
			Label labelD = new Label(bundle_ui.get("changeControlDialog"),skin,"middle");
			labelD.setColor(Color.BROWN);
			labelD.setWrap(true);
			dialog1.getContentTable().add(labelD).width(400).pad(30).padTop(0);
			dialog1.button(dialogButton);
			dialog1.pad(40);
			dialog1.setWidth(500);
			dialog1.pack();
			dialog1.setPosition(stageHUD.getWidth()*0.5f - dialog1.getWidth()*0.5f,
					stageHUD.getHeight()*0.5f - dialog1.getHeight()*0.5f);
			dialog1.setColor(1,1,1,0);

			final Table inputSelectionTable = new Table();

			ControlButton buttons = new ControlButton(new Image(skin.getDrawable("buttons")),bundle_ui.get("buttons").toUpperCase());
			buttons.addListener(UI_Utils.clickSound());
			buttons.setFooterText(bundle_ui.get("control_recom"));
			buttons.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
					onControlSelected(false,inputSelectionTable,dialog1);
					return false;
				}
			});

			ControlButton touch = new ControlButton(new Image(skin.getDrawable("touch")),bundle_ui.get("touch").toUpperCase());
			touch.addListener(UI_Utils.clickSound());
			touch.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
					onControlSelected(true,inputSelectionTable,dialog1);
					return false;
				}
			});

			Table dialog = new Table();
			Label dialogLabel = new Label(bundle_ui.get("chooseControl"),skin,"middle");
			dialogLabel.setColor(0,0,0,1);
			dialog.setBackground(skin.getDrawable("panel_beige_small"));
			dialog.add(dialogLabel);

			float padA = 20;
			inputSelectionTable.add(dialog).colspan(2).fillX().pad(padA);
			inputSelectionTable.row();
			inputSelectionTable.add(buttons).size(buttons.getWidth()).pad(padA);
			inputSelectionTable.add(touch).size(buttons.getWidth()).pad(padA);
			inputSelectionTable.setFillParent(true);
			stageHUD.addActor(inputSelectionTable);
		}

		stageHUD.addActor(cargo);
		stageHUD.addActor(coinsIndicator);
		stageHUD.addActor(pauseButton);
		stageHUD.addActor(pauseMenu);
		stageHUD.setDebugAll(false);

		//Set input processor
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(stageHUD);
        if(Settings.instance.controls)
		    multiplexer.addProcessor(inputHandler);
		Gdx.input.setInputProcessor(multiplexer);
	}

	private void onControlSelected(boolean control, final Table inputSelectionTable, final Dialog dialog1){
		Settings.instance.controls = control;
		Settings.instance.setted = true;
		inputSelectionTable.addAction(Actions.sequence(
				Actions.fadeOut(0.5f, Interpolation.exp5),
				Actions.removeActor()
		));
		dialog1.show(stageHUD,Actions.sequence(
				Actions.delay(0.5f),
				Actions.fadeIn(0.5f, Interpolation.exp5)
		));
	}

	@Override
	public void hide() {
		Assets.instance.unload(Constants.mapsNames[level]);
		Assets.instance.unload("backgrounds/bg.png");
		Assets.instance.unload("backgrounds/bg1.png");
		Assets.instance.unload("backgrounds/bg2.png");
		Assets.instance.unload("sounds/"+levelMap.getProperties().get("music","default_music.ogg",String.class));

		Assets.instance.unload("sounds/doorOpen.ogg");
		Assets.instance.unload("sounds/jump.ogg");
		Assets.instance.unload("sounds/jump_spring.ogg");
		Assets.instance.unload("sounds/lose.ogg");
		Assets.instance.unload("sounds/pickup_coin.ogg");
		Assets.instance.unload("sounds/pickup_object.ogg");
		Assets.instance.unload("sounds/footstep.ogg");
	}

	public void unpause(){
		if(gameState==GameStates.PAUSE){
			setState(previousGameState);
		}
		pauseMenu.setVisible(false);
//		inputHandler.setActive(true);
        activeInput = true;
		multiplexer.addProcessor(0,stage);
	}

	@Override
	public void pause() {
		if(gameState==GameStates.RUN) {
			setState(GameStates.PAUSE);
		}
		pauseMenu.setVisible(true);
		activeInput = false;
		multiplexer.removeProcessor(stage);
	}

	private enum GameStates{
		START,RUN,PAUSE,GAMEOVER,DEATH,TRANSITIONIN,TRANSITIONOUT,REARRANGE,EXIT,EXITGAME
	}

	private void goToLastCheckpoint(){
		//TODO restore coins, platforms, etc after die
		for(Entity checkpoint : engine.getEntitiesFor(Family.all(CheckpointComponent.class,TransformComponent.class).get())) {
			CheckpointComponent checkpointComponent = checkpoint.getComponent(CheckpointComponent.class);
			if(checkpointComponent.number == lastCheckpoint) {
				TransformComponent checkpointTransform = checkpoint.getComponent(TransformComponent.class);
				for (Entity entity : engine.getEntitiesFor(Family.all(Val_Component.class, TransformComponent.class, BodyComponent.class).get())) {
					BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
					MovementComponent movementComponent = entity.getComponent(MovementComponent.class);

					movementComponent.traslation.x = checkpointTransform.pos.x - bodyComponent.body.getPosition().x;
					movementComponent.traslation.y = checkpointTransform.pos.y - bodyComponent.body.getPosition().y + 0.5f;
					bodyComponent.body.setLinearVelocity(0,0);
					entity.flags = checkpoint.flags;
					boolean b = entity.flags==0;
					engine.getSystem(PhysicsSystem.class).setActiveObjects();
					levelMap.getLayers().get("Items").setVisible(b);
					levelMap.getLayers().get("Builds Front").setVisible(b);
					levelMap.getLayers().get("Builds").setVisible(b);
				}
				engine.getSystem(PlatformSystem.class).resetPlatforms();
			}
		}
	}

	public void setState(GameStates state){
		stateTime = 0.0f;
		previousGameState = gameState;
		gameState=state;
		switch (gameState){
			case START:
				//TODO Start animation, story or sequence
				setState(GameStates.RUN);
				break;
			case RUN:
//				inputHandler.setActive(true);
                activeInput = true;
				engine.getSystem(CollisionSystem.class).setProcessing(true);
				break;
			case PAUSE:
				//TODO Set pause settings, IU, etc
				setState(GameStates.RUN);
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
				for (Entity entity : engine.getEntitiesFor(Family.all(Val_Component.class, StateComponent.class).get())) {
					entity.getComponent(StateComponent.class).set(Val_Component.IDLE);
					entity.getComponent(MovementComponent.class).accel.setZero();
				}
				engine.getSystem(Val_System.class).setVelocity(0);
				goToLastCheckpoint();
				break;
			default:
				break;
		}
	}

}
