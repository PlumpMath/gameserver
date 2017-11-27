package co.selim.gameserver.model.dto.outgoing;

public class SnowballCount {
    private final String type = "snowballCountChanged";
    private final int newCount;

    public SnowballCount(int newCount) {
        this.newCount = newCount;
    }
}
