package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Structure of an Action request.
 * Serializable.
 *
 * @version 1.1.0
 */
public final class Action implements Serializable {
    private static final long serialVersionUID = 6244110737592708919L;

    protected String name;
    protected Object content;


    /**
     * Allows to define the action name.
     *
     * @param name The name of the action.
     */
    public final void setName (final String name) {
        this.name = name;
    }

    /**
     * Allows to get the action name.
     * @return The name of the action.
     */
    public final String getName () {
        return this.name;
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
