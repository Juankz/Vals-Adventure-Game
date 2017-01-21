package com.epifania.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import com.epifania.components.*;
import com.epifania.utils.Assets;
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
		super(Family.all(TransformComponent.class).one(TextureComponent.class,ParticleEffectComponent.class).get());
		
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
		super.update(deltaTime);
		Gdx.gl.glClearColor(0,0,0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		MapComponent mapComponent = getEngine().getEntitiesFor(Family.all(MapComponent.class).get()).first().getComponent(MapComponent.class);
		TiledMap map = mapComponent.map;

		//Only render back layers
		for(MapLayer layer : map.getLayers()){
			layer.setVisible(true);
		}
		map.getLayers().get("Coins").setVisible(false);
		map.getLayers().get("Items").setVisible(false);
		map.getLayers().get("Builds Front").setVisible(false);
		map.getLayers().get("Builds").setVisible(false);
		renderQueue.sort(comparator);
		cam.update();
		batch.begin();
		//render background first
		for (Entity entity : renderQueue) {
			if (entity.getComponent(ParallaxComponent.class) != null) {
				float scrollX = entity.getComponent(ParallaxComponent.class).scrollingFactorX;
				float scrollY = entity.getComponent(ParallaxComponent.class).scrollingFactorY;
				batch.setProjectionMatrix(cam.calculateParallaxMatrix(scrollX, scrollY));
				drawEntity(entity,deltaTime,false);
			}
		}
		batch.end();
		//Render Back Layers
		mapRenderer.setView(cam);
		mapRenderer.render();
		
		batch.setProjectionMatrix(cam.combined);
		//Render only inner items
		int valFlag = getEngine().getEntitiesFor(Family.all(Val_Component.class).get()).first().flags;
		if(valFlag!=0 || mapComponent.transition) {
			batch.begin();
			for (Entity entity : renderQueue) {
				if (entity.getComponent(ParallaxComponent.class) != null) continue;
				if (entity.flags != 1) continue; //Does not draw an object which is not in the vals active layer
				drawEntity(entity, deltaTime, true);
			}
			batch.end();
		}


		//render front tiled map layers
		for(MapLayer layer : map.getLayers()){
			layer.setVisible(false);
		}
		map.getLayers().get("Items").setVisible(true);
		map.getLayers().get("Builds Front").setVisible(true);
		map.getLayers().get("Builds").setVisible(true);
		mapRenderer.render();

		//render objects in front layers
		if(valFlag==0 || mapComponent.transition) {
			batch.begin();
			for (Entity entity : renderQueue) {
				if (entity.getComponent(ParallaxComponent.class) != null) continue;
				if (entity.flags != 0) continue; //Does not draw an object which is not in the vals active layer
				drawEntity(entity,deltaTime, true);
			}
			batch.end();
		}

		renderQueue.clear();
	}

	private void drawEntity(Entity entity,float delta,boolean cull) {
		//TODO Clean code
		TextureComponent tex = textureM.get(entity);
		ParticleEffectComponent particleComponent = entity.getComponent(ParticleEffectComponent.class);

		//Check if there must be rendered a texture or particle effect or both
		if(tex==null){
			if(particleComponent==null){
				return;
			}else {
				drawEffect(entity,delta);
				return;
			}
		}

		if (tex.region == null) {
			if(entity.getComponent(ParticleEffectComponent.class)!=null){
				drawEffect(entity,delta);
				return;
			}else {
				return;
			}
		}
		//There is a texture, check if a particle effect must be rendered
		if(entity.getComponent(ParticleEffectComponent.class)!=null){
			drawEffect(entity,delta);
		}

		//Render Texture
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

		//draw
		batch.setColor(1,1,1,tex.alpha);
		batch.draw(tex.region, posX-originX, posY-originY,originX,originY,width,height,t.scale.x,t.scale.y,t.rotation);
		batch.setColor(1,1,1,1);


	}

	private void drawEffect(Entity entity,float delta){
		ParticleEffectComponent particleEffectComponent = entity.getComponent(ParticleEffectComponent.class);
		if(particleEffectComponent.particleEffect==null)return;

		ParticleEffectPool.PooledEffect effect = particleEffectComponent.particleEffect;
		effect.draw(batch, delta);
		Gdx.app.debug("Rendering System","rendering effect");
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
