package co.selim.gameserver.messaging;

public interface Messenger {
    void sendMessage(Object obj);

    void broadCast(Object obj);
}
