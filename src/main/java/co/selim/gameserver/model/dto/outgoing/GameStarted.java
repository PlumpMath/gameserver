package co.selim.gameserver.model.dto.outgoing;

public class GameStarted {
    private String type ="gameStarted";
    private float width;
    private float height;
    private String playerName;
    private String skin;
    private String id;

    public GameStarted(float width, float height, String playerName, String skin, String id) {
        this.width = width;
        this.height = height;
        this.playerName = playerName;
        this.skin = skin;
        this.id = id;
    }
}
