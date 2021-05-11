package fr.i360matt.sokeese.commons.events;


import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.ClientLogged;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Instances of this class represent an MESSAGE event
 *
 * @version 1.3.0
 * @see Message
 */
public final class MessageEvent {

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
            if (this.message.getIdRequest() != 0) {
                final Reply reply = new Reply();



                reply.setIdRequest(this.message.getIdRequest());
                reply.setRecipient(this.message.getSender());
                reply.setChannel(this.message.getChannel());
                reply.setContent(content);
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

            if (this.message.getIdRequest() != 0) {
                reply.setIdRequest(this.message.getIdRequest());
                reply.setRecipient(this.message.getSender());
                reply.setChannel(this.message.getChannel());
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

        /**
         * Shortcut to retrieve the name of the channel.
         * @return The channel name.
         */
        public final String getChannel () {
            return this.message.getChannel();
        }

        /**
         * Shortcut to retrieve the name of the sender.
         * @return The sender name.
         */
        public final String getSender () {
            return this.message.getSender();
        }

        /**
         * Shortcut to retrieve content under a requested type.
         * @param <T> Type that should be returned (not required).
         * @return the content of the query under type T!;
         */
        public final <T> T getContent () {
            try {
                return (T) this.message.getContent();
            } catch (final Exception e) {
                return null;
            }
        }


        /**
         * Allows to retrieve the content of the request as Map.
         * If the content is not of this type, an empty Map will be returned.
         *
         * @return Content as map / or empty if the type is not a Map.
         */
        public <K, V> Map<K, V> getMap () {
            return this.message.getMap();
        }

        /**
         * Allows you to retrieve the content of the as Map query in a lambda
         * If the content is not of this type, an empty Map will be returned.
         */
        public <K, V> void getMap (final Consumer<Map<K, V>> consumer) {
            this.message.getMap(consumer);
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
            if (this.message.getIdRequest() != 0) {
                final Reply reply = new Reply();
                reply.setIdRequest(this.message.getIdRequest());
                reply.setRecipient(this.message.getSender());
                reply.setChannel(this.message.getChannel());
                reply.setContent(content);
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

            if (this.message.getIdRequest() != 0) {
                reply.setIdRequest(this.message.getIdRequest());
                reply.setRecipient(this.message.getSender());
                reply.setChannel(this.message.getChannel());
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


        /**
         * Shortcut to retrieve the name of the channel.
         * @return The channel name.
         */
        public final String getChannel () {
            return this.message.getChannel();
        }

        /**
         * Shortcut to retrieve the name of the sender.
         * @return The sender name.
         */
        public final String getSender () {
            return this.message.getSender();
        }

        /**
         * Shortcut to retrieve content under a requested type.
         * @param <T> Type that should be returned (not required).
         * @return the content of the query under type T!;
         */
        public final <T> T getContent () {
            try {
                return (T) this.message.getContent();
            } catch (final Exception e) {
                return null;
            }
        }

        /**
         * Allows to retrieve the content of the request as Map.
         * If the content is not of this type, an empty Map will be returned.
         *
         * @return Content as map / or empty if the type is not a Map.
         */
        public <K, V> Map<K, V> getMap () {
            return this.message.getMap();
        }

        /**
         * Allows you to retrieve the content of the as Map query in a lambda
         * If the content is not of this type, an empty Map will be returned.
         */
        public <K, V> void getMap (final Consumer<Map<K, V>> consumer) {
            this.message.getMap(consumer);
        }
    }

}
