import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by jacobgold on 5/9/18.
 */
public class Server
{
    public static final int PORT = 5000;

    private final int port;
    private int nextGameNumber;
    private HashMap<Integer,Game> games;

    public static void main(String[] args) {
        Server server = new Server(PORT);
        server.start();
    }

    public Server(int port) {
        this.port = port;
        this.games = new HashMap<>();
        this.nextGameNumber = 0;
    }

    public void start() {
        try
        {
            listen();
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public synchronized boolean addPlayerToGame(int m, String id, Player player) {
        Game game = games.get(m);
        return game != null && game.addPlayer(id, player);
    }

    public int removePlayerFromGame(Player p) {
        return p.leaveGame();
    }

    public Collection<Game> getGames() {
        return games.values();
    }

    public Game getGame(int m) {
        return games.get(m);
    }

    public synchronized int createGame() {
        int number = nextGameNumber++;

        try{

            Game game = new Game(number, this);
            games.put(number, game);

            Thread gameThread = new Thread(game);
            gameThread.start();

        } catch (UnknownHostException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        return number;
    }


    public void startGame(int number) {
        games.remove(number);
    }

    private void listen() throws IOException {
        ServerSocket servSock = new ServerSocket(port);
        while (true) {
            Socket socket = servSock.accept();
            System.out.println(socket.getInetAddress().toString());
            System.out.println(socket.getPort());
            Player player = new Player(socket);
            PlayerLobbyHandler handler = new PlayerLobbyHandler(this, player);
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            player.setLobbyThread(handlerThread);
        }
    }

}
