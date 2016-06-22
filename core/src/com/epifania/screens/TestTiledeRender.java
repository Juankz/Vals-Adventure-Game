package com.epifania.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TestTiledeRender extends ScreenAdapter {

	static final int screen_width = (int) (10*16/9);
	static final int screen_height = 10;
	static final float unitscale = 1/70f;
	
	OrthographicCamera camera;
	SpriteBatch batch;
	TiledMap tiledMap;
	TiledMapRenderer mapRenderer;
	BitmapFont font;
	
	@Override
	public void show () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.app.debug("Main","Do something");
		batch = new SpriteBatch();
		camera = new OrthographicCamera(screen_width,screen_height);
		camera.setToOrtho(false, screen_width, screen_height);
		tiledMap = new TmxMapLoader().load("adventure_maps/village.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,unitscale,batch);
		
		font = new BitmapFont();
	}

	private void update(float delta){
		float camSpeed = 5f*delta;
		if(Gdx.app.getType()==Application.ApplicationType.Desktop){
			if(Gdx.input.isKeyPressed(Keys.DPAD_LEFT)){
				camera.position.x -= camSpeed;
			}
			if(Gdx.input.isKeyPressed(Keys.RIGHT)){
				camera.position.x += camSpeed;
			}
			if(Gdx.input.isKeyPressed(Keys.DPAD_UP)){
				camera.position.y += camSpeed;
			}
			if(Gdx.input.isKeyPressed(Keys.DPAD_DOWN)){
				camera.position.y -= camSpeed;
			}
		}
		camera.update();
	}
	
	private void draw() {
		Gdx.gl.glClearColor(1, 1, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.app.debug("",1/Gdx.graphics.getDeltaTime()+"FPS");
		mapRenderer.setView(camera);
		mapRenderer.render();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, Gdx.graphics.getFramesPerSecond()+" FPS", 30, 30);
		batch.end();
	}

	@Override
	public void render(float deltaTime) {
		update(deltaTime);
		draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

}
