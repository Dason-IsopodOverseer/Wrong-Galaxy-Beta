/**
 * Program: Wrong Galaxy
 * Date: November 5 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang, using a lot of code from Lara Wear
 * Purpose: Runs the game loop, handles key events, and contains global variables
 */
import javax.sound.sampled.*;
import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Game extends Canvas {
	private BufferStrategy strategy;   // take advantage of accelerated graphics
	
	private boolean leftPressed = false;  // if left arrow key currently pressed
    private boolean rightPressed = false; // if right arrow key currently pressed
    protected boolean upPressed = false; // if up arrow key currently pressed
    private boolean attackPressed = false; // if attack key (spacebar) currently pressed
    private boolean forcePressed = false; // if force key (a) currently pressed
    private boolean paused = false; // toggled by pressing pause key (z)
    private boolean restart = false;
    public final int TILESIZE = 50; // width and height of all tiles
    private double amountScrolled = 0; // constantly decreases to make the platforms rise
    private double scrollSpeed = 0; // speed that amountScrolled increases at
    private boolean superScroll = false; // when luke is in the bottom 2/10 of the screen, scroll faster
    private boolean scrolling = true; // true if not at the bottom of a level
    private int lvl = 1; // current level
    private final int ATTACKINTERVAL = 700; // time between attacks in milliseconds
    private final int ATTACKDURATION = 300; // how long each attack lasts in milliseconds
    private long lastAttack = System.currentTimeMillis(); // used to determine if player can attack
    private final int PAUSEDURATION = 300; // how long luke is immobile after taking damage in milliseconds
    private long lastPause = 0; // used to determine if luke is immobile
    private final int REDDURATION = 300; // how long force bar is red after you kill an enemy in milliseconds
    public int redBarCountdown = 0; // used to determine colour of force bar
    private int forceAttackInterval = 2000; // time between force attacks
    private long lastForceAttack = System.currentTimeMillis();
    public ArrayList<Entity> entities = new ArrayList(); // list of entities
    public ArrayList<Entity> deadEnemies = new ArrayList(); // list of enemies killed this loop
    private Sprite heart = (SpriteStore.get()).getSprite("sprites/heart.png");
    private Sprite lostHeart = (SpriteStore.get()).getSprite("sprites/lostheart.png");
    private Sprite[] hearts = {heart, heart, heart}; // what sprites to draw to represent luke's health
    private final int MOVESPEED = 300; // speed luke moves
    public Entity luke; // stores the Entity controlled by the player
    public Entity enemy; // stores other Entities
    private int jumpTarget; // set when luke jumps to say how high luke needs to get before falling
    private boolean jumping = false; // when luke is moving up
    public boolean facingRight = true; // when the right arrow was pressed more recently than the left arrow
    public int health = 3; // player's remaining health
    private boolean gameStarted = false; // when a level of the game is underway
    private final int LASTLEVEL = 6; // number of levels in the game
    private TileMap map = new TileMap("level1.txt", this); // used to draw platforms and initiate enemies
    public final int GAMEWIDTH = TILESIZE * map.getWidth(); // width of game window in px 
    public final int GAMEHEIGHT = 900; // height of game window in px
    private int levelHeight = 0; // height of the current level in px
    private static Clip clip; // plays music and sound effects
    long lastLoopTime = System.currentTimeMillis(); // used to determine how fast the game is running
    Sprite introScreen = (SpriteStore.get()).getSprite("screens/introscreen.png"); // intro screen image
    Sprite winScreen = (SpriteStore.get()).getSprite("screens/win.png"); // win screen image
    Sprite loseScreen = (SpriteStore.get()).getSprite("screens/lose.png"); // lose screen image
    Sprite pauseScreen = (SpriteStore.get()).getSprite("screens/paused.png"); // pause screen image
    
	// Construct the game and set it running
	public Game() {
		// create a swing frame to contain game
		JFrame container = new JFrame("Wrong Galaxy");

		// hold the content of the frame
		JPanel panel = (JPanel) container.getContentPane();

		// set up the resolution of the game
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		panel.setPreferredSize(new Dimension(GAMEWIDTH,GAMEHEIGHT));
		panel.setLayout(null);

		// set up canvas size (this) and add to frame
		setSize(screenSize);
		panel.add(this);

		// Tell AWT not to bother repainting canvas since that will
        // be done using graphics acceleration
		setIgnoreRepaint(true);
		
		// make the window visible
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		// make the game appear in the center of the screen
	    container.setLocation((int) (screenSize.getWidth()/2.0 - GAMEWIDTH/2.0), (int) (screenSize.getHeight()/2.0 - GAMEHEIGHT/1.8));

        // if user closes window, shutdown game and jre
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		});

		// add key listener to this canvas
		addKeyListener(new KeyInputHandler());

		// request focus so key events are handled by this canvas
		requestFocus();

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		// initiate luke and finish the initiation of the level 1 enemies
		luke = new LukeEntity(this, "luke", 0, 0);
		entities.add(luke);
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).setMap();
		} // for

		// start the game
		introScreenLoop();
		gameLoop(); // called when introScreenLoop returns
		endScreen(); // called when gameLoop returns
    } // constructor

	public TileMap getTileMap() {
		return this.map;
	} // getTileMap
	
	public boolean getJumping() {
		return jumping;
	} // getJumping
	
	public void stopJumping() {
		jumping = false;
	} // stopJumping
	
	public void setBackground(Graphics2D g, int imageNum, float alpha) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		Sprite img = (SpriteStore.get()).getSprite("backgrounds/" + imageNum + ".png");
		img.draw(g, 0, 0);
	} // setBackground
	
	// draws the intro screen, then waits for the game to start
	private void introScreenLoop() {
        // get graphics context for the accelerated surface and use it
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        introScreen.draw(g, 0, 0);
        
        // clear graphics and flip buffer
        g.dispose();
        strategy.show();
        
        // wait until game starts
        while(!gameStarted) {
        	// necessary to prevent "caching" of boolean
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
        } // while
	} // introScreenLoop
	
	// draw the end screen
	private void endScreen() {
        // get graphics context for the accelerated surface and use it
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        winScreen.draw(g, 0, 0);
        
        // clear graphics and flip buffer
        g.dispose();
        strategy.show();
	} // endScreen
	
	// repeatedly moves entities, draws the screen and checks collisions
	private void gameLoop() {
		resetLevel();
		lastLoopTime = System.currentTimeMillis();

        // keep loop running until game ends
        while (true) {
			// calculate time since last update
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();
            redBarCountdown -= delta;
            
            // get graphics context for the accelerated surface
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            setBackground(g, lvl, 1);
            
            // remove dead entities
            entities.removeAll(deadEnemies);

            // move entities
            if (!luke.pauseMovement) {
	            for (int i = 0; i < entities.size(); i++) {
	                Entity entity = (Entity) entities.get(i);
	                entity.move(delta);
	            } // for
	            
	            // determine how the screen should scroll
	            if ((levelHeight - GAMEHEIGHT) + amountScrolled < 10) {
	        		// if luke is at bottom of the level, don't scroll
	            	scrolling = false;
	        	} else if (luke.getY() + amountScrolled > GAMEHEIGHT * 0.7) {
	            	// if luke is at the bottom of the screen, start scrolling fast
	            	superScroll = true;
	
	            } else if (luke.getY() + amountScrolled < GAMEHEIGHT * 0.2) {
	            	// if luke is at the top of the screen, stop scrolling fast
	            	superScroll = false;
	            } // else if
	            
	            // scroll
	            if (scrolling) {
		            if (superScroll) {
		            	amountScrolled += (-1000 * delta) / 1000;
		            } else {
		            	amountScrolled += (scrollSpeed * delta) / 1000;
		            } // else
	            } // if
            } // if      
            
    		// update entity animations to account for movement
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = (Entity) entities.get(i);
                entity.updateAnimations(delta);
            } // for

            // draw entities
            for (int i = 0; i < entities.size(); i++) {
            	Entity entity = (Entity) entities.get(i);
            	entity.draw(g, amountScrolled);
            } // for
            
            // check collisions
            for (int i = 0; i < entities.size(); i++) {
    	        if (luke.lightsaberHit(entities.get(i), facingRight) && entities.get(i) != luke) {
    	        	if (luke.getAttackFrame() != 3) {
    	        		// if luke hits an enemy with his lightsaber
    	        		((EnemyEntity) entities.get(i)).health--;
    	        		((EnemyEntity) entities.get(i)).moveBack();
    	        		System.out.println("Hit detected! Enemy health: " + ((EnemyEntity) entities.get(i)).health);
    	        		if (((EnemyEntity) entities.get(i)).health <= 0) {
    	        			// if an enemy is killed
    	        			forceAttackInterval += 500;
    	        			deadEnemies.add(entities.get(i));
    	        			redBarCountdown = REDDURATION;
    	        		} // if
    	        		
    	        		if (((EnemyEntity) entities.get(i)).health > 0) {
    	        			entities.get(i).changeMoveAnimations(((EnemyEntity) entities.get(i)).health);
    	        		} // if
    	        	} // if
    	        } else if (entities.get(i).collidesWith(luke) && entities.get(i) != luke) {
	        		// if an enemy hits luke
    	        	health--;
	        		hearts[health] = lostHeart;
	        		luke.collidedWith(entities.get(i));
	        		lastPause = System.currentTimeMillis();
	        		luke.pauseMovement = true;		
	        	} // else if
    	    } // for
            
            if (System.currentTimeMillis() - lastPause > PAUSEDURATION) {
    			luke.pauseMovement = false;
    		} // if
            
            // check if luke should be moving vertically
            if (jumping) {
            	// check if luke has reached the peak of his jump
            	if (luke.getY() < jumpTarget || (luke.getY() + amountScrolled < 0)) {
            		jumping = false;
            	} // if	
            } // if
            
            // draw
            drawMap(g);
            
            // draw force bar
            g.fillRect(GAMEWIDTH - forceAttackInterval / 30, 0, forceAttackInterval / 30, 40);
        	if (redBarCountdown < 0) {
        		g.setColor(new Color(20,170,255));
        	} else {
        		g.setColor(new Color(150,0,0));
        	} // else
        	g.fillRect(GAMEWIDTH - forceAttackInterval / 30, 0, (int) (System.currentTimeMillis() - lastForceAttack) / 30, 40);
            
            // draw hearts
            for (int i = 1; i <= 3; i++) {
            	hearts[i - 1].draw(g, GAMEWIDTH - (40 * i), 60);
            } // for
            
            // if necessary, pauses the game loop
            if (paused) {
            	drawPauseScreen();
            	while (true) {
            		// necessary to prevent "caching" of boolean
            		try {
            			Thread.sleep(1);
            		} catch (InterruptedException e) {}
            		
            		if (restart) {
            			resetLevel();
            		}
            		
            		if (!paused) {
            			lastLoopTime = System.currentTimeMillis();
            			break;
            		} // if
            	} // while
            } // if
           
            // clear graphics and flip buffer
            g.dispose();
            strategy.show();
            
            // revert luke to default movement
            if (!jumping) {
            	luke.setVerticalMovement(600);
            } // if
            luke.setHorizontalMovement(0);
            	
        	// Handle input
 			if (rightPressed && !leftPressed) {
 				luke.setHorizontalMovement(MOVESPEED);
 				facingRight = true;
 			} else if (leftPressed) {
 				luke.setHorizontalMovement(-MOVESPEED);
 				facingRight = false;
 			} if (upPressed && luke.isTileBelow(delta)) {
 				luke.setVerticalMovement(-600);
 				jumpTarget = luke.getY() - 200;
 				jumping = true;
 			} // if
 			
 			// luke attacks, if necessary
 			if (attackPressed && (System.currentTimeMillis() - lastAttack > ATTACKINTERVAL)) {
 				lastAttack = System.currentTimeMillis();
 				luke.attacking = true;
 				playSound("lightsaber.wav");
 			} // if
 			
 			// stop attack once attack duration is over
 			if (System.currentTimeMillis() - lastAttack > ATTACKDURATION) {
 				luke.attacking = false; 
 			} // if
 			
 			// check if luke can use the force
 			if (forcePressed && (System.currentTimeMillis() - lastForceAttack > forceAttackInterval)) {
 				lastForceAttack = System.currentTimeMillis();
 				
 				// push away each enemy in front of luke
 				for (int i = 0; i < entities.size(); i++) {
 					Entity enemy = entities.get(i);
 					if (enemy.getY() < luke.getY() + 30 && enemy.getY() > luke.getY() - 30) {
 						if (facingRight && enemy.getX() > luke.getX()) {
 							enemy.pushBackTo(enemy.getX() + 100, true);
 						} else if (!facingRight && enemy.getX() < luke.getX()) {
 							enemy.pushBackTo(enemy.getX() - 100, false);
 						} // else if
 					} // if
 				} // for
 			} // if
 			
 			// see if luke is dead
 			if (health == 0) {
 				lose();
 			} else if ((luke.getY() + amountScrolled) < 0 && luke.isTileBelow(delta)) {
            	lose();
            } // if
 			
 			// if luke is at the bottom region of the level, detect tiles below
 			if (luke.y > (levelHeight - 200)) {
	 			if (((LukeEntity) luke).getTileDirectlyBelow() == '@') {
	 				// go to next level
	 				lvl++;
	 				if (lvl <= LASTLEVEL) {
	 					resetLevel();
	 					luke.setMap();
	 				} else {
	 					return;
	 				} // else
	 			} // if
 			} // if
         } // while
	} // gameLoop
	
	// resets variables to prepare to start a level
	private void resetLevel() {
		// reset variables
	    gameStarted = false;
		entities.clear();
		entities.add(luke);
		play(lvl);
		scrolling = true;
	    health = 3;
	    hearts[0] = heart;
	    hearts[1] = heart;
	    hearts[2] = heart;
        luke.attacking = false;
        luke.pauseMovement = false;
        amountScrolled = 0;
        superScroll = false;
        luke.x = 0;
        luke.y = 0;
        restart = false;
        paused = false;
		
        // get graphics context for the accelerated surface and use it
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.fillRect(0,0,GAMEWIDTH,GAMEHEIGHT);
        loadLvlMap(g);
        for (int i = 1; i <= 3; i++) {
        	hearts[i - 1].draw(g, GAMEWIDTH - (40 * i), 60);
        } // for
        drawMap(g); 
        levelTransition(g);
        
        //clear graphics and flip buffer
        g.dispose();
        strategy.show();
        
        // wait 0.2 seconds before continuing
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {}
        lastLoopTime = System.currentTimeMillis();
	} // resetLevel
	
	// draw lose screen, then wait for game to restart
	public void lose() {
		// draw lose screen and wait for input
        gameStarted = false;
        
        // get graphics context for the accelerated surface and use it
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        loseScreen.draw(g, 0, 0);

        // clear graphics and flip buffer
        g.dispose();
        strategy.show();
        
        while (!gameStarted) {
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
        } // while
		                
        // go back to start of level
        resetLevel();
	} // lose
	
	// fade in background and map for a level
	private void levelTransition(Graphics2D g) {
		
		// draw level transition screen
		Sprite lvlTScreen = (SpriteStore.get()).getSprite("screens/lvlt" + lvl + ".png"); //XXX
        lvlTScreen.draw(g, 0, 0);
		
        strategy.show();
        
        // pause a bit on the full opacity level transition screen first
        try {
        	Thread.sleep(250);
        } catch (InterruptedException e) {}
        
        // fade in level
		for (float alpha = 0; alpha <= 1;) {
	        try {
	        	Thread.sleep(20);
	        } catch (InterruptedException e) {}
	        
	        // fade in level
	        setBackground(g, lvl, alpha);
	        drawMap(g, alpha);
	        strategy.show();

	        // further increment alpha
	        if (alpha <= 0.05) {
	        	alpha += 0.01;
	        } else {
	        	alpha += 0.04;
	        } // else
		} // for
		g.dispose();
		gameStarted = true;
	} // levelTransition
	
	// draw pause screen
	private void drawPauseScreen() {
        // get graphics context for the accelerated surface and use it
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        pauseScreen.draw(g, 0, 0);
        // clear graphics and flip buffer
        g.dispose();
        strategy.show();
	} // drawPauseScreen
	
	// make a new tilemap and calculate the level height
	private void loadLvlMap(Graphics g) {
		map = new TileMap("level" + lvl + ".txt", this);
		levelHeight = map.getHeight() * TILESIZE;
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).setMap();
		} // for
	} // loadLvlMap
	
	// draw all tiles that are on screen
	private void drawMap(Graphics g) {
		Sprite tile = null;
        int topY = (int) amountScrolled / -TILESIZE;
        int bottomY = (GAMEHEIGHT / TILESIZE) + topY;
        if (bottomY > map.getHeight() - 1) {
        	bottomY = map.getHeight() - 1;
        } // if
        
        for (int i = 0; i < map.getWidth(); i++) { 
			for (int j = topY; j <= bottomY; j++) {
				tile = map.getTile(i, j);
				if (tile != null) {
					tile.draw(g, (i * TILESIZE), (int)(j * TILESIZE + amountScrolled));
				} // if
			} // inner for
		} // outer for
	} // drawMap
	
	// draw all tiles that are on screen, translucent
	private void drawMap(Graphics2D g, float alpha) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		drawMap(g);
	} // drawMap overloaded
	
	// call playSound
	private void play(final int song) {
		try {
			clip.close();
		} catch (Exception e) {}
		
		if (song == 0) {
			playSound("wars-theme.wav");
		} else if (song == 1) {
			playSound("into-trap.wav");
		} else if (song == 2) {
			playSound("ocampa.wav");
		} else if (song == 3) {
			playSound("trek-theme.wav");
		} else if (song == 4) {
			playSound("borg-engaged.wav");
		} else if (song == 5) {
			playSound("into-trap-fast.wav");
		} else if (song == 6) {
			playSound("borg-take-picard.wav");
		} // else if
	} // play
	
	// play a song
	// Alex Darby and someone named T.U. gave us this code
	public static synchronized void playSound(final String ref) {
		(new Thread(new Runnable() {
	    	public void run() {
	    		try {
	    			clip = AudioSystem.getClip();
	    			URL url = getClass().getClassLoader().getResource("sounds/" + ref);
	    			if (url == null) {
	    				System.out.println("Failed to load: " + ref);
	    				System.exit(0);
	    			} // if
	    			AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	    			clip.open(inputStream);
	    			clip.start();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		} // catch
	    	} // run
		})).start();
	 } // playSound

	// store key events
	private class KeyInputHandler extends KeyAdapter {
        
       /* The following methods are required
        * for any class that extends the abstract
        * class KeyAdapter.
        */
       public void keyPressed(KeyEvent e) {
        	// when a key is first pressed
	         if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	        	 leftPressed = true;
	         } if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	        	 rightPressed = true;
	         } if (e.getKeyCode() == KeyEvent.VK_UP) {
	       	  	upPressed = true;
	         } if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        	 attackPressed = true;
	         } if (e.getKeyChar() == KeyEvent.VK_ENTER) {
	        	 gameStarted = true;
	         } if (e.getKeyCode() == KeyEvent.VK_X) {
	        	 forcePressed = true;
	         } if (e.getKeyCode() == KeyEvent.VK_Z) {
	        	 paused = !paused;
	         } if (e.getKeyCode() == KeyEvent.VK_R) {
	        	 restart = true;
	         } // if
       } // keyPressed

       public void keyReleased(KeyEvent e) {
        	// when a key is released
        	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        		leftPressed = false;
        	} if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        		rightPressed = false;
	        } if (e.getKeyCode() == KeyEvent.VK_UP) {
	        	upPressed = false;
	        	jumping = false;
	        } if(e.getKeyCode() == KeyEvent.VK_SPACE) {
	        	attackPressed = false;
	        } if (e.getKeyCode() == KeyEvent.VK_X) {
	        	forcePressed = false;
	        } if (e.getKeyCode() == KeyEvent.VK_X) {
	        	restart = false;
	        }
       } // keyReleased

	   public void keyTyped(KeyEvent e) {
		   // if escape is pressed, end game
		   if (e.getKeyChar() == 27) {
			   System.exit(0);
		   } // if
	   } // keyTyped

	} // class KeyInputHandler
	
	// starts the program
	public static void main(String[] args) {
		new Game();
	} // main
} // class Game