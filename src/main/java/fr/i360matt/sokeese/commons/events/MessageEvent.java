package fr.i360matt.sokeese.commons.events;

import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.ClientLogged;

import java.util.function.Consumer;

/**
 * Instances of this class represent an MESSAGE event
 *
 * @version 1.1.0
 * @see Message
 */
public class MessageEvent {

    public static final class CLIENT {
        private final Message message;
        private final SokeeseClient client;

        /**
         * Allow to instantiate a new event from a client instance and a 'MESSAGE' request instance.
         * @param client A client instance.
         * @param message A request instance.
         */
        public CLIENT (final SokeeseClient client, final Message message) {
            this.message = message;
            this.client = client;
        }

        /**
         * Retrieve the received 'MESSAGE' request
         * @return The received request
         */
        public final Message getRequest () {
            return this.message;
        }

        /**
         * Allows to answer this query
         * @param content The response.
         */
        public final void reply (final Object content) {
            if (this.message.idRequest != 0) {
                final Reply reply = new Reply();
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                reply.content = content;
                this.client.send(reply);
            }
        }

        /**
         * Allows to answer this query
         * @param function The request response function.
         */
        public final void reply (final Consumer<Reply> function) {
            final Reply reply = new Reply();
            function.accept(reply);

            if (this.message.idRequest != 0) {
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                this.client.send(reply);
            }
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
        private final Message message;
        private final ClientLogged instance;

        /**
         * Allow to instantiate a new event from a server instance and a 'MESSAGE' request instance.
         * @param instance A client-session instance.
         * @param message A request instance.
         */
        public SERVER (final ClientLogged instance, final Message message) {
            this.message = message;
            this.instance = instance;
        }

        /**
         * Retrieve the received 'MESSAGE' request
         * @return The received request
         */
        public final Message getRequest () {
            return this.message;
        }

        /**
         * Allows to answer this query
         * @param content The response.
         */
        public final void reply (final Object content) {
            if (this.message.idRequest != 0) {
                final Reply reply = new Reply();
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                reply.content = content;
                this.instance.send(reply);
            }
        }

        /**
         * Allows to answer this query
         * @param function The request response function.
         */
        public final void reply (final Consumer<Reply> function) {
            final Reply reply = new Reply();
            function.accept(reply);

            if (this.message.idRequest != 0) {
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                this.instance.send(reply);
            }
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
