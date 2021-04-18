package fr.i360matt.sokeese.server;

import fr.i360matt.sokeese.commons.Session;
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

/**
 * Each session will have its own instance of this class.
 *
 * @author 360matt
 * @version 1.0.0
 *
 * @see SokeeseServer
 */
public class ClientLogged implements Closeable {
    protected final ExecutorService service = Executors.newSingleThreadExecutor();
    private boolean isClientEnabled = true;

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

        this.service.execute(() -> {
            try (
                    final ObjectInputStream receiver = new ObjectInputStream(client.getInputStream());
                    final ObjectOutputStream sender = new ObjectOutputStream(client.getOutputStream());
            ) {

                this.sender = sender;
                this.receiver = receiver;


                if (this.waitLogin()) { // if the login is successfull
                    client.setSoTimeout(1000 * 3600 * 6); // can now be connected for 6 hours

                    this.server.getUserManager().addUser(this);
                    // add this reference to users list


                    while (this.server.isOpen() && this.isOpen()) {
                        // can now listen every packets

                        try {
                            final Object obj = this.receiver.readObject(); // receive object over socket

                            if (obj instanceof Action)
                                this.server.catcherManager.handleAction((Action) obj, this);
                            else if (obj instanceof Message) {
                                final Message message = (Message) obj;
                                message.sender = this.session.name;
                                // set this session name as sender name

                                if (message.recipient.equalsIgnoreCase("server")) { // to the server
                                    this.server.catcherManager.handleMessage(message, this);
                                } else { // to be transmitted
                                    this.server.send(message.recipient, message);
                                }
                            } else if (obj instanceof Reply) {
                                final Reply reply = (Reply) obj;
                                reply.sender = this.session.name;
                                // set this session name as sender name

                                if (reply.recipient.equalsIgnoreCase("server")) { // to the server
                                    this.server.catcherManager.handleReply(reply);
                                } else { // to be transmitted
                                    this.server.send(reply.recipient, reply);
                                }
                            }
                        } catch (final ClassNotFoundException ignored) { }
                    }
                }
            } catch (final IOException | ClassNotFoundException ignored) { }
            finally {
                this.isClientEnabled = false;
                this.server.getUserManager().removeUser(this);
                try {
                    client.close();
                } catch (IOException ignored) { }
            }
        });
    }


    /**
     * Allows to send the client a 'MESSAGE', 'ACTION' or 'REPLY' request.
     * @param obj A 'MESSAGE', 'ACTION' or 'REPLY' request.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (final Object obj) {
        if (!this.isClientEnabled) return;
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            try {
                this.sender.writeObject(obj);
                this.sender.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows to send the client a 'MESSAGE', 'ACTION' or 'REPLY' request.
     * And wait for a response within a chosen delay
     *
     * @param obj A 'MESSAGE', 'ACTION' or 'REPLY' request.
     * @param delay The maximum waiting time.
     * @param consumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (Object obj, final int delay, final BiConsumer<Reply, Boolean> consumer) {
        if (!this.isClientEnabled) return;
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            if (obj instanceof Message) {
                final Message message = (Message) obj;
                message.idRequest = this.server.random.nextLong();
                obj = message;

                this.server.catcherManager.addReplyEvent(message.idRequest, delay, consumer);
            }

            try {
                this.sender.writeObject(obj);
                this.sender.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows to send the client a 'MESSAGE', 'ACTION' or 'REPLY' request.
     * And wait for a response within 200ms
     *
     * @param obj A 'MESSAGE', 'ACTION' or 'REPLY' request.
     * @param consumer The event callback that will contain the response
     *                 (which can be null if the delay has expired).
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (final Object obj, final BiConsumer<Reply, Boolean> consumer) {
        this.send(obj, 200, consumer);
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
        } else {
            this.session = (Session) packet;
            if (!blacklisted.contains(this.session.name.toLowerCase())) {
                state = this.server.getLoginManager().goodCredentials(this.session);
                response.code = (state) ? "OK" : "INVALID";
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
