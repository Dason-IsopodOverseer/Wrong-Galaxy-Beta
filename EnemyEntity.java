/**
 * Program: Wrong Galaxy
 * Date: November 5 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang
 * Purpose: Adds to Entity the ability to fall without being luke, and to be pushed back by lightsabers
 */

// after an EnemyEntity is initialized, it must have its setMap called
public abstract class EnemyEntity extends Entity {    
    protected final int fallingSpeed = 300; // how fast the entity falls, in px/s
	public byte health = 1; // how many times the enemy needs to be hit to die
	
    public EnemyEntity(final Game g, final String r, final int newX, final int newY, int speed) {
        super(r, newX, newY);
        game = g;
        dx = speed;
    } // constructor
    
    // moves the enemy
    public void move(long delta) {
    	dy = fallingSpeed;
    	super.move(delta);
    } // move
    
    // when an enemy is hit by a lightsaber, they get knocked back
    public void moveBack (){
    	// if enemy is on luke's left
    	if (!game.facingRight) {
			if (x - 30 < 1) {
				// if enemy would hit left edge of screen
				x = 1;
			} else if (isTileAt((x - 31), bottom) || isTileAt(( x - 31), y)) {
				// if the enemy would hit a tile to the left
				x = ((int) (x - 30) / game.TILESIZE * game.TILESIZE) + game.TILESIZE;
			} else {
				x = x - 30;
			} // else
		} // if
    	
		// if enemy is on luke's right
		else {
			if (x + 31 > game.GAMEWIDTH - this.getWidth() - 1) {
				// if enemy would hit right edge of screen
				x = game.GAMEWIDTH - this.getWidth() - 1;
			} else if (isTileAt(this.x + this.getWidth() + 31, bottom) || isTileAt((this.x + this.getWidth() + 31), y)) {
				// if enemy would hit a tile
				x = (int) (this.x + this.getWidth() + 30) / game.TILESIZE * game.TILESIZE - this.getWidth();
			} // else if
			else {
				x = x + 30;
			} // else
		} // else
    } // moveBack
} // class EnemyEntity