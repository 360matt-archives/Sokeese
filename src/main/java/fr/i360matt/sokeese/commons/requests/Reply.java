package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Structure of an Reply request.
 * Serializable.
 *
 * @version 1.1.0
 */
public final class Reply implements Serializable {
    private static final long serialVersionUID = 472144737983269550L;

    protected long idRequest;

    protected String recipient;
    protected String sender;

    protected String channel;
    protected Object content;



    /**
     * This method, used in the internal API, allows to assign a response id to the request.
     * @param idRequest Response ID.
     */
    public final void setIdRequest (final long idRequest) {
        this.idRequest = idRequest;
    }

    /**
     * This method, used in the internal API, allows to retrive a response id for the request.
     * @return Response ID.
     */
    public final long getIdRequest () {
        return idRequest;
    }

    /**
     * Allows to define the recipient's name.
     * @param recipient The recipient's name.
     */
    public final void setRecipient (final String recipient) {
        this.recipient = recipient;
    }

    /**
     * Allows to retrieve the recipient's name.
     * @return The recipient's name.
     */
    public final String getRecipient () {
        return recipient;
    }

    /**
     * Allows to define the sender's name.
     * @param sender The sender's name.
     */
    public final void setSender (final String sender) {
        this.sender = sender;
    }

    /**
     * Allows to retrieve the sender's name.
     * @return The sender's name.
     */
    public final String getSender () {
        return sender;
    }

    /**
     * Allows to define the channel's name.
     * @param channel The channel's name.
     */
    public final void setChannel (final String channel) {
        this.channel = channel;
    }

    /**
     * Allows to retrieve the channel's name.
     * @return The channel's name.
     */
    public final String getChannel () {
        return channel;
    }


    /**
     * Allows to define the content of the request as Map, with its values.
     * This method allows the creation of a Map in a lambda.
     *
     * @param consumer The consumer of the Map which will allow to modify it.
     */
    public final <K, V> void setMap (final Consumer<Map<K, V>> consumer) {
        final Map<K, V> map = new HashMap<>();
        consumer.accept(map);
        this.content = map;
    }

    /**
     * Allows to define the content of the request.
     *
     * @param map the object.
     */
    public final void setContent (final Object map) {
        this.content = map;
    }

    /**
     * Allows to get the content of the request.
     */
    public final Object getContent () {
        return this.content;
    }

    /**
     * Allows to retrieve the content of the request as Map.
     * If the content is not of this type, an empty Map will be returned.
     *
     * @return Content as map / or empty if the type is not a Map.
     */
    public final <K, V> Map<K, V> getMap () {
        if (this.content instanceof Map)
            return (Map<K, V>) this.content;
        return new HashMap<>();
    }

    /**
     * Allows you to retrieve the content of the as Map query in a lambda
     * If the content is not of this type, an empty Map will be returned.
     */
    public final <K, V> void getMap (final Consumer<Map<K, V>> consumer) {
        if (this.content instanceof Map)
            consumer.accept((Map<K, V>) this.content);
        consumer.accept(new HashMap<>());
    }

}
