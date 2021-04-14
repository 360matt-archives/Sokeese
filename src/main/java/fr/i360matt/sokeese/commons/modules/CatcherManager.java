package fr.i360matt.sokeese.commons.modules;

import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.events.ActionEvent;
import fr.i360matt.sokeese.commons.events.MessageEvent;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.ClientLogged;
import fr.i360matt.sokeese.utils.ExpirableCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CatcherManager {

    public static class CLIENT {
        private final Map<String, Set<Consumer<MessageEvent.CLIENT>>> messageEvents = new HashMap<>();
        private final Map<String, Set<Consumer<ActionEvent.CLIENT>>> actionEvents = new HashMap<>();
        private final ExpirableCallback<Long, BiConsumer<Reply, Boolean>> replyEvents = new ExpirableCallback<>();

        public final void addMessageEvent (final String name, final Consumer<MessageEvent.CLIENT> event) {
            Set<Consumer<MessageEvent.CLIENT>> candidate = messageEvents.get(name);
            if (candidate != null) {
                candidate.add(event);
            } else {
                candidate = new HashSet<>();
                candidate.add(event);
                messageEvents.put(name, candidate);
            }
        }

        public final void addActionEvent (final String name, final Consumer<ActionEvent.CLIENT> event) {
            Set<Consumer<ActionEvent.CLIENT>> candidate = actionEvents.get(name);
            if (candidate != null) {
                candidate.add(event);
            } else {
                candidate = new HashSet<>();
                candidate.add(event);
                actionEvents.put(name, candidate);
            }
        }

        public final void addReplyEvent (final long id, final int delay, final BiConsumer<Reply, Boolean> event) {
            replyEvents.put(id, event, delay);
        }


        public void handleMessage (final SokeeseClient client, final Message message) {
            final Set<Consumer<MessageEvent.CLIENT>> candidates = messageEvents.get(message.channel);
            if (candidates != null) {
                for (final Consumer<MessageEvent.CLIENT> consumer : candidates)
                    consumer.accept(new MessageEvent.CLIENT(client, message));
            }
        }


        public void handleAction (final SokeeseClient client, final Action action) {
            final Set<Consumer<ActionEvent.CLIENT>> candidates = actionEvents.get(action.name);
            if (candidates != null) {
                for (final Consumer<ActionEvent.CLIENT> consumer : candidates)
                    consumer.accept(new ActionEvent.CLIENT(client, action));
            }
        }


        public void handleReply (final Reply reply) {
            final BiConsumer<Reply, Boolean> candidate = replyEvents.get(reply.idRequest);
            if (candidate != null) {
                candidate.accept(reply, true);
                replyEvents.remove(reply.idRequest);
            }
        }
    }




    public static class SERVER {
        private final Map<String, Set<BiConsumer<MessageEvent.SERVER, ClientLogged>>> messageEvents = new HashMap<>();
        private final Map<String, Set<BiConsumer<ActionEvent.SERVER, ClientLogged>>> actionEvents = new HashMap<>();
        private final ExpirableCallback<Long, BiConsumer<Reply, Boolean>> replyEvents = new ExpirableCallback<>();

        public final void addMessageEvent (final String name, final BiConsumer<MessageEvent.SERVER, ClientLogged> event) {
            Set<BiConsumer<MessageEvent.SERVER, ClientLogged>> candidate = messageEvents.get(name);
            if (candidate != null) {
                candidate.add(event);
            } else {
                candidate = new HashSet<>();
                candidate.add(event);
                messageEvents.put(name, candidate);
            }
        }

        public final void addActionEvent (final String name, final BiConsumer<ActionEvent.SERVER, ClientLogged> event) {
            Set<BiConsumer<ActionEvent.SERVER, ClientLogged>> candidate = actionEvents.get(name);
            if (candidate != null) {
                candidate.add(event);
            } else {
                candidate = new HashSet<>();
                candidate.add(event);
                actionEvents.put(name, candidate);
            }
        }

        public final void addReplyEvent (final long id, final int delay, final BiConsumer<Reply, Boolean> event) {
            replyEvents.put(id, event, delay);
        }


        public void handleMessage (final Message message, final ClientLogged client) {
            final Set<BiConsumer<MessageEvent.SERVER, ClientLogged>> candidates = messageEvents.get(message.channel);
            if (candidates != null) {
                for (final BiConsumer<MessageEvent.SERVER, ClientLogged> consumer : candidates)
                    consumer.accept(new MessageEvent.SERVER(client, message), client);
            }
        }


        public void handleAction (final Action action, final ClientLogged client) {
            final Set<BiConsumer<ActionEvent.SERVER, ClientLogged>> candidates = actionEvents.get(action.name);
            if (candidates != null) {
                for (final BiConsumer<ActionEvent.SERVER, ClientLogged> consumer : candidates)
                    consumer.accept(new ActionEvent.SERVER(client, action), client);
            }
        }


        public void handleReply (final Reply reply) {
            final BiConsumer<Reply, Boolean> candidate = replyEvents.get(reply.idRequest);
            if (candidate != null) {
                candidate.accept(reply, true);
                replyEvents.remove(reply.idRequest);
            }
        }
    }


}
