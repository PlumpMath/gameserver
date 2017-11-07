package co.selim.gameserver.model.dto.outgoing;

public class SnowballCoordinates {
    private final String type = "snowballChanged";
    private final float x;
    private final float y;
    private boolean deleted;
    private String id;

    public SnowballCoordinates(float x, float y, boolean deleted, String id) {
        this.x = x;
        this.y = y;
        this.deleted = deleted;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
