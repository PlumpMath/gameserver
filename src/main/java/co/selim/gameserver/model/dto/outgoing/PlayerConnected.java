package co.selim.gameserver.model.dto.outgoing;

public class PlayerConnected {
    private final String type = "playerConnected";
    private final String id;
    private final float x;
    private final float y;
    private final String skin;
    private final String name;

    public PlayerConnected(String id, float x, float y, String skin, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.skin = skin;
        this.name = name;
    }
}
