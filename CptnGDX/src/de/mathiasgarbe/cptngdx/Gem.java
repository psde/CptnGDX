package de.mathiasgarbe.cptngdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class Gem extends GameObject {
	private SpriteBatch batch;
	
	private Texture gemTexture;
	private TextureRegion gemDrawable;
	
	public Vector2 position;
	private float rotation;
	
	public Gem(SpriteBatch batch, Vector2 position)
	{
		this.batch = batch;
		this.position = position;
		rotation = 0;
		
		this.gemTexture = new Texture(Gdx.files.internal("data/gem.png"));
		this.gemTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		gemDrawable = new TextureRegion(gemTexture, 0, 0, 50, 50);
		gemDrawable.flip(false, true);
	}
	
	public void dispose()
	{
		gemTexture.dispose();
	}
	
	public void update(float delta)
	{
		rotation += delta;
	}
	
	public void render(Vector2 offset, float delta)
	{
		batch.draw(gemDrawable, position.x + offset.x + 7, position.y + offset.y + 7, 25, 25, 50, 50, 0.6f, 0.6f, (float)Math.toDegrees(0.5 * Math.sin(rotation / 30.0)));
	}
}
