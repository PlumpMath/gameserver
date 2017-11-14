package co.selim.gameserver.model.dto.outgoing;

public class GameStarted {
    private float width;
    private float height;
    private String playerName;
    private String skin;

    public GameStarted(float width, float height, String playerName, String skin) {
        this.width = width;
        this.height = height;
        this.playerName = playerName;
        this.skin = skin;
    }
}
