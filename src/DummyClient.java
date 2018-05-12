import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jacobgold on 5/10/18.
 */
public class DummyClient implements Runnable{

    public static void main(String[] args) throws IOException{
        Socket s = new Socket("localhost", Server.PORT);
        InputStream is = s.getInputStream();
        OutputStream os = s.getOutputStream();
        ArrayList<Byte> buffer = new ArrayList<>();
        Scanner k = new Scanner(System.in);
        int numStars = 0;
        int i;
        while (true) {
            while (true) {
                try {
                    i = is.read();
                } catch (IOException e) {
                    System.out.println(e);
                    e.printStackTrace();
                    i = -1;
                }
                if (i != -1) {
                    System.out.println(i);
                    buffer.add((byte) i);
                    if ((char) i == '*') {
                        numStars++;
                    } else {
                        numStars = 0;
                    }
                    if (numStars == 3) {
                        for (Byte b : buffer) {
                            System.out.print((char) b.byteValue());
                        }
                        System.out.println();
                        System.out.flush();
                        numStars = 0;
                        buffer.clear();
                        break;
                    }
                }
            }
            System.out.println("go");
            while (true) {
                String in = k.nextLine();
                char c = in.charAt(0);
                if (c == '0') {
                    os.write(Utility.intToTwoOctets(Integer.parseInt(in)));
                    os.flush();
                    numStars = 0;
                    System.out.println("sending (litEnd):" + Integer.parseInt(in));
                } else {
                    os.write((byte) c);
                    os.flush();
                    if (c == '*') {
                        numStars++;
                    } else {
                        numStars = 0;
                    }
                    System.out.println("sending (ASCII): " + c);
                }
                if (numStars == 3) {
                    numStars = 0;
                    System.out.println("sent");
                    break;
                }
            }
        }
    }

    private InputStream is;

    public DummyClient(InputStream is) throws IOException {
        this.is = is;
    }

    public void run() {
        ArrayList<Byte> buffer = new ArrayList<>();
        int numStars = 0;
        int i;
        while (true) {
            try {
                i = is.read();
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
                i = -1;
            }
            if (i != -1) {
                System.out.println(i);
                buffer.add((byte) i);
                if ((char)i == '*') {
                    numStars++;
                }
                if (numStars == 3) {
                    for (Byte b : buffer) {
                        System.out.print((char)b.byteValue());
                    }
                    System.out.println();
                    System.out.flush();
                    numStars = 3;
                    buffer.clear();
                }
            }

        }
    }
}
