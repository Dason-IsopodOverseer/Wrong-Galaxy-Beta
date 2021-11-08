/**
 * Program: Wrong Galaxy
 * Date: November 5 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang
 * Purpose: Adds to certain EnemyEntities the decision-making of a klingon
 */

public class KlingonEntity extends EnemyEntity {
    public boolean isMaster = false; // changes the behaviour of the entity
    private byte count = 0; // dahar master's timer for jumping
    private boolean jumping = false; // when the entity is jumping
    private int jumpCount = 0; // how long the entity has been jumping
	
    public KlingonEntity(final Game g, final String type, final int newX, final int newY) {
        super(g, type, newX, newY, 0);
        
     	// set dahar master attributes
        if (type.equals("master")) {
        	isMaster = true;
        	health = 3;
        } // if
    } // constructor
    
    // moves the klingon
    public void move(long delta) {
    	// counter increments every 50 frames
    	if (frame % 50 == 0) {
    		count++;
    	} if (count > 100) {
    		count = 0;
    	} // if
    	
    	// end the jump after 500 game loops
    	if (jumping == true) {
    		jumpCount++;
    	} if (jumpCount > 500) {
    		dy = fallingSpeed;
    		jumping = false;
    		jumpCount = 0;
    	} // if

    	/* klingons chase after player, masters can jump
    	 * this checks to see if luke is within the enemy's sight
    	 */
    	if (!beingPushed) {
	    	if (game.luke.getY() + 200 >= y) {
	    		dx = 40;
	    		if (isMaster) {
	    			dx *= 1.5;
	    		} if (game.luke.getX() < x) {
	        		dx *= -1;
	    		} // if
	    		
	    		// dahar masters jump every 5000 game loops
	    		if (isMaster && count >= 100 && this.isTileBelow(delta)) {
	        		dy = -400;
	        		jumping = true;
	        	} // if
	    	} // if
    	} // if
    	super.move(delta);
    } // move
    
    public void collidedWith(final Entity other) {}
} // class KlingonEntity