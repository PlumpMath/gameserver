package co.selim.gameserver.model.dto.outgoing;

public class PlayerMoved {
    private final String type = "playerMoved";
    private final float x;
    private final float y;
    private final float angle;
    private final float velocity;
    private final String id;

    public PlayerMoved(float x, float y, float angle, float velocity, String id) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.id = id;
    }
}
