package co.selim.gameserver.websocket;

import co.selim.gameserver.messaging.Messenger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketMessenger implements Messenger {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessenger.class);
    private static Set<Session> ALL_SESSIONS = ConcurrentHashMap.newKeySet();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();
    private final Session session;

    public WebSocketMessenger(Session session) {
        this.session = session;
        ALL_SESSIONS.add(session);
    }

    private static void doSendMessage(Session s, Object obj) {
        try {
            s.getRemote()
                    .sendString(GSON.toJson(obj));
        } catch (IOException e) {
            if (s.isOpen()) {
                LOGGER.error("Error while sending message", e);
            }
        }
    }

    @Override
    public void sendMessage(Object obj) {
        doSendMessage(session, obj);
    }

    @Override
    public void broadCast(Object obj) {
        ALL_SESSIONS.forEach(s -> {
            doSendMessage(s, obj);
        });
    }

    public void removeSession(Session s) {
        ALL_SESSIONS.remove(s);
    }
}
