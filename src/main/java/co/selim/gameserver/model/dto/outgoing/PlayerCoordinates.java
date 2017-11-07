package co.selim.gameserver.model.dto.outgoing;

public class PlayerCoordinates {
    private final String type = "playerMoved";
    private final float x;
    private final float y;

    public PlayerCoordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
