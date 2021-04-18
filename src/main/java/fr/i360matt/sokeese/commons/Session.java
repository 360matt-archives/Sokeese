package fr.i360matt.sokeese.commons;

import java.io.Serializable;

/**
 * Structure of an Session request.
 * Serializable.
 *
 * @version 1.0.0
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 6340669483585616737L;

    public String name;
    public String group = "default"; // default group
    public String token;

}
