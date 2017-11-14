package co.selim.gameserver.handlers;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.incoming.ConnectionRequest;
import co.selim.gameserver.model.dto.outgoing.GameStarted;
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
        player.sendMessage(GSON.toJson(new GameStarted(GameMap.MAP_SIZE.x, GameMap.MAP_SIZE.y,
                player.getName(), player.getSkin())));
    }
}
