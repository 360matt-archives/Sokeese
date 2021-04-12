package fr.i360matt.sokeese.commons;

import java.io.Serializable;

public class Session implements Serializable {

    public String name;
    public String group = "default";
    public String token;

}
