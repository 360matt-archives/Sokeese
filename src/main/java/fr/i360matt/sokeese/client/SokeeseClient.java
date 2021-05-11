package fr.i360matt.sokeese.client;

import fr.i360matt.sokeese.commons.requests.Session;
import fr.i360matt.sokeese.commons.events.ActionEvent;
import fr.i360matt.sokeese.commons.events.MessageEvent;
import fr.i360matt.sokeese.commons.modules.CatcherManager;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.AuthResponse;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Allows to connect to and communicate with a SokeeseClient server.
 * The server must be of the same type and version as the client.
 *
 * @author 360matt
 * @version 1.3.0
 */
public class SokeeseClient implements Closeable {

    private final CatcherManager.CLIENT catcherManager;
    private final ClientOptions options;

    private boolean isEnabled = true;
    private boolean isAvailable = false;

    private Socket socket;
    private ObjectOutputStream sender;
    private ObjectInputStream receiver;

    private final Session session;
    protected final String host;
    protected final int port;

    final String prefix;
    final Random random = new Random();


    /**
     * Allows to connect via the coordinates of the server as well as with an instance of Session.
     *
     * @param host Sokeese server host
     * @param port Sokeese server port
     * @param session Cryptographic session that the connection will use
     *
     * @see Session
     */
    public SokeeseClient (final String host, final int port, final Session session) {
        this(host, port, session, new ClientOptions());
    }

    /**
     * Allows to connect via the coordinates of the server as well as with an instance of Session.
     * You can set the options even before the connection starts.
     *
     * @param host Sokeese server host
     * @param port Sokeese server port
     * @param session Cryptographic session that the connection will use
     * @param options Connection options
     *
     * @see Session
     */
    public SokeeseClient (final String host, final int port, final Session session, final ClientOptions options) {
        this.host = host;
        this.port = port;
        this.session = session;
        this.options = options;
        this.catcherManager = new CatcherManager.CLIENT();

        this.prefix = "[SokeeseClient " + this.host + ":" + this.port + "]";

        final CompletableFuture<Void> future = new CompletableFuture<>();

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            int loop = 0;

            try {
                while (this.isEnabled) {
                    this.isAvailable = false;

                    try (
                            final Socket socket = new Socket(host, port);
                            final ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
                            final ObjectInputStream receiver = new ObjectInputStream(socket.getInputStream());
                    ) {

                        this.socket = socket;
                        this.sender = sender;
                        this.receiver = receiver;


                        if (this.waitLoginValidation()) { // if the login is successfull
                            this.isAvailable = true; // we can now send objects

                            future.complete(null); // and we can free the constructor

                            while (this.isEnabled) { // until close() is called or readObject() have throw an error
                                try {
                                    final Object obj = this.receiver.readObject(); // produces an error if class not found or if socket closed
                                    if (obj instanceof Message)
                                        this.catcherManager.handleMessage(this, (Message) obj);
                                    else if (obj instanceof Action)
                                        this.catcherManager.handleAction(this, (Action) obj);
                                    else if (obj instanceof Reply)
                                        this.catcherManager.handleReply((Reply) obj);
                                } catch (final ClassNotFoundException ignored) { }
                            }
                        }
                    } catch (final IOException ignored) { } // if close() is called, an error will be raised because of readObject()
                    finally {
                        if (this.isAvailable)
                            System.out.println(this.prefix + " Disconnected");
                        this.isAvailable = false;
                        TimeUnit.MILLISECONDS.sleep(this.options.retryDelay);
                    }

                    if (++loop == this.options.maxRetry) {
                        this.isEnabled = false;
                        break;
                    }
                }
            } catch (final Exception ignored) { }
            finally {
                this.catcherManager.close();
                future.complete(null); // and we can free the constructor
            }
        });
        service.shutdown();

        future.join();
    }


    /**
     * Allows to send the session and wait for the login to be validated
     * @return if the connection is accepted by the server
     */
    private boolean waitLoginValidation () throws IOException, ClassNotFoundException {
        this.sender.writeObject(session);
        this.sender.flush();

        final Object res = this.receiver.readObject();
        if (!(res instanceof AuthResponse)) {
            System.err.println(prefix + "Internal error in login phase");
            return false;
        }

        final AuthResponse authRes = (AuthResponse) res;
        switch (authRes.code) {
            case "OK":
                System.out.println(prefix + " Logged as '" + this.session.getName() + "'");
                return true;
            case "INVALID":
                System.err.println(prefix + " Invalid credential for '" + this.session.getName() + "'");
                return false;
            case "MAX_GLOBAL_CLIENT":
                System.err.println(prefix + " Server can't accept other connection just now (MAX_GLOBAL_CLIENT) for '" + this.session.getName() + "'");
                return false;
            case "MAX_SAME_CLIENT":
                System.err.println(prefix + " Max clients connected with same name '" + this.session.getName() + "'");
                return false;
            default:
                System.err.println(prefix + " Internal error in login phase for '" + this.session.getName() + "'");
                return false;
        }
    }


    /**
     * Sends a Message request to the server
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param consumer The Message request consumer.
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> consumer) {
        if (!this.isEnabled) return;

        final Message obj = new Message();
        consumer.accept(obj);

        try {
            this.sender.writeObject(obj);
            this.sender.flush();
        } catch (final IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Sends a Action request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param consumer The Action request consumer.
     *
     * @see Action
     */
    public final void sendAction (final Consumer<Action> consumer) {
        if (!this.isEnabled) return;

        final Action obj = new Action();
        consumer.accept(obj);

        try {
            this.sender.writeObject(obj);
            this.sender.flush();
        } catch (final IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Sends a Message request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param obj The request which must be of the Message or Action type.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void sendMessage (final Message obj) {
        if (!this.isEnabled) return;
        try {
            this.sender.writeObject(obj);
            this.sender.flush();
        } catch (final IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Sends a Action request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param obj The request which must be of the Message or Action type.
     *
     * @see Action
     */
    public final void sendAction (final Action obj) {
        if (!this.isEnabled) return;
        try {
            this.sender.writeObject(obj);
            this.sender.flush();
        } catch (final IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Sends a Reply request to the server
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param obj The Reply request.
     *
     * @see Reply
     */
    public void sendReply (final Reply obj) {
        if (!this.isEnabled) return;
        try {
            this.sender.writeObject(obj);
            this.sender.flush();
        } catch (final IOException e) {
            // e.printStackTrace();
        }
    }



    /**
     * Sends a Message request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * In case of no response before the chosen delay, the consumer will be executed with the second type set to FALSE.
     *
     * @param msgConsumer The message request consumer
     * @param delay The maximum amount of time to wait before the boolean is set to FALSE.
     * @param eventConsumer The callback defined by the developer.
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> msgConsumer, final int delay, final BiConsumer<Reply, Boolean> eventConsumer) {
        if (!this.isEnabled) return;
        try {
            final Message message = new Message();
            msgConsumer.accept(message);

            message.setIdRequest(random.nextLong());

            this.catcherManager.addReplyEvent(message.getIdRequest(), delay, eventConsumer);
            // we save the reply-event

            this.sender.writeObject(message);
            this.sender.flush();

        } catch (final IOException ignored) { }
    }

    /**
     * Sends a Message request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * In case of no response before the chosen delay, the consumer will be executed with the second type set to FALSE.
     *
     * @param message The message request.
     * @param delay The maximum amount of time to wait before the boolean is set to FALSE.
     * @param eventConsumer The callback defined by the developer.
     *
     * @see Message
     */
    public final void sendMessage (final Message message, final int delay, final BiConsumer<Reply, Boolean> eventConsumer) {
        if (!this.isEnabled) return;
        try {
            message.setIdRequest(random.nextLong());

            this.catcherManager.addReplyEvent(message.getIdRequest(), delay, eventConsumer);
            // we save the reply-event

            this.sender.writeObject(message);
            this.sender.flush();

        } catch (final IOException ignored) { }
    }

    /**
     * Sends a Message request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * The default time is 200ms.
     *
     * Use this.send(obj, int, consumer) to set a custom delay.
     *
     * @param msgConsumer The message request consumer.
     * @param eventConsumer The callback defined by the developer.
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> msgConsumer, final BiConsumer<Reply, Boolean> eventConsumer) {
        this.sendMessage(msgConsumer, 200, eventConsumer);
    }

    /**
     * Sends a Message request to the server.
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * The default time is 200ms.
     *
     * Use this.send(obj, int, consumer) to set a custom delay.
     *
     * @param message The message request.
     * @param eventConsumer The callback defined by the developer.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void sendMessage (final Message message, final BiConsumer<Reply, Boolean> eventConsumer) {
        this.sendMessage(message, 200, eventConsumer);
    }

    /**
     * Allows to register an event for the reception of a MESSAGE request on a certain channel
     *
     * @param channel The name of the channel that will be listened to.
     * @param consumer The consumer which will be executed for each reception.
     *
     * @see MessageEvent.CLIENT
     */
    public final void onMessage (final String channel, final Consumer<MessageEvent.CLIENT> consumer) {
        if (!this.isEnabled) return;
        this.catcherManager.addMessageEvent(channel, consumer);
    }

    /**
     * Allows to register an event for the reception of a ACTION request on a certain name
     *
     * @param name The name of the action that will be listened to.
     * @param consumer The consumer which will be executed for each reception.
     *
     * @see ActionEvent.CLIENT
     */
    public final void onAction (final String name, final Consumer<ActionEvent.CLIENT> consumer) {
        if (!this.isEnabled) return;
        this.catcherManager.addActionEvent(name, consumer);
    }


    /**
     * Used to retrieve the option of the current session.
     * @return The current session.
     */
    public final ClientOptions getOptions () {
        return this.options;
    }

    /**
     * Allows the client to be closed.
     *
     * The connection to the server is closed.
     * All services stop.
     * All local data related to the session is deleted (credentials, events)
     */
    @Override
    public void close () {
        this.isEnabled = false;
        try {
            this.socket.close();
        } catch (final IOException ignored) { }
    }

    /**
     * Allow to retrieve the connection state of the client.
     * @return the connection state of the client.
     */
    public final boolean isClosed () {
        return !isEnabled;
    }

    /**
     * Allow to retrieve the connection state of the client.
     * @return the connection state of the client.
     */
    public final boolean isOpen () {
        return isEnabled;
    }
}
