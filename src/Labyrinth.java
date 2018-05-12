import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by jacobgold on 5/11/18.
 */
public class Labyrinth {

    private final int h;
    private final int w;
    private boolean[][] labyrinth;

    private static final String LABY =
            "11000111100000000000000000111111111000100000000000\n"+
                    "11000100100000000000000000100000001000100000000000\n"+
                    "11000100100000000000000000100000001000100000000000\n"+
                    "11111111111111110000000000100000001000111111111111\n"+
                    "11010000100011111111111110100000001000100000000001\n"+
                    "11111111100000000000000010100000001000100000000001\n"+
                    "10000100100000000000000010101111111111100000000001\n"+
                    "10000100100000000000000010101000001000000000000001\n"+
                    "10000100100000000000000011111111111000000000000001\n"+
                    "10000100111111111111111111111100000000000111111111";

    public Labyrinth(int h, int w) {
        this.h = h;
        this.w = w;
        String labyrinthString = LABY;
        labyrinth = new boolean[h][w];
        try {
            readLabyrinth(labyrinthString);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void readLabyrinth(String ls) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(ls));
        for (int i = 0; i < h; i++) {
            String line = br.readLine();
            for (int j = 0; j < w; j++) {
                labyrinth[i][j] = line.charAt(j) == '1';
            }
        }
    }

    public boolean isOpen(int x, int y) {
        return labyrinth[x][y];
    }



}
