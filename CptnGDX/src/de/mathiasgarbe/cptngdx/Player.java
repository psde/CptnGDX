package de.mathiasgarbe.cptngdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Player extends GameObject {
	private SpriteBatch batch;
	private World world;
	
	// Player texture
	private Texture playerTexture;
	private List<TextureRegion> player;
	
	// Position and velocity
	public Vector2 position;
	public Vector2 velocity;
	public boolean facingRight; // Sprites are facing to the left, this is used as a flip flag
	
	enum PlayerState
	{
		STANDING,
		RUNNING,
		JUMPING
	}
	public PlayerState state;
	
	// Dust particles
	private ParticleEffect effect;
	private ParticleEffectPool effectPool;
	private Array<PooledEffect> effects;
	
	public int score;
	
	private boolean stillJumping;
	private long jumpStart;
		
	public Player(SpriteBatch batch, World world)
	{
		this.batch = batch;
		this.world = world;
		
		// Load player texture
		this.player = new ArrayList<TextureRegion>();
		this.playerTexture = new Texture(Gdx.files.internal("data/player.png"));
		this.playerTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for(int i=0; i<4; i++)
		{
			TextureRegion playerRegion = new TextureRegion(this.playerTexture, i*50, 0, 50, 50);
			playerRegion.flip(false, true);
			this.player.add(playerRegion);
		}
		
		// Load dust particles
		effects = new Array<PooledEffect>();
		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/dirt.particle"), Gdx.files.internal("data"));
		effect.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		// ParticlEffect.flipY() is not implemented in GWT / html, so I use this workaround.
		// Bug reported, see http://code.google.com/p/libgdx/issues/detail?id=1163
		//effect.flipY();
		for (ParticleEmitter emitter : effect.getEmitters())
			emitter.flipY();
		
		effectPool = new ParticleEffectPool(effect, 1, 2);

		this.reset(0, 0);
	}
	
	public void reset(float x, float y)
	{
		this.position = new Vector2(x, y);
		this.velocity = new Vector2(0, 0);
		this.score = 0;
		this.facingRight = true;
		this.state = PlayerState.JUMPING;
		this.stillJumping = false;
		this.jumpStart = 0;
	}
	
	@Override
	public void dispose()
	{
		playerTexture.dispose();
	}
	
	@Override
	public void update(float delta)
	{
		float step = 1 * delta;
		int speed = 4;
		
		if(world.isColliding(position.x, position.y + 2))
			this.state = PlayerState.STANDING;
		
		
		boolean keyLeft = false;
		boolean keyRight = false;
		boolean keyJump = false;
		
		switch(Gdx.app.getType()) {
			case Android:
				if(Gdx.input.isTouched(0))
				{
					if(Gdx.input.isTouched(1))
					{
						keyLeft = false;
						keyRight = false;
						keyJump = true;
					}
					else
					{
						int x = Gdx.input.getX();
						int y = Gdx.input.getX();
						
						if(x > Gdx.graphics.getWidth() / 2)
						{
							keyLeft = true;
						}
						else
						{
							keyRight = true;
						}
					}
					
				}
				
				break;
				
			default:
			case Desktop:
			case WebGL:
				keyLeft = Gdx.input.isKeyPressed(Keys.RIGHT);
				keyRight = Gdx.input.isKeyPressed(Keys.LEFT);
				keyJump = Gdx.input.isKeyPressed(Keys.UP);
				break;
		}
		
		if(keyLeft)
		{
			this.facingRight = true;
			for(int i=0; i<speed; i++)
			{
				if(!world.isColliding(position.x + step, position.y))
				{
					if(Math.abs(velocity.y) < 0.5 )
						this.state = PlayerState.RUNNING;
					position.add(1 * delta, 0);
					updatePosition();
				}
			}
		}
		else if(keyRight)
		{
			this.facingRight = false;
			for(int i=0; i<speed; i++)
			{
				if(!world.isColliding(position.x - step, position.y))
				{
					if(Math.abs(velocity.y) < 0.5 )
						this.state = PlayerState.RUNNING;
					position.add(-1 * delta, 0);
					updatePosition();
				}
			}
		}
		
		if(keyJump)
		{
			if(world.isColliding(position.x, position.y + 2) && velocity.y == 0)
			{
				//velocity.add(0, -5.5f);
				velocity.y = -7;
				addDustParticles();
				jumpStart = TimeUtils.millis();
				stillJumping = true;
			}
			else if(stillJumping && jumpStart + 150 >= TimeUtils.millis())
			{
				//velocity.add(0, -0.8f);
				velocity.y = -7;
				
			}
		}
		else
		{
			stillJumping = false;
		}
		
		velocity.add(0, 0.5f);
		
		
		if(velocity.y < 0 )
		{
			for(int i=0; i<-velocity.y; i++)
			{
				if(!world.isColliding(position.x, position.y - step))
				{
					this.state = PlayerState.JUMPING;
					position.add(0, -1 * delta);
					updatePosition();
				}
				else
				{
					velocity.y = 0;
					stillJumping = false;
				}
			}
		}
		else if(velocity.y > 0 )
		{
			for(int i=0; i<velocity.y; i++)
			{
				if(!world.isColliding(position.x, position.y + step))
				{
					this.state = PlayerState.JUMPING;
					position.add(0, 1 * delta);
					updatePosition();
				}
				else
				{
					if(velocity.y > 3)
						addDustParticles();
					velocity.y = 0;
				}
			}
		}
		
	}
	
	private void updatePosition()
	{
		if(world.collectGem(position.x, position.y))
			this.score += 10;
	}
	
	@Override
	public void render(Vector2 offset, float delta)
	{
		TextureRegion playerRegion;
		switch(this.state)
		{
			default:
			case STANDING:
				playerRegion = player.get(0);
				break;
			case RUNNING:
				playerRegion = player.get(1 + (TimeUtils.millis() % 200 > 100 ? 0 : 1));
				break;
			case JUMPING:
				playerRegion = player.get(3);
				break;
		}
		if(facingRight)
		{
			batch.draw(playerRegion, offset.x + this.position.x, offset.y + this.position.y, 25, 0, 50, 50, -1, 1, 0);
		}
		else
		{
			batch.draw(playerRegion, offset.x + this.position.x, offset.y + this.position.y);
		}

		// We need to transform the particles back
		Matrix4 transform = new Matrix4();
		for (int i = effects.size - 1; i >= 0; i--) {
			transform.setToTranslation(offset.x, offset.y, 0);
			batch.setTransformMatrix(transform);
	        PooledEffect effect = effects.get(i);
	        effect.draw(batch, delta / 100);
	        if (effect.isComplete()) {
	                effect.free();
	                effects.removeIndex(i);
	        }
		}
		transform.setToTranslation(0, 0, 0);
		batch.setTransformMatrix(transform);
	}
	
	private void addDustParticles()
	{
		PooledEffect effect = effectPool.obtain();
		effect.setPosition(this.position.x + 25, this.position.y + 45);
		effects.add(effect);
	}
}
