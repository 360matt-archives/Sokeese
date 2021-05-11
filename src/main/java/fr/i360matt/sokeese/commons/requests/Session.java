package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

/**
 * Structure of an Session request.
 * Serializable.
 *
 * @version 1.0.0
 */
public final class Session implements Serializable {
    private static final long serialVersionUID = 6340669483585616737L;

    protected String name;
    protected String group = "default"; // default group
    protected String token;

    /**
     * Allows to define the username of the session
     * @param name The Username.
     * @return The current instance.
     */
    public Session setName (final String name) {
        this.name = name;
        return this;
    }

    /**
     * Allows to retrieve the name of the session user
     * @return The username.
     */
    public String getName () {
        return name;
    }

    /**
     * Allows to define the group of the session user
     * @param group The group name.
     * @return The current instance.
     */
    public Session setGroup (final String group) {
        this.group = group;
        return this;
    }

    /**
     * Allows to retrieve the group of the session user
     * @return The group name.
     */
    public String getGroup () {
        return group;
    }

    /**
     * Allows to define the token of the session
     * @param token The token.
     * @return The current instance.
     */
    public Session setToken (final String token) {
        this.token = token;
        return this;
    }

    /**
     * Allows to retrieve the token of the session user
     * @return The token.
     */
    public String getToken () {
        return token;
    }
}
