package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

/**
 * Structure of an Action request.
 * Serializable.
 *
 * @version 1.0.0
 */
public class Action implements Serializable {
    private static final long serialVersionUID = 6244110737592708919L;

    public String name;
    public Object content;

}
