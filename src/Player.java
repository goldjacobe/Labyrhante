import java.net.InetAddress;
import java.net.Socket;

public class Player extends Ghost{

    private final Socket socket;
    private int port;
    private int game;
    private Thread lobbyThread;

    private boolean started;

    private int points;

    public Player(Socket socket) {
        this.socket = socket;
        this.port = 0;
        this.started = false;
    }

    public Socket getSocket() {
        return socket;
    }
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return port;
    }

    public void joinGame(int p, int g) {
        port = p;
        game = g;
    }

    public int leaveGame() {
        return game;
    }

    public void start() {
        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public void setLobbyThread(Thread lobbyThread) {
        this.lobbyThread = lobbyThread;
    }

    public Thread getLobbyThread() {
        return lobbyThread;
    }

    public int getPoints() {
        return points;
    }

    public void incPoints() {
        points++;
    }

    public String getPointsString() {
        return ("" + (points + 10000)).substring(1);
    }
}
