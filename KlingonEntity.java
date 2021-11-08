/**
 * Program: Wrong Galaxy
 * Date: November 5 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang
 * Purpose: Adds to certain EnemyEntities the decision-making of a klingon
 */

public class KlingonEntity extends EnemyEntity {
    public boolean isMaster = false; // changes the behaviour of the entity
    private byte count = 0; // dahar master's timer for jumping
    public boolean jumping = false; // when the entity is jumping
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
    	/* klingons chase after player, masters run faster
    	 * this checks to see if luke is within the enemy's sight
    	 */
    	if (!beingPushed) {
	    	if (game.luke.getY() + 200 >= y) {
	    		dx = 40;
	    		if (isMaster) {
	    			dx *= 3;
	    		} if (game.luke.getX() < x) {
	        		dx *= -1;
	    		} // if
	    		
	    	} // if
    	} // if
    	super.move(delta);
    } // move
    
    public void collidedWith(final Entity other) {}
} // class KlingonEntity