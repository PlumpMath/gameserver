package co.selim.gameserver.model.dto.outgoing;

public class PlayerCoordinates {
    private final String type = "playerMoved";
    private final double x;
    private final double y;

    public PlayerCoordinates(double x, double y) {
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
