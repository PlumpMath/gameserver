package co.selim.gameserver.websocket;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.handlers.ConnectionHandler;
import co.selim.gameserver.handlers.GameHandler;
import co.selim.gameserver.handlers.MovementHandler;
import co.selim.gameserver.handlers.SnowballHandler;
import co.selim.gameserver.model.GameMap;
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
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Map<Session, Player> PLAYERS = new ConcurrentHashMap<>();
    private static final Map<Session, WebSocketMessenger> MESSENGERS = new ConcurrentHashMap<>();
    private static final Map<String, GameHandler> HANDLERS = new HashMap<>();
    private static final GameMap MAP = new GameMap();

    static {
        HANDLERS.put("movePlayer", new MovementHandler());
        HANDLERS.put("throwBall", new SnowballHandler());
        HANDLERS.put("connectToGame", new ConnectionHandler());
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        String address = session.getRemoteAddress()
                .getHostString();
        LOGGER.info(address + " connected");
        WebSocketMessenger messenger = new WebSocketMessenger(session);
        Player player = new Player(address, MAP, messenger);
        PLAYERS.put(session, player);
        MESSENGERS.put(session, messenger);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        LOGGER.info(session.getRemoteAddress()
                .getHostString() + " disconnected");
        MESSENGERS.remove(session)
                .removeSession(session);
        PLAYERS.remove(session)
                .disconnect();
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        Player player = PLAYERS.get(session);

        LOGGER.info("Received " + message + " from " + session.getRemoteAddress()
                .getHostString());

        String messageType = JSON_PARSER.parse(message)
                .getAsJsonObject()
                .get("type")
                .getAsString();

        co.selim.gameserver.handlers.GameHandler handler = HANDLERS.get(messageType);
        if (Objects.nonNull(handler)) {
            handler.handle(player, message);
        }
    }
}
