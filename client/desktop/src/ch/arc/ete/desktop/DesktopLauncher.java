package ch.arc.ete.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ch.arc.ete.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Mini Games";
		config.height = 768;
		config.width = 1024;
		config.resizable = false;
		//config.fullscreen = true;
		new LwjglApplication(new Main(), config);
	}
}
