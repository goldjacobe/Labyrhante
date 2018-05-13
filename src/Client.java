import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jacobgold on 5/12/18.
 */
public class Client extends TCPHandler {
    public static void main(String[] args) {
        String host = "localhost";
        int port = Server.PORT;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        try {
            Client client = new Client(host, port);
            client.setUpUDP();
            client.handleConnection();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private int port;
    private boolean inGame;
    private boolean started;
    private Scanner input;

    public Client(String host, int port) throws IOException {
        super(new Socket(host, port));
        input = new Scanner(System.in);
        inGame = false;
    }

    private void handleConnection() throws IOException {
        while(true) {
            handleMessage();
            sendRequest();
        }
    }

    private void setUpUDP() {
        try {
            DatagramSocket socket = new DatagramSocket();
            this.port = socket.getLocalPort();
            new Thread(new ServiceUDP(socket)).start();
        } catch (SocketException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void handleMessage() throws IOException {
        CommandHandler handler = new MessageHandler(receiveMessage());
        handler.handleMessage();
    }

    private class MessageHandler extends CommandHandler {

        public MessageHandler(ArrayList<Byte> input) {
            super(input);
        }

        public void handleMessage() throws IOException {
            String type = findNextWord();
            switch(type) {
                case "GAMES":
                    index++;

                    int n = findNextValue();

                    games(n);
                    break;

                case "GAME":
                    index++;

                    int m = findNextValue();

                    index++;

                    int s = findNextValue();

                    game(m, s);
                    break;

                case "REGOK":
                    index ++;

                    m = findNextValue();

                    regOk(m);
                    break;

                case "REGNO":
                    regNo();
                    break;

                case "UNREGOK":
                    index++;

                    m = findNextValue();

                    unregOk(m);
                    break;

                case "SIZE!":
                    index++;

                    m = findNextValue();

                    index++;

                    int h = findNextValue();

                    index++;

                    int w = findNextValue();

                    size(m, h, w);
                    break;

                case "LIST!":
                    index++;

                    m = findNextValue();

                    index++;

                    s = findNextValue();

                    list(m, s);
                    break;

                case "PLAYER":
                    index++;

                    String id = findNextWord();

                    player(id);
                    break;

                case "WELCOME":
                    index++;

                    m = findNextValue();

                    index++;

                    h = findNextValue();

                    index++;

                    w = findNextValue();

                    index++;

                    int f = findNextValue();

                    index++;

                    String ip = findNextWord();

                    index++;

                    int port = Integer.parseInt(findNextWord());

                    welcome(m, h, w, f, ip, port);
                    break;

                case "POS":
                    index++;

                    id = findNextWord();

                    index++;

                    int x = Integer.parseInt(findNextWord());

                    index++;

                    int y = Integer.parseInt(findNextWord());

                    pos(id, x, y);
                    break;

                case "MOV":
                    index++;

                    x = Integer.parseInt(findNextWord());

                    index++;

                    y = Integer.parseInt(findNextWord());

                    mov(x, y);
                    break;

                case "MOF":
                    index++;

                    x = Integer.parseInt(findNextWord());

                    index++;

                    y = Integer.parseInt(findNextWord());

                    index++;

                    int p = Integer.parseInt(findNextWord());

                    mof(x, y, p);
                    break;

                case "GLIST!":
                    index++;

                    s = findNextValue();

                    gList(s);
                    break;

                case "GPLAYER":
                    index++;

                    id = findNextWord();

                    index++;

                    x = Integer.parseInt(findNextWord());

                    index++;

                    y = Integer.parseInt(findNextWord());

                    index++;

                    p = Integer.parseInt(findNextWord());

                    gPlayer(id, x, y, p);
                    break;

                case "ALL!":
                    allSent();
                    break;

                case "SEND!":
                    sent();
                    break;

                case "NOSEND":
                    notSent();
                    break;

                case "BYE":
                    bye();
                    break;

                case "DUNNO":
                    dunno();
                    break;
            }
            System.out.flush();
        }
    }


    private void games(int n) throws IOException {
        System.out.println("Parties  :");
        for (int i = 0; i < n; i++) {
            handleMessage();
        }
    }

    private void game(int m, int s) {
        System.out.println("Partie " + m + " : " + s + " joueurs");
    }

    private void regOk(int m) {
        this.inGame = true;
        System.out.println("Rejoint la partie " + m);
    }

    private void regNo() {
        System.out.println("Cette partie n'existe pas");
    }

    private void unregOk(int m) {
        this.inGame = false;
        System.out.println("Quitté la partie " + m);
    }

    private void size(int m, int h, int w) {
        System.out.println("Partie " + m + " : ");
        System.out.println("Hauteur : " + h);
        System.out.println("Largeur : " + w);
    }

    private void list(int m, int s) throws IOException {
        System.out.println("Joueurs de la partie " + m + " : ");
        for (int i = 0; i < s; i++) {
            handleMessage();
        }
    }

    private void player(String id) {
        System.out.println(id);
    }

    private void welcome(int m, int h, int w, int f, String ip, int port) throws IOException {
        ip = Utility.unpadAddressString(ip);
        new Thread(new ServiceMulti(port, ip)).start();
        this.started = true;
        System.out.println("Bienvenue à la partie " + m);
        System.out.println("Hauteur : " + h);
        System.out.println("Largeur : " + w);
        System.out.println("Fantômes : " + f);
        handleMessage();
    }

    private void pos(String id, int x, int y) {
        System.out.println(id + " @ (" + x + "," + y + ")");
    }

    private void mov(int x, int y) {
        System.out.println("@ (" + x + "," + y + ")");
    }

    private void mof(int x, int y, int p) {
        System.out.println("FANTÔME !");
        System.out.println("@ (" + x + "," + y + ")");
        System.out.println(p + " points");

    }

    private void gList(int s) throws IOException {
        System.out.println("Joueurs : ");
        for (int i = 0; i < s; i++) {
            handleMessage();
        }
    }

    private void gPlayer(String id, int x, int y, int p) {
        System.out.println(id + " @ (" + x + "," + y + ") : " + p + " points");
    }

    private void allSent() {
        System.out.println("Message envoyé !");
    }

    private void sent() {
        System.out.println("Message envoyé !");
    }

    private void notSent() {
        System.out.println("Je ne connais pas ce joueur");
    }

    private void bye() {
        System.out.println("Au revoir !");
    }

    private void dunno() {
        System.out.println("Je ne comprends pas cette demande");
    }

    private void sendRequest() throws IOException {
        printOptions();
        chooseCommand();
    }

    private void printOptions() {
        System.out.println("Options :");
        if (!started) {
            System.out.println("Demander la taille du labyrinthe d'une partie : \"size\"");
            System.out.println("Demander la liste des joueurs d'une partie : \"list\"");
            System.out.println("Demander la liste des parties : \"games\"");
            if(!inGame) {
                System.out.println("Nouvelle partie : \"new\"");
                System.out.println("Rejoindre partie : \"reg\"");
            } else {
                System.out.println("Quitter partie : \"unreg\"");
                System.out.println("Commencer la partie (Tous les joueurs inscrits doivent commencer : \"start\"");
            }
        } else {
            System.out.println("Monter : \"up\"");
            System.out.println("Descendre : \"down\"");
            System.out.println("Déplacer vers la gauche : \"left\"");
            System.out.println("Déplacer vers la droite : \"right\"");
            System.out.println("Quitter : \"quit\"");
            System.out.println("Demander la liste des joueurs : \"glist\"");
            System.out.println("Envoyer un message à tous les joueurs : \"all\"");
            System.out.println("Envoyer un message à un joueur : \"send\"");
        }
    }

    private void chooseCommand() throws IOException {
        String command = input.nextLine();
        switch(command) {
            case "size":
                askSize();
                break;
            case "list":
                askList();
                break;
            case "games":
                askGames();
                break;
            case "new":
                makeGame();
                break;
            case "reg":
                joinGame();
                break;
            case "unreg":
                leaveGame();
                break;
            case "start":
                start();
                break;
            case "up":
            case "down":
            case "left":
            case "right":
                move(command.toUpperCase());
                break;
            case "quit":
                quit();
                break;
            case "glist":
                askGlist();
                break;
            case "all":
                sendAll();
                break;
            case "send":
                send();
                break;
            default:
                sendRequest();
                break;
        }
    }

    private void askSize() throws IOException {
        if (started) {
            sendRequest();
            return;
        }

        System.out.println("De quelle partie ?");
        int m = Integer.parseInt(input.nextLine());

        sendString("SIZE? ");
        sendNumber(m);
        sendStars();
    }

    private void askList() throws  IOException {
        if (started) {
            sendRequest();
            return;
        }

        System.out.println("De quelle partie ?");
        int m = Integer.parseInt(input.nextLine());

        sendString("LIST? ");
        sendNumber(m);
        sendStars();

    }

    private void askGames() throws IOException {
        if (started) {
            sendRequest();
            return;
        }

        sendString("GAMES? ");
        sendStars();
    }

    private void makeGame() throws IOException {
        if (started || inGame) {
            sendRequest();
            return;
        }

        System.out.println("Votre identifiant ?");
        String id = input.nextLine();
        if (id.length() > 8) {
            System.out.println("8 caractères ou moins svp !");
            makeGame();
            return;
        }

        sendString("NEW ");
        sendString(id);
        sendString(" ");
        sendString(Utility.pad(port));
        sendStars();
    }

    private void joinGame() throws IOException {
        if (started || inGame) {
            sendRequest();
            return;
        }

        System.out.println("Votre identifiant ?");
        String id = input.nextLine();
        if (id.length() > 8) {
            System.out.println("8 caractères ou moins svp !");
            joinGame();
        }

        System.out.println("Quelle partie ?");
        int m = Integer.parseInt(input.nextLine());

        sendString("NEW ");
        sendString(id);
        sendString(" ");
        sendString(Utility.pad(port));
        sendString(" ");
        sendNumber(m);
        sendStars();
    }

    private void leaveGame() throws IOException {
        if (started || !inGame) {
            sendRequest();
            return;
        }

        sendString("UNREG");
        sendStars();
    }

    private void start() throws IOException {
        if (started || !inGame) {
            sendRequest();
            return;
        }

        sendString("START");
        sendStars();
    }

    private void move(String direction) throws IOException {
        if (!started) {
            sendRequest();
            return;
        }

        System.out.println("Combien de cases?");
        int d = Integer.parseInt(input.nextLine());

        sendString(direction);
        System.out.println(direction); //todo remove
        sendString(" ");
        System.out.println(d); //todo remove
        sendNumber(d);
        sendStars();
    }

    private void quit() throws IOException {
        if (!started) {
            sendRequest();
            return;
        }

        sendString("QUIT");
        sendStars();

    }

    private void askGlist() throws IOException {
        if (!started) {
            sendRequest();
            return;
        }

        sendString("GLIST?");
        sendStars();
    }

    private void sendAll() throws IOException {
        if (!started) {
            sendRequest();
            return;
        }

        System.out.println("Tapez votre message :");
        String mess = input.nextLine();
        if (mess.length() > 200) {
            System.out.println("200 caractères ou moins svp !");
            sendAll();
            return;
        }

        sendString("ALL? ");
        sendString(mess);
        sendStars();
    }

    private void send() throws IOException {
        if (!started) {
            sendRequest();
            return;
        }

        System.out.println("À qui ?");
        String id = input.nextLine();
        if (id.length() > 8) {
            System.out.println("8 caractères ou moins svp !");
            send();
        }

        System.out.println("Tapez votre message :");
        String mess = input.nextLine();
        if (mess.length() > 200) {
            System.out.println("200 caractères ou moins svp !");
            send();
            return;
        }

        sendString("MESS? ");
        sendString(id);
        sendString(" ");
        sendString(mess);
        sendStars();
    }
}
