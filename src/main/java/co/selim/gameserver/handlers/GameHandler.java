package co.selim.gameserver.handlers;

import co.selim.gameserver.entity.Player;

public interface GameHandler {
    void handle(Player player, String message);
}
