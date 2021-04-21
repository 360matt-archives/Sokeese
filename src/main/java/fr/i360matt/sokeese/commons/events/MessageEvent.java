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
 * @version 1.2.0
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
                this.client.sendReply(reply);
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
                this.client.sendReply(reply);
            }
        }

        /**
         * Serves as a shortcut to send a 'MESSAGE' request to the server faster.
         * @param message A 'MESSAGE' request.
         */
        public final void sendMessage (final Message message) {
            this.client.sendMessage(message);
        }

        /**
         * Serves as a shortcut to send a 'MESSAGE' request to the server faster.
         * @param consumer A 'MESSAGE' request consumer.
         */
        public final void sendMessage (final Consumer<Message> consumer) {
            this.client.sendMessage(consumer);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param action A 'ACTION' request.
         */
        public final void sendAction (final Action action) {
            this.client.sendAction(action);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param consumer A 'ACTION' request consumer.
         */
        public final void sendAction (final Consumer<Action> consumer) {
            this.client.sendAction(consumer);
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
                this.instance.sendReply(reply);
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
                this.instance.sendReply(reply);
            }
        }

        /**
         * Serves as a shortcut to send a 'Message' request to the server faster.
         * @param message A 'MESSAGE' request.
         */
        public final void sendMessage (final Message message) {
            this.instance.sendMessage(message);
        }

        /**
         * Serves as a shortcut to send a 'MESSAGE' request to the server faster.
         * @param consumer A 'MESSAGE' request consumer.
         */
        public final void sendMessage (final Consumer<Message> consumer) {
            this.instance.sendMessage(consumer);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param action A 'ACTION' request.
         */
        public final void sendAction (final Action action) {
            this.instance.sendAction(action);
        }

        /**
         * Serves as a shortcut to send a 'ACTION' request to the server faster.
         * @param consumer A 'ACTION' request consumer.
         */
        public final void sendAction (final Consumer<Action> consumer) {
            this.instance.sendAction(consumer);
        }
    }

}
