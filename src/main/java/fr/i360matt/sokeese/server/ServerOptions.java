package fr.i360matt.sokeese.server;

/**
 * Allows to modificate the server behavor
 *
 * @version 1.0.0
 */
public class ServerOptions {

    public enum Level {

        ALL (3),
       /* GROUP (2), */
        SINGLE (1),
        NOTHING (0);

        private final int level;
        public int getLevel () {
            return this.level;
        }

        Level (final int level) {
            this.level = level;
        }
    }

    protected Level levelMessages = Level.SINGLE;

    protected int maxClients = 50;

    protected int maxSameClient = 10;
    /*  protected int maxClientsPerGroup = 50; */


    public final void setLevelMessages (final Level levelMessages) {
        this.levelMessages = levelMessages;
    }
    public final void setMaxClients (final int maxClients) {
        this.maxClients = maxClients;
    }
    public final void setMaxSameClient (final int maxSameClient) {
        this.maxSameClient = maxSameClient;
    }
    /*  public final void setMaxClientsPerGroup (final int maxClientsPerGroup) {
        this.maxClientsPerGroup = maxClientsPerGroup;
    } */

    public final Level getLevelMessages () {
        return this.levelMessages;
    }
    public final int getMaxClients () {
        return maxClients;
    }
    public final int getMaxSameClient () {
        return maxSameClient;
    }
    /* public final int getMaxClientsPerGroup () {
        return maxClientsPerGroup;
    } */
}
