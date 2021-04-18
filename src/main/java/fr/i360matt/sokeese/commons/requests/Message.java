package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

/**
 * Structure of an Message request.
 * Serializable.
 *
 * @version 1.0.0
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -3107782551846954635L;

    public long idRequest;

    public String recipient;
    public String sender;

    public String channel;
    public Object content;

}
