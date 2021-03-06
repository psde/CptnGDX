package de.mathiasgarbe.cptngdx;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "CptnGDX";
		cfg.useGL20 = true;
		cfg.resizable = false;
		cfg.width = 700;
		cfg.height = 500;
		
		new LwjglApplication(new CptnGDXGame(), cfg);
	}
}
