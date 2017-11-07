package co.selim.gameserver.handlers;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.model.dto.incoming.Movement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MovementHandler implements GameHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();

    @Override
    public void handle(Player player, String message) {
        Movement movement = GSON.fromJson(message, Movement.class);
        player.move(movement.getxDirection(), movement.getyDirection());
    }
}
