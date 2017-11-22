package co.selim.gameserver.entity;

import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tree implements GameEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tree.class);
    private final TreeType treeType;
    private final Vector2 position;

    public Tree(TreeType treeType, Vector2 position) {
        this.treeType = treeType;
        this.position = position;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collided(GameEntity other) {
        LOGGER.info("Someone collided with a tree");
    }

    @Override
    public Type getType() {
        return Type.TREE;
    }

    public TreeType getTreeType() {
        return treeType;
    }

    public Vector2 getPosition() {
        return position;
    }

    public enum TreeType {
        PINALE {
            @Override
            public Vector2 getSize() {
                return new Vector2(90, 40);
            }
        }, BROADLEAF {
            @Override
            public Vector2 getSize() {
                return new Vector2(30, 40);
            }
        };

        public abstract Vector2 getSize();
    }
}
