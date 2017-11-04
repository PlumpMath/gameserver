package co.selim.gameserver.handlers;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.model.MouseClick;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SnowballHandler implements GameHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .create();

    @Override
    public void handle(Player player, String message) {
        MouseClick mouseClick = GSON.fromJson(message, MouseClick.class);
        player.throwSnowball(mouseClick.getPointerX(), mouseClick.getPointerY());
    }
}
