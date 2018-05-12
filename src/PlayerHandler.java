import java.io.IOException;

/**
 * Created by jacobgold on 5/10/18.
 */
public abstract class PlayerHandler extends TCPHandler implements Runnable {
    protected Player player;

    public PlayerHandler (Player player) throws IOException {
        super(player.getSocket());
        this.player = player;
    }

    public void run() {
        try {
            handlePlayer();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    protected abstract void handlePlayer() throws IOException;
}
