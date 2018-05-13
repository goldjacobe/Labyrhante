import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jacobgold on 5/12/18.
 */
public class TCPHandler {
    protected final InputStream is;
    protected final OutputStream os;

    public TCPHandler(Socket s) throws IOException {
        is = s.getInputStream();
        os = s.getOutputStream();
    }

    protected void sendDunno() throws IOException {
        sendString("DUNNO");
        sendStars();
    }

    protected void sendStars() throws IOException {
        sendString("***");
        os.flush();
    }

    protected void sendString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sendCharAscii(c);
        }
    }

    protected synchronized void sendNumber(int i) throws IOException {
        byte[] toSend = Utility.intToTwoOctets(i);
        //System.out.println("SENDING LITTLE ENDIAN: " + i); //todo remove
        os.write(toSend[0]);
        os.write(toSend[1]);
        os.flush();
    }

    protected synchronized void sendCharAscii(char c) throws IOException {
        //System.out.println("SENDING CHAR: " + (byte)c); //todo remove
        os.write((byte) c);
        os.flush();
    }

    protected ArrayList<Byte> receiveMessage() throws IOException {
        int numStars = 0;
        ArrayList<Byte> out = new ArrayList<>();
        while (numStars < 3) {
            synchronized (is) {
                int i;
                i = is.read();
                //System.out.println(i); //todo remove
                if (i != -1) {
                    out.add((byte) i);
                    if ((char)i == '*') {
                        ++numStars;
                    } else {
                        numStars = 0;
                    }
                }
            }

        }
        /*
        //todo remove all this
        int j=0;
        byte[] bytes = new byte[out.size()];
        for(Object o: out.toArray())
            bytes[j++] = (Byte)o;
        System.out.println(new String(bytes));
        //todo to here
        */
        return out;
    }

    protected abstract class CommandHandler {

        public CommandHandler(ArrayList<Byte> input) {
            this.input = input;
            this.index = 0;

        }

        protected final ArrayList<Byte> input;
        protected int index;

        public abstract void handleMessage() throws IOException;

        protected String findNextWord() {
            StringBuilder sb = new StringBuilder();
            while (input.get(index) != (byte) ('*') && input.get(index) != (byte) (' ')) {
                sb.append((char) input.get(index++).byteValue());
            }
            return sb.toString();

        }

        protected String findMessage() {
            StringBuilder sb = new StringBuilder();
            while (input.get(index) != (byte) ('*')) {
                sb.append((char) input.get(index++).byteValue());
            }
            return sb.toString();

        }

        protected int findNextValue() {
            byte[] bytes = new byte[2];
            try {
                bytes[0] = input.get(index++);
                bytes[1] = input.get(index++);
                return Utility.twoOctetstoInt(bytes);
            } catch (IndexOutOfBoundsException e) {
                return -1;
            }
        }

    }
}
