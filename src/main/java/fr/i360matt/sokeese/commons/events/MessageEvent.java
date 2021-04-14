package fr.i360matt.sokeese.commons.events;

import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.ClientLogged;

public class MessageEvent {

    public static final class CLIENT {
        private final Message message;
        private final SokeeseClient client;
        public CLIENT (final SokeeseClient client, final Message message) {
            this.message = message;
            this.client = client;
        }

        public final Message getMessage () {
            return this.message;
        }

        public final void reply (final Reply reply) {
            if (this.message.idRequest != 0) {
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                this.client.send(reply);
            }
        }

        public final void send (final Message message) {
            this.client.send(message);
        }

        public final void send (final Action action) {
            this.client.send(action);
        }
    }


    public static final class SERVER {
        private final Message message;
        private final ClientLogged instance;
        public SERVER (final ClientLogged instance, final Message message) {
            this.message = message;
            this.instance = instance;
        }

        public final Message getMessage () {
            return this.message;
        }

        public final void reply (final Reply reply) {
            if (this.message.idRequest != 0) {
                reply.idRequest = this.message.idRequest;
                reply.channel = this.message.channel;
                this.instance.sendObject(reply);
            }
        }

        public final void send (final Message message) {
            this.instance.sendObject(message);
        }

        public final void send (final Action action) {
            this.instance.sendObject(action);
        }
    }

}
