package fr.i360matt.sokeese.server;


import fr.i360matt.sokeese.commons.requests.Session;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.AuthResponse;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Each session will have its own instance of this class.
 *
 * @author 360matt
 * @version 1.3.0
 *
 * @see SokeeseServer
 */
public class ClientLogged implements Closeable {
    private boolean isClientEnabled = false;

    protected ObjectOutputStream sender;
    protected ObjectInputStream receiver;

    public final SokeeseServer server;
    private final Socket socket;
    private Session session;


    /**
     * Used to instantiate this class by filling in an instance of server and an instance of a Socket connection.
     * @param server A server instance.
     * @param client A Socket connection instance.
     *
     * @see SokeeseServer
     * @see Socket
     */
    public ClientLogged (final SokeeseServer server, final Socket client) {
        this.server = server;
        this.socket = client;

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            try (
                    final ObjectInputStream receiver = new ObjectInputStream(client.getInputStream());
                    final ObjectOutputStream sender = new ObjectOutputStream(client.getOutputStream());
            ) {

                this.sender = sender;
                this.receiver = receiver;


                if (this.waitLogin()) { // if the login is successfull
                    client.setSoTimeout(1000 * 3600 * 6); // can now be connected for 6 hours

                    this.isClientEnabled = true;

                    this.server.getUserManager().addUser(this);
                    // add this reference to users list


                    while (this.server.isOpen() && this.isOpen()) {
                        // can now listen every packets

                        try {
                            final Object obj = this.receiver.readObject(); // receive object over socket

                            if (obj instanceof Action)
                                this.server.getCatcherManager().handleAction((Action) obj, this);
                            else if (obj instanceof Message) {
                                final Message message = (Message) obj;
                                message.setSender(this.session.getName());
                                // set this session name as sender name

                                if (message.getRecipient().equalsIgnoreCase("server")) { // to the server
                                    this.server.getCatcherManager().handleMessage(message, this);
                                } else { // to be transmitted
                                    final ServerOptions.Level level = this.server.getOptions().getLevelMessages();

                                    if ((!message.getRecipient().equalsIgnoreCase("ALL") && level.getLevel() == 1) || level.getLevel() == 3) {
                                        this.server.sendMessage(message);
                                    }
                                }
                            } else if (obj instanceof Reply) {
                                final Reply reply = (Reply) obj;
                                reply.setSender(this.session.getName());
                                // set this session name as sender name

                                if (reply.getRecipient().equalsIgnoreCase("server")) { // to the server
                                    this.server.getCatcherManager().handleReply(reply);
                                } else { // to be transmitted
                                    final ServerOptions.Level level = this.server.getOptions().getLevelMessages();

                                    if ((!reply.getRecipient().equalsIgnoreCase("ALL") && level.getLevel() == 1) || level.getLevel() == 3) {
                                        this.server.sendReply(reply);
                                    }
                                }
                            }
                        } catch (final ClassNotFoundException ignored) { }
                    }
                }
            } catch (final IOException | ClassNotFoundException ignored) { }
            finally {
                if (this.isClientEnabled) {
                    this.isClientEnabled = false;
                    this.server.getUserManager().removeUser(this);
                    try {
                        client.close();
                    } catch (IOException ignored) { }
                }
            }
        });
    }




    /**
     * Allows to send the client a 'MESSAGE' request.
     * @param message The message request.
     *
     * @see Message
     */
    public final void sendMessage (final Message message) {
        if (!this.isClientEnabled) return;
        try {
            this.sender.writeObject(message);
            this.sender.flush();
        } catch (final IOException ignored) { }
    }

    /**
     * Allows to send the client a 'ACTION' request.
     * @param action The action request.
     *
     * @see Action
     */
    public final void sendAction (final Action action) {
        if (!this.isClientEnabled) return;
        try {
            this.sender.writeObject(action);
            this.sender.flush();
        } catch (final IOException ignored) { }
    }

    /**
     * Allows to send the client a 'MESSAGE' request.
     * @param consumer The message request consumer.
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> consumer) {
        if (!this.isClientEnabled) return;
        try {
            final Message message = new Message();
            consumer.accept(message);

            this.sender.writeObject(message);
            this.sender.flush();
        } catch (final IOException ignored) { }
    }

    /**
     * Allows to send the client a 'ACTION' request.
     * @param consumer The action request consumer.
     *
     * @see Action
     */
    public final void sendAction (final Consumer<Action> consumer) {
        if (!this.isClientEnabled) return;
        try {
            final Action action = new Action();
            consumer.accept(action);

            this.sender.writeObject(action);
            this.sender.flush();
        } catch (final IOException ignored) { }
    }

    /**
     * Allows to send the client a 'REPLY' request.
     * @param reply The reply request.
     *
     * @see Reply
     */
    public final void sendReply (final Reply reply) {
        if (!this.isClientEnabled) return;
        try {
            this.sender.writeObject(reply);
            this.sender.flush();
        } catch (final IOException ignored) { }
    }

    /**
     * Allows to send the client a 'MESSAGE' request.
     * And wait for a response within a chosen delay
     *
     * @param message A 'MESSAGE' request.
     * @param delay The maximum waiting time.
     * @param consumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     */
    public final void sendMessage (final Message message, final int delay, final BiConsumer<Reply, Boolean> consumer) {
        if (!this.isClientEnabled) return;

        message.setIdRequest(this.server.random.nextLong());
        this.server.getCatcherManager().addReplyEvent(message.getIdRequest(), delay, consumer);

        try {
            this.sender.writeObject(message);
            this.sender.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows to send the client a 'MESSAGE' request.
     * And wait for a response within a chosen delay
     *
     * @param messageConsumer A 'MESSAGE' request consumer.
     * @param delay The maximum waiting time.
     * @param eventConsumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> messageConsumer, final int delay, final BiConsumer<Reply, Boolean> eventConsumer) {
        if (!this.isClientEnabled) return;

        final Message message = new Message();
        messageConsumer.accept(message);

        message.setIdRequest(this.server.random.nextLong());
        this.server.getCatcherManager().addReplyEvent(message.getIdRequest(), delay, eventConsumer);

        try {
            this.sender.writeObject(message);
            this.sender.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows to send the client a 'MESSAGE' request.
     * And wait for a response within 200ms
     *
     * @param message A 'MESSAGE' request.
     * @param consumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void sendMessage (final Message message, final BiConsumer<Reply, Boolean> consumer) {
        this.sendMessage(message, 200, consumer);
    }

    /**
     * Allows to send the client a 'MESSAGE' request consumer.
     * And wait for a response within 200ms
     *
     * @param messageConsumer A 'MESSAGE' request consumer.
     * @param eventConsumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void sendMessage (final Consumer<Message> messageConsumer, final BiConsumer<Reply, Boolean> eventConsumer) {
        this.sendMessage(messageConsumer, 200, eventConsumer);
    }


    private final List<String> blacklisted = Arrays.asList("server", "all");
    /**
     * Allows to start the connection process.
     * @return If the connection was successful.
     */
    private boolean waitLogin () throws IOException, ClassNotFoundException {
        final AuthResponse response = new AuthResponse();

        boolean state;

        final Object packet = receiver.readObject();
        if (!(packet instanceof Session)) {
            // if isn't the good object type for login

            state = false;
            response.code = "MALFORMED_PACKET";
        } else if (this.server.getOptions().getMaxClients() <= this.server.getUserManager().getCount()) {
            // if the limit of simultaneous connected clients is reached

            state = false;
            response.code = "MAX_GLOBAL_CLIENT";
        } else {
            this.session = (Session) packet;
            if (!blacklisted.contains(this.session.getName().toLowerCase())) {
                state = this.server.getLoginManager().goodCredentials(this.session);
                response.code = (state) ? "OK" : "INVALID";
            } else if (this.server.getOptions().getMaxSameClient() < this.server.getUserManager().getUserCount(session.getName())) {
                state = false;
                response.code = "MAX_SAME_CLIENT";
            } else {
                state = false;
                response.code = "FORBIDDEN";
            }
        }

        sender.writeObject(response);
        sender.flush();
        return state;
    }

    /**
     * Retrieves the session data that were used to connect.
     * @return Session data.
     *
     * @see Session
     */
    public final Session getSession () {
        return this.session;
    }

    /**
     * Allows to disconnect the server connection
     */
    @Override public void close () {
        this.isClientEnabled = false;
        try {
            this.socket.close();
        } catch (final IOException ignored) { }
    }

    /**
     * Allow to retrieve the connection state of the client.
     * @return the connection state of the client.
     */
    public final boolean isClosed () {
        return !this.isClientEnabled;
    }

    /**
     * Allow to retrieve the connection state of the client.
     * @return the connection state of the client.
     */
    public final boolean isOpen () {
        return this.isClientEnabled;
    }

}
