/**
 * Program: Wrong Galaxy
 * Date: November 5 2021
 * Authors: Carter Cranston, Emily Wang and Dason Wang
 * Purpose: Adds to certain EnemyEntities the decision-making of a borg drone or queen
 */

public class BorgEntity extends EnemyEntity {
	public boolean isQueen = false; // changes the behaviour of the entity
	private byte count = 0; // the queen's timer for spawning a drone
	
    public BorgEntity(final Game g, final String type, final int newX, final int newY) {
        super(g, type, newX, newY, 25);
        
     	// set queen attributes
        if (type.equals("queen")) {
        	isQueen = true;
        	health = 3;
        	dx = 12.5; // queen is slower than drones
        } // if
    } // constructor
    
    // moves the enemy
    public void move(long delta) {
    	super.move(delta);
    	
    	// check if this entity should turn around
		if (isTileBelow(delta) && !isTileCompletelyBelow(delta)) {
			dx = -dx;
			x += (delta * dx) / 500;
		} // if
		
		// queens behave differently and can spawn drones
		if (isQueen && frame == 0) {
			count++;
			
			// queen sometimes (about once every 6000 game loops) turns around randomly
			if (Math.random() < 0.1) {
				dx *= -1;
			} // if
			
			// queen spawns a drone once every 6000 game loops
			if (count % 10 == 0) {
				count = 0;
				Entity b = new BorgEntity(game, "borg", this.getX(), this.getY());
				game.entities.add(b);
				b.setMap();
			} // if
		} // if
    } // move
    
	// if entity's bottom-left or bottom-right corner is in a tile
    private boolean isTileCompletelyBelow(long delta) {
     	try {
     		return isTileAt(right, (bottom + (delta * dy) / 1000 + 1)) && isTileAt(x, (bottom + (delta * dy) / 1000 + 1));
     	} catch (Exception e) {
     		return false;
     	} // catch
    } // isTileCompletelyBelow
    
    public void collidedWith(final Entity other) {}
} // class BorgEntity