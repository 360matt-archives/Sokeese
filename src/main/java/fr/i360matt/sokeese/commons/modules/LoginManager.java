package fr.i360matt.sokeese.commons.modules;


import fr.i360matt.sokeese.commons.requests.Session;
import fr.i360matt.sokeese.utils.Sha256;

/**
 * This class ensures the principle of API cryptography.
 * The token of each account is the result of the hash of the server's private key and the session name.
 *
 * @version 1.0.0
 * @author 360matt
 */
public class LoginManager {

    private final String privateKey;

    /**
     * Allow to instantiate the LoginManager from a private key.
     * @param privateKey The private key of the server which will be used to generate the tokens.
     */
    public LoginManager (final String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Allows you to retrieve the unique token linked to a session.
     * The Session#token field is not used, it can be null.
     *
     * @param session A session for which we want to obtain the linked token.
     * @return The corresponding token.
     */
    public final String getTokenRelated (final Session session) {
        return Sha256.hash(session.getName() + session.getGroup() + this.privateKey);
    }

    /**
     * Retrieves the session-bound token and compares it to the provided session token.
     * @param session The session with the 'token' field filled in.
     * @return If the token entered corresponds to the token linked to the account.
     */
    public final boolean goodCredentials (final Session session) {
        return (session != null && getTokenRelated(session).equals(session.getToken()));
    }

}
