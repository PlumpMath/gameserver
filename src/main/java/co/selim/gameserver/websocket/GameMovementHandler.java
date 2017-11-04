package co.selim.gameserver.websocket;

import co.selim.gameserver.Coordinates;
import co.selim.gameserver.Movement;
import co.selim.gameserver.entity.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class GameMovementHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private Map<Session, Player> players = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void connected(Session session) {
        System.err.println("CONNECTED");
        players.put(session, new Player(session));
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        players.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        Movement movement = gson.fromJson(message, Movement.class);
        Player player = players.get(session);
        player.move(movement.getxDirection(), movement.getyDirection());

        session.getRemote()
                .sendString(gson.toJson(new Coordinates("playerMoved", player.getX(), player.getY
                        ())));
    }
}
