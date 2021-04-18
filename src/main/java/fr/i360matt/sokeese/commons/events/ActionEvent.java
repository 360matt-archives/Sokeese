package fr.i360matt.sokeese.commons.events;

import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.server.ClientLogged;

/**
 * Instances of this class represent an ACTION event
 *
 * @version 1.0.0
 * @see Action
 */
public class ActionEvent {
    public static final class CLIENT {
        private final Action action;
        private final SokeeseClient client;

        /**
         * Allow to instantiate a new event from a client instance and a 'ACTION' request instance.
         * @param client A client instance.
         * @param action A request instance.
         */
        public CLIENT (final SokeeseClient client, final Action action) {
            this.action = action;
            this.client = client;
        }

        /**
         * Retrieve the received 'ACTION' request
         * @return The received request
         */
        public final Action getRequest () {
            return this.action;
        }

        /**
         * Serves as a shortcut to send a 'MESSAGE' request to the server faster.
         * @param message A 'MESSAGE' request.
         */
        public final void send (final Message message) {
            this.client.send(message);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param action A 'ACTION' request.
         */
        public final void send (final Action action) {
            this.client.send(action);
        }
    }


    public static final class SERVER {
        private final Action action;
        private final ClientLogged instance;

        /**
         * Allow to instantiate a new event from a server instance and a 'ACTION' request instance.
         * @param instance A client-session instance.
         * @param action A request instance.
         */
        public SERVER (final ClientLogged instance, final Action action) {
            this.action = action;
            this.instance = instance;
        }

        /**
         * Retrieve the received 'ACTION' request
         * @return The received request
         */
        public final Action getRequest () {
            return this.action;
        }

        /**
         * Serves as a shortcut to send a 'Message' request to the server faster.
         * @param message A 'MESSAGE' request.
         */
        public final void send (final Message message) {
            this.instance.send(message);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param action A 'ACTION' request.
         */
        public final void send (final Action action) {
            this.instance.send(action);
        }
    }
}
