package co.selim.gameserver.websocket;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.handlers.MovementHandler;
import co.selim.gameserver.handlers.SnowballHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();
    private static final Map<Session, Player> players = new ConcurrentHashMap<>();
    private static final Map<String, co.selim.gameserver.handlers.GameHandler> handlers = new
            HashMap<>();

    static {
        handlers.put("movePlayer", new MovementHandler());
        handlers.put("throwBall", new SnowballHandler());
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        String address = session.getRemoteAddress()
                .getHostString();
        LOGGER.info(address + " connected");
        players.put(session, new Player(address, (msg) -> {
            try {
                session.getRemote()
                        .sendString(msg);
            } catch (IOException e) {
                if (session.isOpen()) {
                    LOGGER.error("Error while sending message to " + address, e);
                }
            }
        }));
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        LOGGER.info(session.getRemoteAddress()
                .getHostString() + " disconnected");
        players.remove(session)
                .disconnect();
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        Player player = players.get(session);

        JsonParser jsonParser = new JsonParser();

        LOGGER.info("Received " + message + " from " + session.getRemoteAddress()
                .getHostString());

        String messageType = jsonParser.parse(message)
                .getAsJsonObject()
                .get("type")
                .getAsString();

        co.selim.gameserver.handlers.GameHandler handler = handlers.get(messageType);
        if (Objects.nonNull(handler)) {
            handler.handle(player, message);
        }
    }
}
