package co.selim.gameserver.messaging;

import co.selim.gameserver.entity.Player;

public interface Messenger {
    void sendMessage(Object obj);

    void broadCast(Object obj);

    void broadCastToOthers(Player player, Object obj);
}
