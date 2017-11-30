package co.selim.gameserver.model.dto.outgoing;

public class PlayerScored {
    private final String type = "playerScored";
    private final String playerId;
    private final int scoreDelta;

    public PlayerScored(String playerId, int scoreDelta) {
        this.playerId = playerId;
        this.scoreDelta = scoreDelta;
    }
}
