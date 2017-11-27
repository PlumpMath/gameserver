package co.selim.gameserver.model.dto.outgoing;

import co.selim.gameserver.entity.Player;
import co.selim.gameserver.entity.Tree;
import co.selim.gameserver.model.GameMap;
import com.badlogic.gdx.math.Vector2;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GameStarted {
    private final String type = "gameStarted";
    private final int maxSnowballs = 5;
    private final float width;
    private final float height;
    private final String playerName;
    private final String skin;
    private final String id;
    private final List<PlayerInfo> playersInfo;
    private final List<TreeInfo> trees;

    public GameStarted(float width, float height, Player player, Collection<Player> otherPlayers) {
        this.width = width;
        this.height = height;
        this.playerName = player.getName();
        this.skin = player.getSkin();
        this.id = player.getId();
        playersInfo = otherPlayers.stream()
                .filter(p -> !player.equals(p))
                .filter(Player::isConnected)
                .map(p -> new PlayerInfo(p.getPosition().x, p.getPosition().y, p.getName(), p
                        .getId(), p.getSkin()))
                .collect(Collectors.toList());
        trees = GameMap.getTrees()
                .stream()
                .map(t -> new TreeInfo(t.getPosition(), t.getTreeType()
                        .getSize(), t.getTreeType()))
                .collect(Collectors.toList());
    }

    private static class PlayerInfo {
        private final float x;
        private final float y;
        private final String name;
        private final String id;
        private final String skin;

        public PlayerInfo(float x, float y, String name, String id, String skin) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.id = id;
            this.skin = skin;
        }
    }

    private static class TreeInfo {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final String type;

        public TreeInfo(Vector2 pos, Vector2 size, Tree.TreeType type) {
            this.x = pos.x;
            this.y = pos.y;
            this.width = size.x;
            this.height = size.y;
            this.type = type.name()
                    .toLowerCase();
        }
    }
}
