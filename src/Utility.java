/**
 * Created by jacobgold on 5/9/18.
 */
public class Utility {
    public static byte[] intToTwoOctets(int i) {
        byte[] out = new byte[2];
        out[0] = (byte)(i % 256);
        i = (i - out[0]) / 256;
        out[1] = (byte)(i % 256);
        return out;
    }

    public static int twoOctetstoInt(byte[] o) {
        return 256 * o[1] + o[0];
    }

    public static String padAddressString(String address) {
        int numHashes = 15 - address.length();
        StringBuilder out = new StringBuilder(address);
        for (int i = 0; i < numHashes; i++) {
            out.append('#');
        }
        return out.toString();
    }

    public static String unpadAddressString(String address) {
        int pos = address.indexOf('#');
        if (pos > -1) {
            return address.substring(0, pos);
        }
        return address;
    }

    public static String pad(int i) {
        return i < 10000 ? ("" + (i + 10000)).substring(1) : "" + i;
    }

}
