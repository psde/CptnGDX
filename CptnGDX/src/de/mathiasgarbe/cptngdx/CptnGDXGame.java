package de.mathiasgarbe.cptngdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CptnGDXGame implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private BitmapFont font;

	private Player player;
	private World world;
	
	private boolean paused;
	
	@Override
	public void create() {			
		Gdx.graphics.setVSync(true);
		
		// Create Ortho 2D Camera, with 0, 0 being top left corner (don't forget to flip textures)
		camera= new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), Gdx.files.internal("data/font.png"), true);	
		
		// Create stuff
		this.batch = new SpriteBatch();
		this.world = new World(batch);
		this.player = new Player(batch, world);
		
		this.paused = false;
	}

	@Override
	public void dispose() {
		this.player.dispose();
		this.world.dispose();
		this.batch.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		paused = true;
	}

	@Override
	public void resume() {
		paused = false;
	}

	@Override
	public void render() {			
		// Update game objects
		float delta = Gdx.graphics.getDeltaTime() * 100;
		
		if(!paused && delta < 20.0f)
		{
			world.update(delta);
			player.update(delta);
		}
		
		// Scrolling offset?
		Vector2 offset = new Vector2(Gdx.graphics.getWidth() / 2 - this.player.position.x, Gdx.graphics.getHeight() / 2  - this.player.position.y);
		
		// Clear screen
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Render game objects
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		world.render(offset, delta);
		player.render(offset, delta);
				
		String scoreString = "Score: " + player.score;
		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		font.draw(batch, scoreString, 10, 10);

		batch.end();
	}
}
