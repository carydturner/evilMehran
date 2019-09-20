
import acm.graphics.*;
import java.awt.color.*;
import acm.program.*;
import java.util.*;
import java.awt.*;

public class Ladder extends GCompound {
	
	private static final int LADDER_HEIGHT = 260;
	private static final int LADDER_WIDTH = 65;
	private static final int RUNG_SPACE = 50;
	private static final int THICKNESS = 15;
	private static final int RUNG_OFFSET = 20;

	public Ladder() {
		for(int i = 0; i < 2; i++) {
			G3DRect side = new G3DRect(i * 50, 0, THICKNESS, LADDER_HEIGHT);
			side.setFilled(true);
			side.setColor(Color.DARK_GRAY);
			add(side);
		}
		for(int i = 1; i <= 5; i++) {
			G3DRect rung = new G3DRect(0, i * RUNG_SPACE - RUNG_OFFSET, LADDER_WIDTH, THICKNESS);
			rung.setFilled(true);
			rung.setColor(Color.DARK_GRAY);
			add(rung);
		}
		GRect sensor = new GRect(0, 0, LADDER_WIDTH, LADDER_HEIGHT);
		sensor.setFilled(true);
		sensor.setVisible(false);
		add(sensor);
	}
}
