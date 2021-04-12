package fr.i360matt.sokeese.commons.events;

import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.server.ClientLogged;

public class ActionEvent {
    public static class CLIENT {
        private final Action action;
        private final SokeeseClient client;
        public CLIENT (final SokeeseClient client, final Action action) {
            this.action = action;
            this.client = client;
        }

        public final Action getAction () {
            return this.action;
        }


        public final void send (final Message message) {
            this.client.sendObject(message);
        }

        public final void send (final Action action) {
            this.client.sendObject(action);
        }
    }


    public static class SERVER {
        private final Action action;
        private final ClientLogged instance;
        public SERVER (final ClientLogged instance, final Action action) {
            this.action = action;
            this.instance = instance;
        }

        public final Action getAction () {
            return this.action;
        }

        public final void send (final Message message) {
            this.instance.sendObject(message);
        }

        public final void send (final Action action) {
            this.instance.sendObject(action);
        }
    }
}
