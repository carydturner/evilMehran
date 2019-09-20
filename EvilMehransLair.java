/*
 * File: EvilMehransLair.java
 * ---------------------
 * Student: Cary Turner
 * Section Leader: Thapelo Sebolai
 */

import java.awt.Color;
import java.awt.event.*;
import java.io.File;

import acm.program.*;
import acm.graphics.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import acm.util.*;

/*
 * This program runs "Evil Mehran's Lair", and old school arcade-style game
 * featuring all original graphics and music.
 */
public class EvilMehransLair extends GraphicsProgram {
	
	/* Evil Mehran's Lair Constants */
	
	private static final int APPLICATION_WIDTH = 1000;
	private static final int APPLICATION_HEIGHT = 760;
	
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;
	
	private static final int LEVEL_OFFSET = -5;
	private static final int FIRST_LEVEL_VERTICAL_OFFSET = 50;
	private static final int DELAY = 10;
	private static final int BULLET_WIDTH = 10;
	private static final int BULLET_HEIGHT = 3;
	private static final int BULLET_SPEED = 8;
	private static final int FIREBALL_SPEED = 5;
	private static final int LASER_WIDTH = 30;
	private static final int LASER_HEIGHT = 5;
	private static final int LIFE_BAR_WIDTH = 90;
	private static final int LIFE_BAR_HEIGHT = 10;
	private static final int LIFE_BAR_OFFSET = 18;
	private static final int PLAYER_X_MOTION = 12;
	private static final int PLAYER_Y_MOTION = 23;
	
	/* Enumeration */
	private static final int DRAGON_ONE = 0;
	private static final int DRAGON_TWO = 1;
	private static final int MEHRAN = 2;
	
	/* Difficulty / number of lives for each of the monsters */
	private static final int DRAGON_ONE_LIVES = 3;
	private static final int DRAGON_TWO_LIVES = 4;
	private static final int MEHRAN_LIVES = 6;
	
	
	/* Initializes the program and sets up the canvas */
	
	public void init() {
		
		setSize(WIDTH, HEIGHT);
		Color backgroundColor = new Color(60, 0, 60);
		setBackground(backgroundColor);
		intro = new GImage("EvilMehranIntroGraphic.png");
		intro.setLocation(0,0);
		add(intro);
		playMusic(new File("EvilMehransLairThemeMusic.wav"));
		initAnimationArray();
		addKeyListeners();
		addMouseListeners();
		waitForClick();
		buildWorld();
	}
	
	/*
	 * Runs the program as long as gameOver() returns false. Displays results when gameOver()
	 * returns true.
	 */
	public void run() {
		count = 270;
		while(!gameOver()) {
			moveGame();
		}
		if (laser != null)
			remove(laser);
		displayResults();
	}
	
	/*
	 * Moves each element in the world one "frame".
	 */
	private void moveGame() {
		moveFireball1();
		moveFireball2();
		moveMehran();
		moveLaser();
		moveBullet();
		checkPlayerCollisions();
		checkForBulletCollisions();
		pause(DELAY);
		count++;
	}
	
	/*
	 * Performs the appropriate action for each KeyEvent that occurs while the game is running.
	 */
	public void keyPressed(KeyEvent e) {
		if(!gameOver()) {
			
			GObject top = getElementAt(player.getX() + player.getWidth(), player.getY() - 16);
			GObject bottom = getElementAt(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() + 16);
			GObject bottomRight = getElementAt(player.getX() + player.getWidth() + 1, player.getY() + player.getHeight() * .8);
			GObject bottomLeft = getElementAt(player.getX() - 1, player.getY() + player.getHeight() * .8);
			GObject topRight = getElementAt(player.getX() + player.getWidth() + 1, player.getY());
			GObject topLeft = getElementAt(player.getX() - 1, player.getY());
			
			switch(e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
				moveRight(bottom);
				break;
			case KeyEvent.VK_LEFT:
				moveLeft(bottom);
				break;
			case KeyEvent.VK_UP:
				if ((top == null || (bottom == ladder1 || bottom == ladder2 || top == ladder1 || top == ladder2)) && player.getY() > 23)
					player.move(0, -PLAYER_Y_MOTION);
				break;
			case KeyEvent.VK_DOWN:
				if (bottom == null || (bottom == ladder1 || bottom == ladder2))
					player.move(0, PLAYER_Y_MOTION);
				break;
			case KeyEvent.VK_SPACE:
				if(bullet == null && bottom != null && bottomLeft == null && topRight == null && bottomRight == null && topLeft == null)
					shoot(bottom, bottomLeft, topRight, bottomRight, topLeft);
				break;
			}
		}
	}
	
	/*
	 * Adds a bullet to the screen if the player is in the appropriate position (ie not in the air or on a ladder).
	 */
	private void shoot(GObject bottom, GObject bottomLeft, GObject topRight, GObject bottomRight, GObject topLeft) {
		//if(bullet == null && bottom != null && bottomLeft == null && topRight == null && bottomRight == null && topLeft == null) {
			{bullet = new GRect(BULLET_WIDTH, BULLET_HEIGHT);
			bullet.setFilled(true);
			bullet.setColor(Color.green);
			if(facingEast == true) {
				bulletVelocity = BULLET_SPEED;
				add(bullet, player.getX() + player.getWidth() + 1, player.getY() + player.getHeight() * 3/5.0);
			} else {
				bulletVelocity = -BULLET_SPEED;
				add(bullet, player.getX() - BULLET_WIDTH, player.getY() + player.getHeight() * 3/5.0);
			}
		}
	}
	
	/*
	 * Moves the player left by PLAYER_X_MOTION pixels, as long as the motion won't put the player outside the world.
	 * If the player is facing East, the player is first removed and replaced facing West before moving left.
	 */
	private void moveLeft(GObject bottom) {
		if(facingEast) {
			double x = player.getX();
			double y = player.getY();
			remove(player);
			player = new GImage("PlayerWest.png");
			player.scale(.75);
			add(player, x, y);
			facingEast = false;
		}
		if(player.getX() > 5 && bottom != null)
			player.move(-PLAYER_X_MOTION, 0);
	}
	
	/*
	 * Moves the player right by PLAYER_X_MOTION pixels, as long as the motion won't put the player outside the world.
	 * If the player is facing West, the player is first removed and replaced facing East before moving right.
	 */
	private void moveRight(GObject bottom) {
		if(!facingEast) {
			double x = player.getX();
			double y = player.getY();
			remove(player);
			player = new GImage("PlayerEast.png");
			player.scale(.75);
			add(player, x, y);
			facingEast = true;
		}
		if(player.getX() < getWidth() - player.getWidth() - 5 && bottom != null)
			player.move(PLAYER_X_MOTION, 0);
	}
	
	/*
	 * Moves the bullet horizontally by one unit.
	 */
	private void moveBullet() {
		if(bullet != null) {
			bullet.move(bulletVelocity, 0);
		}
	}
	
	/*
	 * Checks for bullet collisions with other objects and checks to see if the bullet has moved off screen.
	 */
	private void checkForBulletCollisions() {
		bulletMoveOffScreen();
		bulletCollisionWithObject();
	}
	
	/*
	 * Removes the bullet if it moves off screen.
	 */
	private void bulletMoveOffScreen() {
		if(bullet != null && (bullet.getX() > getWidth() || bullet.getX() < 0)){
			remove(bullet);
			bullet = null;
		}
	}
	
	/*
	 * Removes the passed GObject parameter if it moves off screen.
	 */
	private void moveOffScreen(GObject element) {
		if (element != null && (element.getX() > getWidth()|| element.getX() + element.getWidth() < 0)) {
			remove(element);
			element = null;
		}
	}
	
	/*
	 * Checks for player collisions with the monsters. If the player touches either of the dragons, they immediately
	 * lose all their lives and lose the game.
	 */
	private void checkPlayerCollisions() {
		GObject left = getElementAt(player.getX() - 1, player.getY() + player.getHeight() / 2.0);
		GObject right = getElementAt(player.getX() + player.getWidth() + 1, player.getY() + player.getHeight() / 2.0);
		if(dragon1 != null && (left == dragon1 || right == dragon1)) {
			remove(lifeBar);
			lifeBar = null;
		}
		if(dragon2 != null && (left == dragon2 || right == dragon2)) {
			remove(lifeBar);
			lifeBar = null;
		}
	}
	
	/*
	 * Checks for bullet collisions with other objects. If the bullet collides with either dragon or Evil Mehran,
	 * then that object receives one hit and loses life.
	 */
	private void bulletCollisionWithObject() {
		if(bullet != null) {
			GObject collider = getElementAt(bullet.getX() -1 , bullet.getY() + 1);
			if (dragon1 != null && collider == dragon1) {
				hit(dragon1);
			}
			if(dragon2 != null && collider == dragon2) {
				hit(dragon2);
			}
			if(mehran != null && collider == mehran && bullet.getY() > mehran.getY() + mehran.getHeight() / 2.0) {
				hit(mehran);
			}
		}
	}
	
	/*
	 * Moves fireball1 horizontally by one unit given by FIREBALL_SPEED.
	 */
	private void moveFireball1() {
		if(count == 280 && dragon1 != null) {
			fireball1 = new GImage("fireball1.png");
			add(fireball1, dragon1.getX() - fireball1.getWidth(), dragon1.getY() + 10);
			count = 0;
		}
		if(fireball1 != null) {
			fireball1.move(-FIREBALL_SPEED, 0);
			//checkWeaponCollisions(fireball1);
			fireball1Collisions();
			moveOffScreen(fireball1);
		}
	}
	
	/*
	 * Moves fireball2 horizontally by one unit given by FIREBALL_SPEED.
	 */
	private void moveFireball2() {
		if(dragon1 == null && dragon2 != null && count == 230) {
			fireball2 = new GImage("fireball2.png");
			add(fireball2, dragon2.getX() + dragon2.getWidth(), dragon2.getY() + 10);
			count = 0;
		}
		if(fireball2 != null) {
			fireball2.move(FIREBALL_SPEED, 0);
			//checkWeaponCollisions(fireball2);
			fireball2Collisions();
			moveOffScreen(fireball2);
		}
	}
	
	/*
	 * This was my failed attempt at creating a generic method that could check for collisions with all the fireballs
	 * and lasers in the world by passing those objects in as parameters. Unfortunately, it created all sorts of awful
	 * bugs when implemented this way, so please excuse my horribly redundant code in the next few methods!
	 */
	private void checkWeaponCollisions(GObject element) {
		GObject topLeft = getElementAt(element.getX() - 1, element.getY());
		GObject topRight = getElementAt(element.getX() + element.getWidth() + 1, element.getY());
		GObject bottomLeft = getElementAt(element.getX() - 1, element.getY() + element.getHeight());
		GObject bottomRight = getElementAt(element.getX() + element.getWidth() + 1, element.getY() + element.getHeight());
		if(topLeft == player || topRight == player || bottomLeft == player || bottomRight == player) {
			remove(element);
			element = null;
			hits++;
			loseLife();
		}
	}
	
	/*
	 * Checks for fireball1 collisions with the player. If fireball1 collides with the player, the fireball is removed
	 * and the player loses a life.
	 */
	private void fireball1Collisions() {
		GObject topLeft = getElementAt(fireball1.getX() - 1, fireball1.getY());
		GObject bottomLeft = getElementAt(fireball1.getX() - 1, fireball1.getY() + fireball1.getHeight());
		GObject topRight = getElementAt(fireball1.getX() + fireball1.getWidth() + 1, fireball1.getY());
		GObject bottomRight = getElementAt(fireball1.getX() + fireball1.getWidth() + 1, fireball1.getY() + fireball1.getHeight());
		if(topLeft == player || bottomLeft == player || topRight == player || bottomRight == player) {
			remove(fireball1);
			fireball1 = null;
			hits++;
			loseLife();
		}
	}
	
	/*
	 * Checks for fireball2 collisions with the player. If fireball1 collides with the player, the fireball is removed
	 * and the player loses a life.
	 */
	private void fireball2Collisions() {
		GObject topLeft = getElementAt(fireball2.getX() - 1, fireball2.getY());
		GObject topRight = getElementAt(fireball2.getX() + fireball2.getWidth() + 1, fireball2.getY());
		GObject bottomLeft = getElementAt(fireball2.getX() - 1, fireball2.getY() + fireball2.getHeight());
		GObject bottomRight = getElementAt(fireball2.getX() + fireball2.getWidth() + 1, fireball2.getY() + fireball2.getHeight());
		if(topLeft == player || topRight == player || bottomLeft == player || bottomRight == player) {
			remove(fireball2);
			fireball2 = null;
			hits++;
			loseLife();
		}
	}
	
	/*
	 * Checks for laser collisions with the player. If fireball1 collides with the player, the laser is removed
	 * and the player loses a life.
	 */
	private void laserCollisions() {
		GObject topLeft = getElementAt(laser.getX() - 1, laser.getY());
		GObject topRight = getElementAt(laser.getX() + laser.getWidth() + 1, laser.getY());
		GObject bottomLeft = getElementAt(laser.getX() - 1, laser.getY() + laser.getHeight());
		GObject bottomRight = getElementAt(laser.getX() + laser.getWidth() + 1, laser.getY() + laser.getHeight());
		if(topLeft == player || topRight == player || bottomLeft == player || bottomRight == player) {
			remove(laser);
			laser = null;
			hits++;
			loseLife();
		}
	}
	
	/*
	 * Initiates the Evil Mehran animation array by adding each of the Evil Mehran images.
	 */
	private void initAnimationArray() {
		animationArr = new GObject[8];
		for(int i = 1; i < animationArr.length; i++) {
			String fileName = "EvilMehran" + i + ".png";
			animationArr[i] = new GImage(fileName);
		}
	}
	
	/*
	 * Uses the Evil Mehran animation array to animate the motion of Mehran lowering and raising his lightsaber
	 * and shooting a laser out of the end.
	 * 
	 * PS: I know that lightsabers don't actually shoot lasers.
	 */
	private void moveMehran() {
		if(count == 300 && dragon2 == null && mehran != null) {
			double x = mehran.getX();
			double y = mehran.getY();
			for(int i = 1; i < animationArr.length; i++) {
				remove(mehran);
				mehran = animationArr[i];
				add(mehran, x, y);
				moveBullet();
				pause(DELAY);
			}
			laser = new GRect(mehran.getX() - 1, mehran.getY() + 100, LASER_WIDTH, LASER_HEIGHT);
			laser.setFilled(true);
			laser.setColor(Color.RED);
			add(laser);
			for(int i = animationArr.length - 1; i > 0; i--) {
				remove(mehran);
				mehran = animationArr[i];
				add(mehran, x, y);
				moveBullet();
				pause(DELAY);
			}
			count = 0;
		}
	}
	
	/*
	 * Moves the laser one unit determined by FIREBALL_SPEED.
	 */
	private void moveLaser() {
		if(laser != null) {
			laser.move(-FIREBALL_SPEED, 0);
			//checkWeaponCollisions(laser);
			laserCollisions();
			moveOffScreen(laser);
		}
	}
	
	/*
	 * Docks the life of the hit element by one. If the life of the hit element is less than one,
	 * that element is removed from the game.
	 */
	private void hit(GObject element) {
		remove(bullet);
		bullet = null;
		hitGraphic(element);
		if(element == dragon1) {
			lifeArray[DRAGON_ONE]--;
			if(lifeArray[DRAGON_ONE] < 1) {
				remove(dragon1);
				dragon1 = null;
			}
		}
		if(element == dragon2) {
			lifeArray[DRAGON_TWO]--;
			if(lifeArray[DRAGON_TWO] < 1) {
				remove(dragon2);
				dragon2 = null;
			}
		}
		if(element == mehran) {
			lifeArray[MEHRAN]--;
			if(lifeArray[MEHRAN] < 1) {
				remove(mehran);
				mehran = null;
			}
		}
	}
	
	/*
	 * Creates a flashing effect of the hit element to illustrate that it has been hit.
	 */
	private void hitGraphic(GObject element) {
		for(int i = 0; i < 10; i++) {
			element.setVisible(true);
			moveGame();
			element.setVisible(false);
			moveGame();
			element.setVisible(true);
		}
	}
	
	/*
	 * Docks player life by 1 and updates the lifeBar display appropriately.
	 */
	private void loseLife() {
		hitGraphic(player);
		if (hits ==1) {
			lifeBar.scale(.67, 1);
			lifeBar.setColor(Color.ORANGE);
		} else if (hits == 2) {
			lifeBar.scale(.5, 1);
			lifeBar.setColor(Color.RED);
		} else if (hits == 3) {
			remove(lifeBar);
			lifeBar = null;
		}
	}
	
	/*
	 * Displays the results at the end of the game. The graphics displayed depend on whether
	 * the player won or lost the game.
	 */
	private void displayResults() {
		GImage results;
		if(mehran == null ) {
			results = new GImage("WinImage.png");
			results.scale(.7);
		} else {
			results = new GImage("LoseImage.png");
			results.scale(1.5);
		}
		results.setLocation((getWidth() - results.getWidth()) / 2.0, (getHeight() - results.getHeight()) / 2.0);
		add(results);
	}
	
	/*
	 * Opens and plays Evil Mehran's Lair Theme Music.
	 */
	private void playMusic(File filepath) {
		try {
		    AudioInputStream song = AudioSystem.getAudioInputStream(filepath);
			AudioFormat format = song.getFormat();
		    DataLine.Info info = new DataLine.Info(Clip.class, format);
		    Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(song);
		    clip.start();
		}
		catch (Exception ex) {
			throw new ErrorException(ex);
		}
	}
	
	/*
	 * Returns true if the game is over.
	 */
	private boolean gameOver() {
		return (lifeBar == null  || mehran == null);
	}
	
	/*
	 * Builds the world by adding all the levels, ladders, and characters.
	 */
	private void buildWorld() {
		removeAll();
		intro = null;
		facingEast = true;
		
		addLevels();
		addCharacters();
		addLifeDisplay();
	}
	
	/*
	 * Adds each of the three levels.
	 */
	private void addLevels() {
		level1 = new Level();
		add(level1, LEVEL_OFFSET, getHeight() - FIRST_LEVEL_VERTICAL_OFFSET);
		
		level2 = new Level();
		add(level2, LEVEL_OFFSET, (level1.getY() * (2/3.0)) - level2.getHeight() / 2.0);
		
		level3 = new Level();
		add(level3, LEVEL_OFFSET, (level1.getY() * (1/3.0)) - level3.getHeight() / 2.0);
		
		ladder1 = new Ladder();
		add(ladder1, getWidth() - ladder1.getWidth() * 1.5, level2.getY());
		
		ladder2 = new Ladder();
		add(ladder2, ladder2.getWidth() / 2.0, level3.getY());
	}
	
	/*
	 * Adds all of the characters to the world.
	 */
	private void addCharacters() {
		player = new GImage("PlayerEast.png");
		player.scale(.75);
		add(player, player.getWidth() / 2.0, level1.getY() - player.getHeight());
		
		dragon1 = new GImage("Dragon1.png");
		add(dragon1, getWidth() - dragon1.getWidth() * 1.5, level1.getY() - dragon1.getHeight());
		
		dragon2 = new GImage("Dragon2.png");
		add(dragon2, dragon2.getWidth() / 2.0, level2.getY() - dragon2.getHeight());
		
		GObject karel = new GImage("karel.png");
		add(karel, getWidth() - karel.getWidth(), level3.getY() - karel.getHeight());
		
		mehran = new GImage("EvilMehran1.png");
		add(mehran, getWidth() - mehran.getWidth() - karel.getWidth(), level3.getY() - mehran.getHeight());
	}
	
	/*
	 * Adds the life display to the upper left corner of the screen.
	 */
	private void addLifeDisplay() {
		GImage lifeDisplay = new GImage("PlayerEast.png");
		lifeDisplay.scale(.3);
		add(lifeDisplay, 0, 0);
		lifeBar = new GRect(lifeDisplay.getWidth(), LIFE_BAR_OFFSET, LIFE_BAR_WIDTH, LIFE_BAR_HEIGHT);
		lifeBar.setFilled(true);
		lifeBar.setColor(Color.GREEN);
		add(lifeBar);
	}
	
	/* Private Instance Variables */
	private int bulletVelocity;
	private int hits = 0;
	private int[] lifeArray = {DRAGON_ONE_LIVES, DRAGON_TWO_LIVES, MEHRAN_LIVES};
	private boolean facingEast;
	private int count;
	private GRect lifeBar;
	private GObject[] animationArr;
	private GObject level1;
	private GObject level2;
	private GObject level3;
	private GImage player;
	private GImage dragon1;
	private GImage dragon2;
	private GObject mehran;
	private GObject ladder1;
	private GObject ladder2;
	private GObject fireball1;
	private GObject fireball2;
	private GRect bullet;
	private GRect laser;
	private GObject intro;
	
}

