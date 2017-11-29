package co.selim.gameserver.websocket;

import co.selim.gameserver.messaging.Messenger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketMessenger implements Messenger {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessenger.class);
    private final BlockingQueue<Object> pendingMessages = new LinkedBlockingQueue<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();
    private final Session session;
    private static Set<Messenger> ALL_MESSENGERS = ConcurrentHashMap.newKeySet();
    private final Runnable messageQueueTask = () -> {
        while (getSession().isOpen()) {
            try {
                Object nextMessage = pendingMessages.take();
                doSendMessage(getSession(), nextMessage);
            } catch (InterruptedException e) {
                LOGGER.error("Error in WebSocketMessenger ", e);
            }
        }
    };

    public WebSocketMessenger(Session session) {
        this.session = session;
        ALL_MESSENGERS.add(this);
        new Thread(messageQueueTask, "MessageQueueThread").start();
    }

    private static void doSendMessage(Session s, Object obj) {
        try {
            if (s.isOpen()) {
                s.getRemote()
                        .sendString(GSON.toJson(obj));
            }
        } catch (IOException e) {
            if (s.isOpen()) {
                LOGGER.error("Error while sending message", e);
            }
        }
    }

    @Override
    public void sendMessage(Object obj) {
        pendingMessages.add(obj);
    }

    @Override
    public void broadCast(Object obj) {
        sendMessage(obj);
        broadCastToOthers(obj);
    }

    @Override
    public void broadCastToOthers(Object obj) {
        ALL_MESSENGERS.stream()
                .filter(messenger -> !this.equals(messenger))
                .forEach(messenger -> messenger.sendMessage(obj));
    }

    public void removeFromBroadcastList() {
        ALL_MESSENGERS.remove(this);
    }

    private Session getSession() {
        return session;
    }
}
