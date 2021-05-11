package fr.i360matt.sokeese.commons.requests;

import java.io.Serializable;

/**
 * Structure of an Authentification Response request.
 * Serializable.
 *
 * @version 1.1.0
 */
public final class AuthResponse implements Serializable {
    private static final long serialVersionUID = 3700388399416501217L;

    public String code;
}
