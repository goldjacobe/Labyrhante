import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Game implements Runnable {

    private final int number;
    private final Server server;

    private final HashMap<String, Player> players;
    private final Ghost[] ghosts;

    private final Labyrinth labyrinth;

    private static final int NUM_GHOSTS = 5;
    private final int height;
    private final int width;
    private int numGhosts;

    private final InetAddress multicastAddress;
    private final int multicastPort;

    private DatagramSocket ds;

    public Game (int number, Server server) throws UnknownHostException {
        this.number = number;
        this.multicastAddress = InetAddress.getByAddress(new byte[]{(byte)239, (byte)255, (byte)(number/256), (byte)(number)});
        this.multicastPort = 5000 + number;
        this.height = server.getHeight();
        this.width = server.getWidth();
        players = new HashMap<>();

        this.labyrinth = new Labyrinth(height, width);

        numGhosts = NUM_GHOSTS;
        this.ghosts = new Ghost[numGhosts];
        for (int i = 0; i < numGhosts; i++) {
            ghosts[i] = new Ghost();
        }

        this.server = server;
    }

    public int getNumber() {
        return number;
    }

    public int getNumPlayers() {
        return players.size();
    }

    public Collection<String> getPlayers() {
        return players.keySet();
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public boolean addPlayer(String id, Player player) {
        synchronized (players) {
        return players.putIfAbsent(id, player) == null;
        }
    }

    public void run() {
        while(!started());

        server.startGame(number);
        setUpUDP();
        setUpGame();
        spawnGameHandlers();

        int i = 0;

        while(!isOver()) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                System.out.println(e);
                e.printStackTrace();
            }
            try {
                Ghost g = ghosts[i++ % ghosts.length];
                moveGhost(g);
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

    }

    private void setUpUDP() {
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void spawnGameHandlers() {
        for (Map.Entry<String, Player> e: players.entrySet()) {
            String id = e.getKey();
            Player p = e.getValue();
            p.getLobbyThread().interrupt();
            try {
                PlayerGameHandler handler = new PlayerGameHandler(this, p, id);
                Thread t = new Thread(handler);
                t.start();
            } catch(IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    }

    private void setUpGame() {
        players.values().forEach((p) -> choosePosition(p));
        for(int i = 0; i < numGhosts; i++) {
            createGhost();
        }
    }

    private void choosePosition(Ghost g) {
        int x;
        int y;
        do {
            x = (int)(Math.random() * height);
            y = (int)(Math.random() * width);
        } while (!labyrinth.isOpen(x,y));
        g.setX(x);
        g.setY(y);
    }

    private void createGhost() {
        Ghost g = new Ghost();
        choosePosition(g);
    }

    private void moveGhost(Ghost g) throws IOException {
        if (g == null) {
            return;
        }
        choosePosition(g);
        castMove(g);
    }

    private void castMove(Ghost g) throws IOException {
        byte[] buffer = makeMoveBuffer(g);
        multicast(buffer);
    }

    private byte[] makeMoveBuffer(Ghost g) {
        byte[] out = new byte[15];
        int offset = 0;
        offset = putStringInBuffer("FANT ", out, offset);
        offset = putStringInBuffer(g.getPosition(), out, offset);
        putPlusInBuffer(out, offset);
        return out;

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getNumGhosts() {
        return ghosts.length;
    }

    public String getMulticastAddress() {
        return multicastAddress.getHostAddress();
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public boolean started() {
        if (players.isEmpty()) {
            return false;
        }

        boolean out = true;

        synchronized (players) {
            for (Player p : players.values()) {
                out &= p.isStarted();
            }

            return out;
        }
    }

    public void sendAll(String id, String mess) throws IOException {
        byte[] buffer = makeSendAllBuffer(id, mess);
        multicast(buffer);
    }

    private void multicast(byte[] buffer) throws  IOException {
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, multicastAddress, multicastPort);
        ds.send(dp);
    }

    private byte[] makeSendAllBuffer(String id, String mess) {
        byte[] out = new byte[id.length() + mess.length() + 9];
        int offset = 0;
        offset = putStringInBuffer("MESA ", out, offset);
        offset = putStringInBuffer(id, out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(mess, out, offset);
        putPlusInBuffer(out, offset);
        return out;
    }

    public boolean send(String id, String id2, String mess) throws IOException{
        Player p = players.get(id);
        if (p == null) {
            return false;
        }

        InetAddress address = p.getInetAddress();
        int port = p.getPort();
        byte[] buffer = makeSendBuffer(id2, mess);

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, port);
        ds.send(dp);
        return true;
    }

    private byte[] makeSendBuffer(String id2, String mess) {
        byte[] out = new byte[id2.length() + mess.length() + 9];
        int offset = 0;
        offset = putStringInBuffer("MESP ", out, offset);
        offset = putStringInBuffer(id2, out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(mess, out, offset);
        putPlusInBuffer(out, offset);
        return out;
    }

    private int putPlusInBuffer(byte[] buffer, int offset) {
        return putStringInBuffer("+++", buffer, offset);
    }

    private int putStringInBuffer(String s, byte[] buffer, int offset) {
        int i;
        for (i = 0; i < s.length() && offset + i < buffer.length; i++) {
            buffer[offset + i] = (byte)s.charAt(i);
        }
        return offset + i;
    }

    public void removePlayer(String id) {
        synchronized (players) {
            players.remove(id);
        }
    }


    public boolean isOver() {
        return numGhosts == 0;
    }

    private void end() throws IOException {
        int maxPoints = 0;
        String maxId = "";
        for (Map.Entry<String, Player> e: players.entrySet()){
            int p = e.getValue().getPoints();
            if (p > maxPoints) {
                maxPoints = p;
                maxId = e.getKey();
            }
        }
        byte[] buffer = makeEndBuffer(maxId);
        multicast(buffer);
    }

    private byte[] makeEndBuffer(String id) {
        byte[] out = new byte[12 + id.length()];
        int offset = 0;
        offset = putStringInBuffer("END ", out, offset);
        offset = putStringInBuffer(id, out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(players.get(id).getPointsString(), out, offset);
        putPlusInBuffer(out, offset);
        return out;
    }

    public synchronized boolean movePlayer(String id, String dir, int distance) throws  IOException {
        Player p = getPlayer(id);

        for (int i = 0; i < distance; i++) {
            int newX = p.getX();
            int newY = p.getY();
            switch (dir) {
                case "UP":
                    newX--;
                    break;
                case "DOWN":
                    newX++;
                    break;
                case "LEFT":
                    newY--;
                    break;
                case "RIGHT":
                    newY++;
                    break;
            }
            if (!labyrinth.isOpen(newX, newY)) {
                break;
            }
            p.setX(newX);
            p.setY(newY);
            for (int j = 0; j < ghosts.length; j++) {
                Ghost g = ghosts[j];
                if (g == null) {
                    continue;
                }
                if (newX == g.getX() && newY == g.getY()) {
                    p.incPoints();
                    castCapture(id);
                    ghosts[j] = null;
                    numGhosts--;
                    if (isOver()) {
                        end();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void castCapture(String id) throws  IOException {
        byte[] buffer = makeCaptureBuffer(id);
        multicast(buffer);
    }

    private byte[] makeCaptureBuffer(String id) {
        Player p = getPlayer(id);
        byte[] out = new byte[id.length() + 21];
        int offset = 0;
        offset = putStringInBuffer("SCOR ", out, offset);
        offset = putStringInBuffer(id, out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(p.getPointsString(), out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(p.getPosition(), out, offset);
        putPlusInBuffer(out, offset);
        return  out;
    }
}
