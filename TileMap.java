import java.io.*;
import java.util.ArrayList; // import the ArrayList class

public class TileMap {

	private Sprite[][] tiles;
	public ArrayList<String> tileConfig = new ArrayList(); // stores the configuration of different tiles after being read by mapReader
	private Sprite tileSprite[] = new Sprite[10];
	private Game game;
	private int height = 0; // stores the height of the map
	private int width = 0; // stores the width of the map
	private final int TILESIZE = 50; // the height and width of each tile in px
	
	
	public TileMap(String tileFile, Game g) {
		
		// read the txt tileFile and populate the empty tileConfig arraylist
		// also gets width and height
		mapReader(tileFile);
		game = g;
		
		tiles = new Sprite[width][height];
		
		// set all tile sprites to corresponding tile images
		for (int i = 0; i < tileSprite.length; i++) {
			tileSprite[i] = (SpriteStore.get()).getSprite("sprites/" + i + ".png");
		}
		fillMap();
	}
	
	public int getWidth() {
		return tiles.length;
	}
	
	public int getHeight() {
		return tiles[0].length;
	}
	
	// get width - tiles.length
	// get height - tiles[0].length
	public Sprite getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public void setTile(int x, int y, Sprite tile) {
		tiles[x][y] = tile;
	}
	
	// fills sprite array with appropriate tiles (based on string [] input)
	private Sprite[][] fillMap() {
	    // begin to parse!
	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	        	String line = tileConfig.get(y);
	            char ch = line.charAt(x);
	            
	            // check if the char represents tile A, B, N, etc.
	            switch (ch) {
		            case '@':
		            	tiles[x][y] = tileSprite[0];
		            	break;
		            case 'A':
		            	tiles[x][y] = tileSprite[1];
		            	break;
		            case 'B':
		            	tiles[x][y] = tileSprite[2];
		            	break;
		            case 'C':
		            	tiles[x][y] = tileSprite[3];
		            	break;
		            case 'D':
		            	tiles[x][y] = tileSprite[4];
		            	break;
		            case 'E':
		            	tiles[x][y] = tileSprite[5];
		            	break;
		            case 'F':
		            	tiles[x][y] = tileSprite[6];
		            	break;
		            case 'G':
		            	tiles[x][y] = tileSprite[7];
		            	break;
		            case 'H':
		            	tiles[x][y] = tileSprite[8];
		            	break;
		            case 'J':
		            	tiles[x][y] = tileSprite[9];
		            	break;
		            case 'K':
		            	tiles[x][y] = tileSprite[10];
		            	break;
		            case 'L':
		            	tiles[x][y] = tileSprite[11];
		            	break;
		            case 'M':
		            	tiles[x][y] = tileSprite[12];
		            	break;
		            case 'N':
		            	tiles[x][y] = tileSprite[13];
		            	break;
		            case 'O':
		            	tiles[x][y] = tileSprite[14];
		            	break;
		            case 'P':
		            	tiles[x][y] = tileSprite[15];
		            	break;
		            case 'k':
		            	tiles[x][y] = null;
						game.entities.add(new KlingonEntity(game, "kling", (x * TILESIZE), (y * TILESIZE)));
						break;
		            case 'm':
		            	tiles[x][y] = null;
						game.entities.add(new KlingonEntity(game, "master", (x * TILESIZE), (y * TILESIZE)));
						break;
		            case 'b':
		            	tiles[x][y] = null;
						game.entities.add(new BorgEntity(game, "borg", (x * TILESIZE), (y * TILESIZE)));
						break;
		            case 'q':
		            	tiles[x][y] = null;
						game.entities.add(new BorgEntity(game, "queen", (x * TILESIZE), (y * TILESIZE)));
		            default:
		            	tiles[x][y] = null;
	            }
	        }
	    }
		return tiles;
	}
	
	// reads the tileFile ad stores it in tileConfig
	private void mapReader(String tileFile) {
		
        // try to retrieve file contents
        try {
        	
        	// input
            String folderName = "maps/";
            String resource = tileFile;

			// this is the path within the jar file
			InputStream input = TileMap.class.getResourceAsStream(folderName + resource);
			if (input == null) {
				
				// this is how we load file within editor (eg eclipse)
				input = TileMap.class.getClassLoader().getResourceAsStream(resource);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			in.mark(Short.MAX_VALUE);  // see api

		    while (true) {
		        String line = in.readLine();
		        
		        // no more lines to read
		        if (line == null) {
		            in.close();
		            break;
		        }
		        
		        // add every line except for comments
		        if (!line.startsWith("#")) {
		        	
		        	// * makes an entire row empty
		        	if (line.startsWith("*")) {
		        		line = "                            "; // 28 spaces
		        	}
		            tileConfig.add(line);
		            
		            // set the width
		            width = Math.max(width, line.length());
		        }
		    }
		    
		    // set the height
		    height = tileConfig.size();
        } catch (Exception e) {
        	System.out.println("File Input Error");
        } // end of try catch
    }
}

