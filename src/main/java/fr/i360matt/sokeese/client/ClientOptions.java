package fr.i360matt.sokeese.client;

/**
 * Allows to modificate the client behavor
 *
 * @version 1.1.0
 */
public class ClientOptions {

    protected int retryDelay = 100;
    protected int maxRetry = 20;
    protected boolean debug = false;


    public final void setRetryDelay (final int delay) {
        this.retryDelay = delay;
    }
    public final void setMaxRetry (final int maxRetry) {
        this.maxRetry = maxRetry;
    }
    public final void setDebug (final boolean debug) {
        this.debug = debug;
    }

    public final int getRetryDelay () {
        return this.retryDelay;
    }
    public final int getMaxRetry () {
        return this.maxRetry;
    }
    public final boolean getDebug () {
        return this.debug;
    }

}
