package co.selim.gameserver.model;

public class Coordinates {
    private final String type;
    private final double x;
    private final double y;

    public Coordinates(String type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
