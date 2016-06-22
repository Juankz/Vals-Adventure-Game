package com.epifania.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import com.epifania.components.ParallaxComponent;
import com.epifania.components.TextureComponent;
import com.epifania.components.TransformComponent;
import com.epifania.components.Val_Component;
import com.epifania.utils.Constants;
import com.epifania.utils.ParallaxCamera;

public class RenderingSystem extends IteratingSystem {

	static final float FRUSTUM_WIDTH = Constants.ViewportWidth;
	static final float FRUSTUM_HEIGHT = Constants.ViewportHeight;
	static final float PIXELS_TO_METRES = 1.0f / 70.0f;
	
	private SpriteBatch batch;
	private Array<Entity> renderQueue;
	private Comparator<Entity> comparator;
	private ParallaxCamera cam;
	private TiledMapRenderer mapRenderer;
	private Rectangle cameraViewport = new Rectangle();
	private Rectangle entityBounds = new Rectangle();
	
	private ComponentMapper<TextureComponent> textureM;
	private ComponentMapper<TransformComponent> transformM;
	
	public RenderingSystem(SpriteBatch batch) {
		super(Family.all(TransformComponent.class, TextureComponent.class).get());
		
		textureM = ComponentMapper.getFor(TextureComponent.class);
		transformM = ComponentMapper.getFor(TransformComponent.class);
		
		renderQueue = new Array<Entity>();
		
		comparator = new Comparator<Entity>() {
			@Override
			public int compare(Entity entityA, Entity entityB) {
				return (int)Math.signum(transformM.get(entityB).pos.z -
										transformM.get(entityA).pos.z);
			}
		};
		
		this.batch = batch;
		
		cam = new ParallaxCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		cam.setToOrtho(false, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
	}

	@Override
	public void update(float deltaTime) {
		//TODO Check for Improvements
		super.update(deltaTime);
		Gdx.gl.glClearColor(0,0,0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderQueue.sort(comparator);
		cam.update();
		batch.begin();

		for (Entity entity : renderQueue) {
			if (entity.getComponent(ParallaxComponent.class) != null) {
				float scrollX = entity.getComponent(ParallaxComponent.class).scrollingFactorX;
				float scrollY = entity.getComponent(ParallaxComponent.class).scrollingFactorY;
				batch.setProjectionMatrix(cam.calculateParallaxMatrix(scrollX, scrollY));
				drawEntity(entity,false);
			}
		}
		batch.end();

		mapRenderer.setView(cam);
		mapRenderer.render();
		
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		int valFlag = getEngine().getEntitiesFor(Family.all(Val_Component.class).get()).first().flags;
		for (Entity entity : renderQueue) {
			if (entity.getComponent(ParallaxComponent.class) != null) continue;
			if(entity.flags!=valFlag)continue; //Does not draw an object which is not in the vals active layer
			drawEntity(entity,true);
		}
		batch.end();
		renderQueue.clear();
	}

	private void drawEntity(Entity entity,boolean cull) {
		TextureComponent tex = textureM.get(entity);

		if (tex.region == null) {
			return;
		}

		TransformComponent t = transformM.get(entity);

		float width = tex.region.getRegionWidth()*PIXELS_TO_METRES;
		float height = tex.region.getRegionHeight()*PIXELS_TO_METRES;
		float originX = width * t.origin.x;
		float originY = height * t.origin.y;
		float posX = t.pos.x;
		float posY = t.pos.y;

		//-originY*Math.abs(t.scale.y);

		if(entity.getComponent(Val_Component.class)!=null){
			originY = Val_Component.HEIGHT;
		}
		//execute culling
		if(cull) {
			cameraViewport.set(cam.position.x - cam.viewportWidth * 0.5f, cam.position.y - cam.viewportHeight * 0.5f, cam.viewportWidth, cam.viewportHeight);
			entityBounds.set(posX - originX, posY - originY, width, height);
			if (!cameraViewport.overlaps(entityBounds)) {
				return;
			}
		}

		batch.setColor(1,1,1,tex.alpha);
		batch.draw(tex.region, posX-originX, posY-originY,originX,originY,width,height,t.scale.x,t.scale.y,t.rotation);
		batch.setColor(1,1,1,1);
	}
	@Override
	public void processEntity(Entity entity, float deltaTime) {
		renderQueue.add(entity);
	}
	
	public OrthographicCamera getCamera() {
		return cam;
	}
	
	public void loadMap(TiledMap tiledMap){
		mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,PIXELS_TO_METRES,batch);
	}

}
