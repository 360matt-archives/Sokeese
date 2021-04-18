package fr.i360matt.sokeese.client;

import fr.i360matt.sokeese.commons.requests.Reply;

import java.util.function.BiConsumer;

/**
 * Send queue structure.
 * @version 1.0.0
 */
public class Queue {
    public Object objToSend;
    public int delay;
    public BiConsumer<Reply, Boolean> consumer;
}
