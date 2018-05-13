import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;

public class Server
{
    public static final int PORT = 5000;
    private static final int HEIGHT = 20;
    private static final int WIDTH = 20;
    private static final int NUM_GHOSTS = 5;

    private final int port;
    private final int height;
    private final int width;
    private final int numGhosts;
    private int nextGameNumber;
    private HashMap<Integer,Game> games;

    public static void main(String[] args) {
        int port = PORT;
        int height = HEIGHT;
        int width = WIDTH;
        int numGhosts = NUM_GHOSTS;

        if (args.length > 0)
        {
            port = Integer.parseInt(args[0]);
        }
        if (args.length > 1)
        {
            height = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            width = Integer.parseInt(args[2]);
        }
        if (args.length > 3) {
            width = Integer.parseInt(args[3]);
        }

        Server server = new Server(port, height, width, numGhosts);
        server.start();
    }

    public Server(int port, int height, int width, int numGhosts) {
        this.port = port;
        this.height = height;
        this.width = width;
        this.numGhosts = numGhosts;
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

    public int removePlayerFromGame(Player p, String id) {
        int m = p.leaveGame();
        Game g = getGame(m);
        g.removePlayer(id);
        return m;
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

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getNumGhosts() {
        return numGhosts;
    }
}
