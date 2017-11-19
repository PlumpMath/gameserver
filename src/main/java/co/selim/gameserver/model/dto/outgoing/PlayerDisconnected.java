package co.selim.gameserver.model.dto.outgoing;

public class PlayerDisconnected {
    private final String type = "playerDisconnected";
    private final String id;

    public PlayerDisconnected(String id) {
        this.id = id;
    }
}
