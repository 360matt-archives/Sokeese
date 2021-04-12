package fr.i360matt.sokeese.commons.modules;

import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.utils.Sha256;

public class LoginManager {

    private final String privateKey;

    public LoginManager (final String privateKey) {
        this.privateKey = privateKey;
    }

    public String getTokenRelated (final Session session) {
        return Sha256.hash(session.name + session.group + this.privateKey);
    }

    public boolean goodCredentials (final Session session) {
        return (session != null && getTokenRelated(session).equals(session.token));
    }

}
