/**
 * Created by jacobgold on 5/12/18.
 */
public class Ghost {
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getPosition() {
        return padLoc(getX()) + " " + padLoc(getY());
    }

    private static String padLoc(int i) {
        return ("" + (i + 1000)).substring(1);
    }
}
