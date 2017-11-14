package co.selim.gameserver.model.dto.incoming;

public class ConnectionRequest {
    private String playerName;
    private String skin;

    public ConnectionRequest(String playerName, String skin) {
        this.playerName = playerName;
        this.skin = skin;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getSkin() {
        return skin;
    }
}
