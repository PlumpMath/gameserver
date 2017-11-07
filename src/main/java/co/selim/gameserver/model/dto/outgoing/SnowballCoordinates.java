package co.selim.gameserver.model.dto.outgoing;

public class SnowballCoordinates {
    private final String type = "snowballChanged";
    private final double x;
    private final double y;
    private boolean deleted;
    private String id;

    public SnowballCoordinates(double x, double y, boolean deleted, String id) {
        this.x = x;
        this.y = y;
        this.deleted = deleted;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
