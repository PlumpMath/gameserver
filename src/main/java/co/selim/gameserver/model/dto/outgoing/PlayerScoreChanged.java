package co.selim.gameserver.model.dto.outgoing;

public class PlayerScoreChanged {
    private final String type = "playerScoreChanged";
    private final String playerId;
    private final int newScore;

    public PlayerScoreChanged(String playerId, int newScore) {
        this.playerId = playerId;
        this.newScore = newScore;
    }
}
