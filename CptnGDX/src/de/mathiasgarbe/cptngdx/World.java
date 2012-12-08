package de.mathiasgarbe.cptngdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class World extends GameObject {
	private SpriteBatch batch;
	
	// Tilemap and tiles
	private Texture tilemapTexture;
	private List<TextureRegion> tiles;
	
	// Map definitions
	public enum TileType {
		GRASS,
		DIRT,
		NOTHING
	}
	private TileType[][] map;
	
	// Gems
	private List<Gem> gems;
	
	public World(SpriteBatch batch) {
		this.batch = batch;
		
		// Load tilemap textures
		this.tiles = new ArrayList<TextureRegion>();
		this.tilemapTexture = new Texture(Gdx.files.internal("data/tileset.png"));
		this.tilemapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for(int i=0; i<2; i++)
		{
			TextureRegion tileRegion = new TextureRegion(this.tilemapTexture, i*60, 0, 60, 60);
			tileRegion.flip(false, true);
			this.tiles.add(tileRegion);
		}
		
		loadMap("data/map2.txt");
	}
	
	@Override
	public void dispose()
	{
		tilemapTexture.dispose();
	}
	
	public void loadMap(String fileUrl)
	{
		gems = new ArrayList<Gem>();
		
		// Load Map
		FileHandle file = Gdx.files.internal(fileUrl);
		String mapText = file.readString();
		String[] mapLines = mapText.split("\\r?\\n");
		map = new TileType[mapLines[0].length()][mapLines.length];
		
		// Parse stupid map format
		for(int y=0; y<mapLines.length; y++)
		{
			String mapLine = mapLines[y];
			
			for(int x= 0; x<mapLine.length(); x++)
			{
				char mapTile = mapLine.charAt(x);
				
				TileType tile = TileType.NOTHING;
				if(mapTile == '#')
					tile = TileType.DIRT;
				if(mapTile == '"')
					tile = TileType.GRASS;
				
				if(mapTile == 'x')
				{
					Gem newGem = new Gem(batch, new Vector2(x * 50, y * 50));
					gems.add(newGem);
				}
				
				map[x][y] = tile;
			}
		}
		
		// Second processing step, change dirt to grass if it has access to light
		/*for(int x=1; x<map.length-1; x++)
		{
			for(int y=1; y<map[x].length-1; y++)
			{
				TileType current = map[x][y];
				TileType above = map[x][y-1];
				
				if(current == TileType.DIRT && above == TileType.NOTHING)
					map[x][y] = TileType.GRASS;
			}
		}*/
	}
	
	@Override
	public void update(float delta)
	{
		for(Gem gem : gems)
		{
			gem.update(delta);
		}
	}
	
	@Override
	public void render(Vector2 offset, float delta)
	{
		for(Gem gem : gems)
		{
			gem.render(offset, delta);
		}
		
		for(int x=0; x<map.length; x++)
		{
			for(int y=0; y<map[x].length; y++)
			{
				TileType tile = map[x][y];
				if(tile == TileType.NOTHING)
					continue;
				
				batch.draw(tiles.get(tile.ordinal()), offset.x + x * 50, offset.y + y * 50);
			}
		}
	}
	
	public boolean isMapSolid(float x, float y)
	{	
		int mapX = (int)(x / 50);
		int mapY = (int)((y + 50) / 50);
		
		if(mapX < 0 || mapY < 0 || mapX >= map.length || mapY >= map[0].length)
			return false;
		
		return map[mapX][mapY] != TileType.NOTHING;
	}
	
	public boolean isColliding(float rectX, float rectY)
	{
		Rectangle rect = new Rectangle(rectX, rectY, 50, 50);
		Rectangle tile = new Rectangle();
		for(int x=0; x<map.length; x++)
		{
			for(int y=0; y<map[x].length; y++)
			{
				if(map[x][y] == TileType.NOTHING)
					continue;
				
				tile.set((x * 50) + 5, (y * 50) + 5, 45, 45);
				
				if(tile.overlaps(rect))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean collectGem(float x, float y)
	{
		for(Gem gem : gems)
		{
			if(gem.position.dst(x + 10, y + 10) < 40)
			{
				gems.remove(gem);
				return true;
			}
		}
		return false;
	}
}
