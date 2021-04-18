package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

/**
 * Structure of an Reply request.
 * Serializable.
 *
 * @version 1.0.0
 */
public class Reply implements Serializable {
    private static final long serialVersionUID = 472144737983269550L;

    public long idRequest;

    public String recipient;
    public String sender;

    public String channel;
    public Object content;

}
