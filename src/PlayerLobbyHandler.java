import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jacobgold on 5/9/18.
 */
public class PlayerLobbyHandler extends PlayerHandler {
    private final Server server;
    private boolean inGame;
    private boolean started;
    private String id;

    public PlayerLobbyHandler(Server server, Player player) throws IOException {
        super(player);
        this.server = server;
        this.inGame = false;
        this.started = false;
        this.id = "";
    }

    protected void handlePlayer() throws IOException {
        sendGamesList();
        while (!started) {
            lobbyMenu();
        }
    }

    private void sendGamesList() throws IOException {
        Collection<Game> games = server.getGames();

        sendString("GAMES ");
        sendNumber(games.size());
        sendStars();

        for (Game g : games) {
            sendGameInfo(g);
        }

    }

    private void sendGameInfo (Game g) throws IOException {
        sendString("GAME ");
        sendNumber(g.getNumber());
        sendCharAscii(' ');
        sendNumber(g.getNumPlayers());
        sendStars();
    }

    private void lobbyMenu() throws IOException {
        CommandHandler handler = new LobbyCommandHandler(receiveMessage());
        handler.handleMessage();
    }

    private void start() throws IOException {
        if (!inGame) {
            sendDunno();
            return;
        }
        started = true;
        player.start();
    }

    private void newGame(String id, int port) throws IOException {
        if (inGame) {
            sendDunno();
            return;
        }

        int m = server.createGame();
        server.addPlayerToGame(m, id, player);
        this.id = id;
        player.joinGame(port, m);
        inGame = true;
        sendRegok(m);
    }

    private void joinGame(String id, int port, int m) throws IOException {
        if (inGame) {
            sendDunno();
            return;
        }

        if (!server.addPlayerToGame(m, id, player)) {
            sendRegno();
            return;
        }

        this.id = id;;
        player.joinGame(port, m);
        inGame = true;
        sendRegok(m);
    }

    private void sendRegok(int m) throws IOException {
        sendString("REGOK ");
        sendNumber(m);
        sendStars();
    }

    private void sendRegno() throws IOException {
        sendString("REGNO");
        sendStars();
    }

    private void leaveGame() throws IOException {
        inGame = false;
        int m = server.removePlayerFromGame(player, id);
        sendString("UNREGOK ");
        sendNumber(m);
        sendStars();
    }

    private void sendSize(int m) throws IOException {
        Game g = server.getGame(m);

        if (g == null) {
            sendDunno();
            return;
        }

        int h = g.getHeight();
        int w = g.getWidth();
        sendString("SIZE! ");
        sendNumber(m);
        sendString(" ");
        sendNumber(h);
        sendString(" ");
        sendNumber(w);
        sendStars();
    }

    private void sendPlayerList(int m) throws IOException {
        Game g = server.getGame(m);

        if (g == null) {
            sendDunno();
            return;
        }

        int s = g.getNumPlayers();
        Collection<String> players = g.getPlayers();

        sendString("LIST! ");
        sendNumber(m);
        sendString(" ");
        sendNumber(s);
        sendStars();

        for (String id : players) {
            sendString("PLAYER " + id);
            sendStars();
        }
    }

    private class LobbyCommandHandler extends CommandHandler {

        public LobbyCommandHandler(ArrayList<Byte> input) {
            super(input);
        }

        public void handleMessage() throws IOException {
            String command = findNextWord();
            System.out.println(command);
            switch (command) {
                case "START":
                    if (inGame) {
                        start();

                    } else {
                        sendDunno();
                    }

                    break;

                case "NEW":
                    if (inGame) {
                        sendDunno();
                        break;
                    }

                    index++;

                    String id = findNextWord();
                    if (id.length() == 0 || id.length() > 8) {
                        sendDunno();
                        break;
                    }

                    index++;

                    String portStr = findNextWord();
                    if (portStr.length() < 4) {
                        sendDunno();
                        break;
                    }

                    int port = Integer.parseInt(portStr);
                    if (port == 0) {
                        sendDunno();
                        break;
                    }

                    newGame(id, port);

                    break;

                case "REG":
                    if (inGame) {
                        sendDunno();
                        break;
                    }

                    index++;

                    id = findNextWord();
                    if (id.length() == 0 || id.length() > 8) {
                        sendDunno();
                        break;
                    }

                    index++;

                    portStr = findNextWord();
                    if (portStr.length() < 4) {
                        sendDunno();
                        break;
                    }

                    port = Integer.parseInt(portStr);
                    if (port == 0) {
                        sendDunno();
                        break;
                    }

                    index++;

                    int m = findNextValue();

                    if (m == -1) {
                        sendDunno();
                        break;
                    }

                    joinGame(id, port, m);
                    break;

                case "UNREG":
                    if (inGame) {
                        leaveGame();
                    } else {
                        sendDunno();
                    }
                    break;

                case "SIZE?":
                    index++;

                    m = findNextValue();

                    if (m == -1) {
                        sendDunno();
                        break;
                    }

                    sendSize(m);
                    break;

                case "LIST?":
                    index++;

                    m = findNextValue();

                    if (m == -1) {
                        sendDunno();
                        break;
                    }

                    sendPlayerList(m);
                    break;

                case "GAMES?":
                    sendGamesList();
                    break;

                default:
                    sendDunno();
                    break;
            }
        }


    }
}
