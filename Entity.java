/* Program: Wrong Galaxy
 * Date: November 5, 2021
 * Authors: Carter Cranston, Dason Wang, Emily Wang, with a lot of code from Lara Wear
 * Purpose: Defines the behavior and attributes of all entities in the game (enemies and player character)
 */
 
 import java.awt.*;
 
 public abstract class Entity {

    protected double x;   // current x location
    protected double y;   // current y location
    protected Sprite sprite; // this entity's sprite
    protected double dx; // horizontal speed (px/s)  + -> right
    protected double dy; // vertical speed (px/s) + -> down
    protected double right; // location of the right edge of the entity
    protected double bottom; // location of the bottom edge of the entity
    protected TileMap map; // current tile map
    protected Game game; // entity's copy of current game
    protected boolean beingPushed = false; // true when entity is being force pushed
    protected int pushTarget = 0;
    protected boolean pushedRight = false; // true when entity is being force pushed to the right
    
    protected int tileSize = 50;
    protected int mapWidth = 28;
    
    private String entityName;
    
    public boolean pauseMovement = false;
    public boolean attacking = false;

    // the following variables control animations for this entity
	private String[] moveLeft = new String[4]; // array of images for moving left
	private String[] moveRight = new String[4]; // array of images for moving right
	private String[] attackLeft = new String[3]; // array of images for attacking left
	private String[] attackRight = new String[3]; // array of images for attacking right
	private String[] hurtLeft = new String[4]; // array of images for taking damage facing left
	private String[] hurtRight = new String[4]; // array of images for taking damage facing right
	protected boolean isFacingRight = true; // true if sprite is facing right
	protected int frame = 0; // stores the specific frame the game is on (refreshes every 600 frames)
    private int refreshRate = 200; // higher values result in slower FPS
    private byte attackFrame = 3; // counts entity attack animation frames
    private boolean finishedAttack = false; // true if entity has just finished attacking whilst facing left
    

    private Rectangle me = new Rectangle(); // bounding rectangle of this entity
    private Rectangle him = new Rectangle(); // bounding rectangle of other entities
                                             
    /* Constructor
     * input: reference to the image for this entity,
     * initial x and y location to be drawn at
     */
     public Entity(String type, int newX, int newY) {
       x = newX;
       y = newY;
       entityName = type;
       setAnimations();
     } // constructor
     
     /* determines and sets entity animation variables
      * selects direction of entity
      * selects correct image to use
      */
     private void setAnimations() {

    	 // configure all moveLeft images
    	 for (int i = 0; i < moveLeft.length; i++) {
    		 moveLeft[i] = "sprites/" + entityName + "/L" + i + ".png";
    	 }
    	 
    	 // configure all moveRight images
    	 for (int i = 0; i < moveRight.length; i++) {
    		 moveRight[i] = "sprites/" + entityName + "/R" + i + ".png";
    	 }
    	 
    	 // configure all attackLeft images
    	 for (int i = 0; i < attackLeft.length; i++) {
    		 attackLeft[i] = "sprites/" + entityName + "/ATKL" + i + ".png";
    	 }
    	 
    	 // configure all attackRight images
    	 for (int i = 0; i < attackRight.length; i++) {
    		 attackRight[i] = "sprites/" + entityName + "/ATKR" + i + ".png";
    	 }
    	 
    	 for (int i = 0; i < hurtLeft.length; i++) {
    		 hurtLeft[i]= "sprites/" + entityName + "/DMGL" + i + ".png";
    	 }
    	 
    	 for (int i = 0; i < hurtRight.length; i++) {
    		 hurtRight[i]= "sprites/" + entityName + "/DMGR" + i + ".png";
    	 }
    	 
    	 // configure initial state of entity
    	 sprite = (SpriteStore.get()).getSprite(moveRight[1]);
     }
     
  // set additional animations if this entity is a boss
     public void changeMoveAnimations (int health) {
       	 for (int i = 0; i < 4; i++) {
       		 moveLeft[i] = "sprites/" + entityName + "/L" + i + "H" + health + ".png";
       		 moveRight[i] = "sprites/" + entityName + "/R" + i + "H" + health + ".png";
       	 }
     } 
    
     /* updateAnimations
      * Takes an entity state (jumping, moving, attacking) and
      * changes the sprite to match the given animation.
      */
     public void updateAnimations(long delta) {
    	 frame++;
    	 // reset frames if needed
    	 if (frame > (moveRight.length) * refreshRate) {
    		 frame = 0;
    	 } // if 
    	 
    	 if (frame != 0 && (frame - refreshRate) % refreshRate == 0) {
    		 int index = (frame - refreshRate) / refreshRate;
    		 if (dx < 0) {
    			 isFacingRight = false;
    		 } else if (dx > 0) {
    			 isFacingRight = true;
    		 }
    		 
    		 // determine if flicker animation is required
    		 if (pauseMovement) {
    			 refreshRate = 50;
    			 if (isFacingRight) {
    				 sprite = (SpriteStore.get()).getSprite(hurtRight[index]);
    			 }
    			 else {
    				 sprite = (SpriteStore.get()).getSprite(hurtLeft[index]);
    			 }
			 } else {
    			 refreshRate = 200;
    			 String idle =  isFacingRight ? moveRight[1] : moveLeft[1];
				 
    			 // determine state (attacking or not) and reset attack frames to begin an attack animation
    			 if (attacking && attackFrame == 3) {
    				 attackFrame = 0;
    				 if (!isFacingRight) {
    					 x -= 22;
    				 }
    				 if (right + 22 > game.GAMEWIDTH) {
    					 x -= 22;
    				 }
    			 } 
    			 
    			 // execute attack animation
    			 if (attackFrame < 3) {
					 // determine direction, update animation
					 if (isFacingRight) {
            			 sprite = (SpriteStore.get()).getSprite(attackRight[attackFrame]);
            		 } else {
            			 sprite = (SpriteStore.get()).getSprite(attackLeft[attackFrame]);
            			 if (attackFrame + 1 == 3) {
    						 finishedAttack = true;
    					 }
            		 }
					 attackFrame++;
    			 } else {
    				 
    				 // if luke has just finished a left attack, compensate direction
    				 if (finishedAttack) {
    					 finishedAttack = false;
    					 x += 22;
    				 }
    				 
    				 // if luke is jumping, configure animations to match
    				 if (dy < 0 && !isTileBelow(delta)) {
    					 idle =  isFacingRight ? moveRight[0] : moveLeft[0];
    					 index = 0;
    				 }
    				 
    				 // determine direction, update animation
    				 if (dx < 0 && !isTileRight(delta)) {
            			 //isFacingRight = false;
            			 sprite = (SpriteStore.get()).getSprite(moveLeft[index]);
            		 } else if (dx > 0 && !isTileLeft(delta)) {
            			 //isFacingRight = true;
            			 sprite = (SpriteStore.get()).getSprite(moveRight[index]);
            		 } else {
            			 sprite = (SpriteStore.get()).getSprite(idle);
            		 }
	    		 } // determine state if else
	    	 } // else
	     } // if
     } // updateAnimations
     
     /* setMap
      * sets entity's copy of the tile map
      */
     public void setMap() {
    	 map = game.getTileMap();
     }
     
     /* move
      * input: delta - the amount of time passed in ms
      * output: none
      * purpose: after a certain amount of time has passed, update the location
      */
     public void move(long delta) {
    	 // set location of tile edges
         right = x + sprite.getWidth();
         bottom = y + sprite.getHeight();

         // check if entity is being force pushed
 	 	 if (beingPushed) {
 	 		
 	 		// check if it'll hit a platform
 	         if (x < 0) {
 	             beingPushed = false;
 	             return;
 	         } else if (x > tileSize * mapWidth - 10) {
 	        	 beingPushed = false;
 	             return;
 	         } else if (isTileLeft(delta)) {
 	        	 beingPushed = false;
 	             return;
 	         } else if (isTileRight(delta)) {
 	        	 beingPushed = false;
 	             return;
 	         }
 	 		
 	         // move based on force push
 	    	 if (pushedRight) {
 	    		 x += delta * 100.0 / 1000;
 	    		 if (x > pushTarget) {
 	    			beingPushed = false;
 	    		 } // if
 	    	 } else {
 	    		 x -= delta * 100.0 / 1000;
 	    		 if (x < pushTarget) {
 	    			beingPushed = false;
 	    		 } // if
 	    	 } // else
 	     } // if (being force pushed)
 	 	 
 	 	 // if not being force pushed
 	 	 else {
 	 		 
	    	 // check if it'll hit left edge
	         if (dx < 0 && x + ((delta * dx) / 1000) - 1 < 0) {
	        	 // move back so it won't hit left edge
	        	 x = 1;
	             dx = -dx;
	         // check if it'll hit right edge]
	         } else if (dx > 0 && x + ((delta * dx) / 1000) + sprite.getWidth() + 5 > game.GAMEWIDTH) {
	        	 x = (game.GAMEWIDTH - sprite.getWidth() - 1);
	             dx = -dx;
	         // check if it'll hit tile to the left when moved
	         } else if (dx < 0 && isTileLeft(delta)) {
	        	 // move to tile, change movement direction
	        	 x = ((int) (x + ((delta * dx) / 1000) - 1) / tileSize) * tileSize + (tileSize + 1);
	             dx = -dx;
	         // check if it'll hit a tile to the right when moved
	         } else if (dx > 0 && isTileRight(delta)) {
	        	 // move to tile, change movement direction
	        	 x = ((int) (right + ((delta * dx) / 1000) + 1) / tileSize) * tileSize - sprite.getWidth() - 1;
	             dx = -dx;
	         } // else if  
	         
    	 } // else (not being force pushed)
    	 
 	 	 // check if it'll hit a tile to the left when moved
    	 if (dy < 0 && isTileAbove(delta)) {
         	dy = -dy;
         	this.stopJumping();
         // check if it'll hit a tile below when moved
         } else if (isTileBelow(delta)) {
        	 y = ((int) (bottom + ((delta * dy) / 1000) + 1) / tileSize) * tileSize - sprite.getHeight() - 1;
             dy = 0;
        	 
         } // else if
    	 
    	 // update location of entity based on move speeds
    	 x += (delta * dx) / 1000;
    	 y += (delta * dy) / 1000;
    	 
     } // move

     // get and set velocities
     public void setHorizontalMovement(double newDX) {
       dx = newDX;
     } // setHorizontalMovement

     public void setVerticalMovement(double newDY) {
       dy = newDY;
     } // setVerticalMovement

     public double getHorizontalMovement() {
       return dx;
     } // getHorizontalMovement

     public double getVerticalMovement() {
       return dy;
     } // getVerticalMovement

     // get position
     public int getX() {
       return (int) x;
     } // getX

     public int getY() {
       return (int) y;
     } // getY
     
     // getHeight
     public int getHeight() {
    	 return sprite.getHeight();
     } // getHeight
     
     // getWidth
     public int getWidth() {
    	 return sprite.getWidth();
     } // getWidth
     
     /* getAttackFrame
      * returns the animation frame is on during his attack
      */
     public byte getAttackFrame() {
    	 return attackFrame;
     }
     
     // pushBackTo: triggers effects of force push, sets target location of force push
     public void pushBackTo(int target, boolean pushedRight) {
    	 beingPushed = true;
    	 pushTarget = target;
    	 this.pushedRight = pushedRight;
     } // pushBackTo
     
     // isTileAt: returns whether or not there is a tile at a set of coordinates
     public boolean isTileAt (double x, double y) {
    	 return map.getTile((int) x / tileSize, (int) y / tileSize) != null;
     } // isTileAt
     
     //isTileAbove: returns true if entity would hit a tile above
     protected boolean isTileAbove(long delta) {

         // if entity's top-left or top-right corner is in a tile
         return isTileAt(right, (y + (delta * dy) / 1000 - 1)) || 
        		 isTileAt(x, (y + (delta * dy) / 1000 - 1));
     } // isTileAbove
     
   //isTileBelow: returns true if entity would hit a tile below
     protected boolean isTileBelow(long delta) {
     	
     	// if entity's bottom-left or bottom-right corner is in a tile
     		return isTileAt (right, (bottom + (delta * dy) / 1000 + 1)) ||
     				isTileAt(x, (bottom + (delta * dy) / 1000 + 1)); 
     } // isTileBelow
     
   //isTileLeft: returns true if entity would hit a tile left
     protected boolean isTileLeft(long delta) {
         
    	 //try {
          return isTileAt((x + (delta * dx) / 1000 - 1), y) || 
        		  isTileAt((x + (delta * dx) / 1000 - 1), bottom);
     } // isTileLeft
     
   //isTileRight: returns true if entity would hit a tile right
     protected boolean isTileRight(long delta) {
         
     	// if entity's top-right or bottom-right corner is in a tile
     	//try {
     		return isTileAt((right + (delta * dx) / 1000 + 1), y) || 
     				isTileAt((right + (delta * dx) / 1000 + 1), bottom);
     } // isTileRight

    /* draw
     * Draw this entity to the graphics object provided at (x,y)
     */
     public void draw (Graphics g, double translateY) {
       sprite.draw(g,(int)x,(int) (y + translateY));
     }  // draw
     
     /*
      * tells game to stop luke jumping, further defined in LukeEntity
      */
     public void stopJumping() {};
     
     /* collidesWith
      * input: the other entity to check collision against
      * output: true if entities collide
      * purpose: check if this entity collides with the other.
      */
     public boolean collidesWith(Entity other) {
       me.setBounds((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
       him.setBounds(other.getX(), other.getY(), 
                     other.sprite.getWidth(), other.sprite.getHeight());
       return me.intersects(him);
     } // collidesWith
     
     //check if luke's lightsaber makes contact with enemy. Only called by luke
     public boolean lightsaberHit(Entity other, boolean isFacingRight) {
     	boolean hit = false;
      	if (bottom + 20 > other.bottom && y - 20 < other.y) {
      		if (isFacingRight) {
          		if (other.x <= right + 50 && other.x > x + 5) {
          			hit = true;
          		} // if
          	} else {
          		if (other.right >= x - 50 && other.right < x - 5) {
          			hit = true;
          		} // if
          	} // else
      	} // if
      	return hit;
     } // lightsaberHit
     
     
     /* collidedWith
      * input: the entity with which this has collided
      * purpose: notification that this entity collided with another
      * Note: abstract methods must be implemented by any class
      *       that extends this class
      */
      public abstract void collidedWith(Entity other); {}

 } // Entity class