package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    public static class Position {
        int x;
        int y;
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillWorldWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.TREE;
            case 4: return Tileset.MOUNTAIN;
            case 5: return Tileset.GRASS;
            default: return Tileset.NOTHING;
            //grass, sand, flower, mountain, tree, water, nothing
        }
    }

    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    public static void addHexagon(TETile[][] tiles, Position p, TETile tile, int size) {
        if (size < 2) {
            System.out.println("Length must be greater than 2");
            return;
        }
        addHexagonHelper(tiles, p, tile, size - 1, size);
    }
    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int length, int blanks, int notBlank) {
        Position startOfRow = p.shift(blanks, 0);
        drawRow(tiles, startOfRow, tile, notBlank);
         if (blanks > 0) {
             Position nextP = p.shift(0, -1);
             addHexagonHelper(tiles, nextP, tile, length, blanks-1, notBlank+2);
         }
         Position startOfReflection = startOfRow.shift(0, -(2*blanks+1));
         drawRow(tiles, startOfReflection, tile, notBlank);
    }

    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        //fillWorldWithNothing(tiles);
        //Position p = new Position(20, 40);
        addHexColumn(tiles, p, hexSize, tessSize);
        for (int i = 0; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }
        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }
    }

    public static void addHexColumn(TETile[][] tiles, Position p, int size, int num) {
        if (size < 1) {
            return;
        }
        addHexagon(tiles, p, randomTile(), size);
        if (size > 1) {
            Position bottomNeighbor = getBottomNeighbor(p, size);
            addHexColumn(tiles, bottomNeighbor, size, num - 1);
        }
    }

    public static Position getBottomNeighbor(Position p, int n) {
        return p.shift(0, -2*n);
    }

    public static Position getTopRightNeighbor(Position p, int n) {
        return p.shift(2*n-1, n);
    }

    public static Position getBottomRightNeighbor(Position p, int n) {
        return p.shift(2*n-1, -n);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillWorldWithNothing(world);
        Position anchor = new Position(12, 34);
        drawWorld(world, anchor, 3, 3);

        ter.renderFrame(world);
    }

}
