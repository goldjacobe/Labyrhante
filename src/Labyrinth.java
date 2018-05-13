import java.io.*;

public class Labyrinth {

    private final int h;
    private final int w;
    private boolean[][] labyrinth;

    public Labyrinth(int h, int w) {
        this.h = h;
        this.w = w;
        try {
            String labyrinthString = runLaby();
            System.out.println(labyrinthString);
            labyrinth = new boolean[h][w];
            readLabyrinth(labyrinthString);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private String runLaby() throws IOException {
        Process process = new ProcessBuilder("./laby", "" + h, "" + w).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder out = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            out.append(line);
            out.append("\n");
        }

        return out.toString();
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
        try {
            return labyrinth[x][y];
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }



}
