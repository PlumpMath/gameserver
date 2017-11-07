package co.selim.gameserver.executor;

import java.util.function.Supplier;

public interface GameTask extends Runnable {
    boolean isDone();
}
