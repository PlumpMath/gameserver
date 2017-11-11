package co.selim.gameserver.executor;

interface GameTask extends Runnable {
    boolean isDone();
}
