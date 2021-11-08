/**
 * Program: Wrong Galaxy
 * Date: November 6 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang
 * Purpose: Adds to entity collision detection (with other entities) and the ability to detect the tile directly
 * below (for level transitions)
 */

public class LukeEntity extends Entity {   
    
	// constructor
    public LukeEntity(final Game g, final String r, final int newX, final int newY) {
        super(r, newX, newY);
        this.game = g;
        this.map = g.getTileMap();
    } // constructor
    
    /* collidedWith
     * deals with collisions between luke and enemy entities when luke is not attacking (deals damage to luke and
     * moves him away)
     */
    public void collidedWith(final Entity other) {
    	if (other instanceof EnemyEntity ) {
    		// move enemy away
    		other.setHorizontalMovement(-other.getHorizontalMovement());
    		
    		// moves luke away from the enemy:
    		// if luke is on the enemy's left
    		if (x + (this.getWidth() / 2) < other.x + (other.getWidth() / 2)) {
    			// if luke would hit left edge
    			if (x - 40 < 1) {
    				x = 1;
    			} // if
    			// if luke would hit a tile to the left
    			else if (isTileAt((x - 41), bottom) || isTileAt((x - 41), y)) {
    				x = ((int) (x - 40) / game.TILESIZE * game.TILESIZE) + game.TILESIZE;
    			} // else if
    			else {
    				x = x - 40;
    			} // else
    		}
    		// if luke is on the enemy's right
    		else {
    			// if luke would hit right edge
    			if (other.x + other.getWidth() + 41 > game.GAMEWIDTH - this.getWidth() - 1) {
    				x = game.GAMEWIDTH - this.getWidth() - 1;
    			} // if
    			// if luke would hit a tile
    			else if (isTileAt((this.x + this.getWidth() + 41), bottom) || isTileAt((this.x + this.getWidth() + 41), y)) {
    				x = (int) (this.x + this.getWidth() + 40) / game.TILESIZE * game.TILESIZE - this.getWidth();
    			} // else if
    			else {
    				x = other.x + other.getWidth() + 40;
    			} // else
    		} // else (if luke is on enemy's right)
    	} // if (luke collided with enemy) XXX remove this loop?
    } // collidedWith
    
    // stops luke from jumping in game
    public void stopJumping() {
    	game.stopJumping();
    } // stopJumping
    
    // returns encoding of tile directly below luke, used to trigger level transitions
    protected char getTileDirectlyBelow() {
    	String s = "test";
    	if (isTileAt(right, (bottom + 1)) && isTileAt (x, (bottom + 1))) {
    		if (map.getTile((int) right / tileSize, ((int) bottom + 1) / tileSize) == map.getTile((int) x / tileSize, ((int) bottom + 1) / tileSize)) {
    			s = map.tileConfig.get(((int) bottom + 1) / tileSize);
	    		return s.charAt((int) right / tileSize);
    		} // if
    	} // if
    	return 'x';
    } // getTileDirectlyBelow
    
    
    
}