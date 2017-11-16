package co.selim.gameserver.model.dto.outgoing;

public class PlayerStopped {
    private final String type = "playerStopped";
    private final float x;
    private final float y;
    private final String id;

    public PlayerStopped(float x, float y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }
}
