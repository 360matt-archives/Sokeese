package fr.i360matt.sokeese.client;

/**
 * Allows to modificate the client behavor
 *
 * @version 1.0.0
 */
public class ClientOptions {

    protected int retryDelay = 100;
    protected int maxRetry = 20;


    public final void setRetryDelay (final int delay) {
        this.retryDelay = delay;
    }
    public final void setMaxRetry (final int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public final int getRetryDelay () {
        return retryDelay;
    }
    public final int getMaxRetry () {
        return maxRetry;
    }

}
