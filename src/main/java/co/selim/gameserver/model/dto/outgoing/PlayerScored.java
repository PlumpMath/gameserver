package co.selim.gameserver.model.dto.outgoing;

public class PlayerScored {
    private final String type = "playerScored";
    private final String playerId;
    private final int scoreDelta;
    private final transient int DELTA = 10;

    public PlayerScored(String playerId, ScoreType scoreType) {
        this.playerId = playerId;
        this.scoreDelta = scoreType.getDelta();
    }

    public enum ScoreType {
        HIT {
            @Override
            public int getDelta() {
                return 10;
            }
        }, GOT_HIT {
            @Override
            public int getDelta() {
                return -10;
            }
        };

        public abstract int getDelta();
    }
}
