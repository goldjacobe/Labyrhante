import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jacobgold on 5/10/18.
 */
public class PlayerGameHandler extends PlayerHandler {

    private final Game game;
    private final String id;
    private boolean inGame;

    public PlayerGameHandler (Game game, Player player, String id) throws IOException {
        super(player);
        this.game = game;
        this.id = id;
        this.inGame = true;
    }

    protected void handlePlayer() throws IOException {
        sendWelcomeMessage();
        sendPos();
        while(inGame) {
            gameMenu();
        }
    }

    private void sendWelcomeMessage() throws IOException {
        sendString("WELCOME ");
        sendNumber(game.getNumber());
        sendString(" ");
        sendNumber(game.getHeight());
        sendString(" ");
        sendNumber(game.getWidth());
        sendString(" ");
        sendNumber(game.getNumGhosts());
        sendString(" " + Utility.padAddressString(game.getMulticastAddress()) + " " + Utility.pad4(game.getMulticastPort()));
        sendStars();
    }

    private void sendPos() throws IOException {
        sendString("POS " + id + " " + player.getPosition());
        sendStars();
    }

    private void gameMenu() throws IOException {
        CommandHandler handler = new GameCommandHandler(receiveMessage());
        handler.handleMessage();
    }

    private void move(Direction dir, int distance) throws  IOException {
        if(game.movePlayer(id,  dir, distance)) {
            sendString("MOF " + player.getPosition() + " " + player.getPointsString());
            sendStars();
        } else {
            sendString("MOV " + player.getPosition());
            sendStars();
        }
    }

    private void leave() throws IOException {
        game.removePlayer(id);
        inGame = false;
        sendBye();
    }

    private void sendBye() throws IOException {
        sendString("BYE");
        sendStars();
    }

    private void sendList() throws IOException {
        sendString("GLIST! ");
        sendNumber(game.getNumPlayers());
        sendStars();
        for (String id: game.getPlayers()) {
            sendPlayerInfo(id);
        }
    }

    private void sendPlayerInfo(String id) throws IOException {
        sendString("GPLAYER " + id + " ");
        Player p = game.getPlayer(id);
        sendNumber(p.getX());
        sendString(" ");
        sendNumber(p.getY());
        sendString(" ");
        sendNumber(p.getPoints());
        sendStars();
    }

    private void sendAll(String mess) throws IOException {
        game.sendAll(this.id, mess);
    }

    private void send(String id, String mess) throws  IOException {
        if (game.send(id, this.id, mess)) {
            sendSend();
        } else {
            sendNosend();
        }
    }

    private void sendSend() throws IOException {
        sendString("SEND!");
        sendStars();
    }

    private void sendNosend() throws IOException {
        sendString("NOSEND");
        sendStars();
    }

    private class GameCommandHandler extends CommandHandler {
        public GameCommandHandler(ArrayList<Byte> input) {
            super(input);
        }

        public void handleMessage() throws IOException {
            String command = findNextWord();
            if (game.isOver()) {
                leave();
            }
            System.out.println(command); //todo remove
            switch (command) {
                case "UP":
                case "DOWN":
                case "LEFT":
                case "RIGHT":
                    index++;

                    int d = Integer.parseInt(findNextWord());

                    move(Direction.valueOf("command"), d);
                    break;

                case "QUIT":
                    leave();
                    break;

                case "GLIST?":
                    sendList();
                    break;

                case "ALL?":
                    index++;

                    String mess = findMessage();
                    if (mess.length() > 200) {
                        sendDunno();
                        break;
                    }

                    sendAll(mess);
                    break;

                case "SEND?":
                    index++;

                    String id = findNextWord();
                    if (id.length() == 0 || id.length() > 8) {
                        sendDunno();
                        break;
                    }

                    index++;

                    mess = findMessage();
                    if (mess.length() > 200) {
                        sendDunno();
                        break;
                    }

                    send(id, mess);
                    break;
                default:
                    sendDunno();
                    break;
            }
        }
    }
}
