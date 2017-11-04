package co.selim.gameserver.model;

public enum Tile {
    SNOW {
        @Override
        public boolean canBeEntered() {
            return true;
        }
    },
    WALL {
        @Override
        public boolean canBeEntered() {
            return false;
        }
    };

    public abstract boolean canBeEntered();
}
