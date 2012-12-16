package de.mathiasgarbe.cptngdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
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
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class CptnGDXGame implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private BitmapFont font;
	private FrameBuffer fbo;
	private TextureRegion fboRegion;

	private Player player;
	private World world;
		
	private enum GameState 
	{
		PAUSED,
		RUNNING,
		FINISHED
	}
	private GameState gameState;
	
	private long startTime;
	private long endTime;
		
	@Override
	public void create() {			
		Gdx.graphics.setVSync(true);
		
		// Create Ortho 2D Camera, with 0, 0 being top left corner (don't forget to flip textures)
		camera= new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), Gdx.files.internal("data/font.png"), true);
		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);	
		
		// Create stuff
		this.batch = new SpriteBatch();
		this.world = new World(batch);
		this.player = new Player(batch, world);
		
		resetGame();
	}
	
	private void resetGame()
	{
		this.world.loadMap("data/map2.txt");
		this.player.reset(100, 100);
		this.gameState = GameState.RUNNING;
		this.startTime = TimeUtils.millis();
		this.endTime = 0;
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
		//paused = true;
	}

	@Override
	public void resume() {
		//paused = false;
	}
	
	private String formatTime(long millis)
	{
		String minutes = "" + (millis / 1000 / 60);
		String seconds = "" + (millis / 1000) % 60;
		String milliseconds = "" + millis % 1000;

		String str = "";
		for(int i=0; i<2-minutes.length(); i++) str += "0";
		str += minutes + ":";
		for(int i=0; i<2-seconds.length(); i++) str += "0";
		str += seconds + ":";
		for(int i=0; i<3-milliseconds.length(); i++) str += "0";
		str += milliseconds;
		return str;
	}

	@Override
	public void render() {			
		
		if(Gdx.input.isKeyPressed(Keys.P))
		{
			if(gameState == GameState.RUNNING )
			{
				gameState = GameState.PAUSED;
			}
			else if(gameState == GameState.PAUSED)
			{
				gameState = GameState.RUNNING;
			}
		}
		
		
		if(Gdx.input.isKeyPressed(Keys.ENTER) && gameState == GameState.FINISHED)
		{
			resetGame();
		}
		
		
		// Update game objects
		float delta = Gdx.graphics.getDeltaTime() * 100;
		
		if(gameState == GameState.RUNNING )
		{
			if(delta < 20.0f)
			{
				world.update(delta);
				player.update(delta);
			}
			
			if(world.isFinished())
			{
				this.endTime = TimeUtils.millis();
				gameState = GameState.FINISHED;
			}
			
			if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			{
				resetGame();
			}
		}
		
		
		// Scrolling offset
		Vector2 offset = new Vector2(Gdx.graphics.getWidth() / 2 - this.player.position.x, Gdx.graphics.getHeight() / 2  - this.player.position.y);

		// Clear screen
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Render game objects
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		world.render(offset, delta);
		player.render(offset, delta);
				
		if(gameState != GameState.FINISHED)
		{
			// Render and time
			font.draw(batch, "Time: " + formatTime(TimeUtils.millis() - startTime), 10, 10);
			font.draw(batch, "Score: " + player.score, 10, 50);
	
			// Render "paused" if paused
			if(gameState == GameState.PAUSED)
			{
				font.draw(batch, "Paused", 50, 50);
			}
		}
		else
		{
			font.draw(batch, "Level Finished!", 50, 50);
			font.draw(batch, "You took " + formatTime(endTime - startTime) + " seconds.", 50, 100);
			font.draw(batch, "Press ENTER to try again.", 50, 150);
		}
		batch.end();
	}
}
