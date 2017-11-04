package co.selim.gameserver.model;

public class Coordinates {
    private final String type;
    private final int x;
    private final int y;

    public Coordinates(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
