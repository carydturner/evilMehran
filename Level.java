
import java.util.*;
import java.awt.*;
import acm.program.*;
import acm.graphics.*;

public class Level extends GCompound {
	
	private static final int BRICK_WIDTH = 20;
	private static final int BRICK_HEIGHT = 10;
	
	
	public Level() {
		for(int i = 0; i < 3; i++) {
			buildRow(i);
		}
	}
	
	private void buildRow(int i) {
		int x;
		int y;
		for(int j = 0; j <= 60; j++) {
			if(i % 2 == 0) x = 0;
			else x = -1 * (BRICK_WIDTH / 2);
			y = BRICK_HEIGHT * i;
			G3DRect brick = new G3DRect(x + BRICK_WIDTH * j, y, BRICK_WIDTH, BRICK_HEIGHT, true);
			brick.setFilled(true);
			brick.setFillColor(Color.LIGHT_GRAY);
			add(brick);
		}
	}
}
