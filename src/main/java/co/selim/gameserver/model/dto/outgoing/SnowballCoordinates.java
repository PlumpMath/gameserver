package co.selim.gameserver.model.dto.outgoing;

public class SnowballCoordinates {
    private final String type = "snowballChanged";
    private final float x;
    private final float y;
    private float angle;
    private float velocity;
    private boolean deleted;
    private String id;

    public SnowballCoordinates(float x, float y, float angle, float velocity, boolean deleted,
                               String id) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.deleted = deleted;
        this.id = id;
    }
}
