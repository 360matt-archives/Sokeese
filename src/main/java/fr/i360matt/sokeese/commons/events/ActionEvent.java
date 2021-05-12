package fr.i360matt.sokeese.commons.events;


import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.server.ClientLogged;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Instances of this class represent an ACTION event
 *
 * @version 1.2.0
 * @see Action
 */
public final class ActionEvent {
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
         * Shortcut used to retrieve the name of the action
         * @return The name of action.
         */
        public final String getName () {
            return action.getName();
        }

        /**
         * Shortcut used to retrieve the content of the action
         * @return The content of action.
         */
        public final Object getContent () {
            return action.getContent();
        }

        /**
         * Allows to retrieve the content of the request as Map.
         * If the content is not of this type, an empty Map will be returned.
         *
         * @return Content as map / or empty if the type is not a Map.
         */
        public <K, V> Map<K, V> getMap () {
            return this.action.getMap();
        }

        /**
         * Allows you to retrieve the content of the as Map query in a lambda
         * If the content is not of this type, an empty Map will be returned.
         */
        public <K, V> void getMap (final Consumer<Map<K, V>> consumer) {
            this.action.getMap(consumer);
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
         * Shortcut used to retrieve the name of the action
         * @return The name of action.
         */
        public final String getName () {
            return action.getName();
        }

        /**
         * Shortcut used to retrieve the content of the action
         * @return The content of action.
         */
        public final Object getContent () {
            return action.getContent();
        }

        /**
         * Allows to retrieve the content of the request as Map.
         * If the content is not of this type, an empty Map will be returned.
         *
         * @return Content as map / or empty if the type is not a Map.
         */
        public <K, V> Map<K, V> getMap () {
            return this.action.getMap();
        }

        /**
         * Allows you to retrieve the content of the as Map query in a lambda
         * If the content is not of this type, an empty Map will be returned.
         */
        public <K, V> void getMap (final Consumer<Map<K, V>> consumer) {
            this.action.getMap(consumer);
        }
    }
}
