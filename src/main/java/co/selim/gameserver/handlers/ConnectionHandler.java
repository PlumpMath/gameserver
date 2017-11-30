package co.selim.gameserver.handlers;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.incoming.ConnectionRequest;
import co.selim.gameserver.model.dto.outgoing.GameStarted;
import co.selim.gameserver.model.dto.outgoing.PlayerConnected;
import co.selim.gameserver.model.dto.outgoing.PlayerStopped;
import co.selim.gameserver.websocket.WebSocketHandler;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConnectionHandler implements GameHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();

    @Override
    public void handle(Player player, String message) {
        ConnectionRequest request = GSON.fromJson(message, ConnectionRequest.class);
        player.setName(request.getPlayerName());
        player.setSkin(request.getSkin());
        player.sendMessage(new GameStarted(GameMap.MAP_SIZE.x, GameMap.MAP_SIZE.y, player,
                WebSocketHandler.getAllPlayers()));
        player.setGameStarted();
        Vector2 position = player.getPosition();
        player.broadCastToOthers(new PlayerConnected(player.getId(), position.x, position.y,
                player.getSkin(), player.getName()));
        player.broadCastMessage(new PlayerStopped(position.x, position.y, player.getId()));
    }
}
