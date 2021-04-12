package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

public class Reply implements Serializable {

    public long idRequest;

    public String recipient;
    public String sender;

    public String channel;
    public Object content;

}
