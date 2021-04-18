package fr.i360matt.sokeese.client;

import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.commons.events.ActionEvent;
import fr.i360matt.sokeese.commons.events.MessageEvent;
import fr.i360matt.sokeese.commons.modules.CatcherManager;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.AuthResponse;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
 * @version 1.0.0
 */
public class SokeeseClient implements Closeable {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final CatcherManager.CLIENT catcherManager;

    private Set<Queue> mustBeSent;
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
        this.host = host;
        this.port = port;
        this.session = session;
        this.catcherManager = new CatcherManager.CLIENT();

        this.prefix = "[SokeeseClient " + this.host + ":" + this.port + "]";


        this.service.execute(() -> {
            try {
                while (this.isEnabled) {
                    this.isAvailable = false;
                    if (this.mustBeSent == null)
                        this.mustBeSent = ConcurrentHashMap.newKeySet();

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
                            this.sendAllQueue();

                            while (this.isEnabled) {
                                final Object obj = this.receiver.readObject();
                                if (obj instanceof Message)
                                    this.catcherManager.handleMessage(this, (Message) obj);
                                else if (obj instanceof Action)
                                    this.catcherManager.handleAction(this, (Action) obj);
                                else if (obj instanceof Reply)
                                    this.catcherManager.handleReply((Reply) obj);
                            }
                        }
                    } catch (final IOException | ClassNotFoundException ignored) { }

                    if (this.isAvailable)
                        System.out.println(this.prefix + " Disconnected");

                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (final Exception ignored) { }
            finally {
                if (this.mustBeSent != null)
                    this.mustBeSent.clear();
                this.catcherManager.close();
            }
        });
        this.service.shutdown();
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
                System.out.println(prefix + " Logged as '" + this.session.name + "'");
                return true;
            case "INVALID":
                System.err.println(prefix + " Invalid credential for '" + this.session.name + "'");
                return false;
            default:
                System.err.println(prefix + " Internal error in login phase for '" + this.session.name + "'");
                return false;
        }
    }

    /**
     * Sends a Message or Action type request to the server
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * @param obj The request which must be of the Message or Action type.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (final Object obj) {
        if (!this.isEnabled) return;
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            if (this.isAvailable) {
                try {
                    this.sender.writeObject(obj);
                    this.sender.flush();
                } catch (final IOException e) {
                    // e.printStackTrace();
                }
            } else { // we are keep all objects, and send after, when the client will be logged
                final Queue queue = new Queue();
                queue.objToSend = obj;
                this.mustBeSent.add(queue);
            }
        }
    }

    /**
     * Sends a Message or Action type request to the server
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * In case of no response before the chosen delay, the consumer will be executed with the second type set to FALSE.
     *
     * @param obj The request which must be of the Message or Action type.
     * @param delay The maximum amount of time to wait before the boolean is set to FALSE.
     * @param consumer The callback defined by the developer.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (Object obj, final int delay, final BiConsumer<Reply, Boolean> consumer) {
        if (!this.isEnabled) return;
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            if (this.isAvailable || this.mustBeSent == null) {

                if (obj instanceof Message) {
                    final Message message = (Message) obj;
                    message.idRequest = random.nextLong();
                    obj = message;

                    this.catcherManager.addReplyEvent(message.idRequest, delay, consumer);
                }

                try {
                    this.sender.writeObject(obj);
                    this.sender.flush();
                } catch (final IOException e) {
                   // e.printStackTrace();
                }
            } else { // we are keep all objects, and send after, when the client will be logged
                final Queue queue = new Queue();
                queue.objToSend = obj;
                queue.delay = delay;
                queue.consumer = consumer;
                this.mustBeSent.add(queue);
            }
        }
    }

    /**
     * Sends a Message or Action type request to the server
     * If the client is not connected, the request will be placed on a waiting list to be sent later.
     *
     * This method also retrieves the response, which will be provided in a consumer.
     * The default time is 200ms.
     *
     * Use this.send(obj, int, consumer) to set a custom delay.
     *
     * @param obj The request which must be of the Message or Action type.
     * @param consumer The callback defined by the developer.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    public final void send (Object obj, final BiConsumer<Reply, Boolean> consumer) {
        this.send(obj, 200, consumer);
    }


    /**
     * Allows to send all the requests that had been put on hold.
     */
    private void sendAllQueue () {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            final Iterator<Queue> it = this.mustBeSent.iterator();

            try {
                while (it.hasNext()) {
                    if (this.isEnabled && !this.socket.isClosed()) {

                        final Queue queue = it.next();
                        Object obj = queue.objToSend;

                        if (obj instanceof Message && queue.consumer != null) {
                            final Message message = (Message) obj;
                            message.idRequest = random.nextLong();
                            obj = message;

                            this.catcherManager.addReplyEvent(message.idRequest, queue.delay, queue.consumer);
                        }

                        this.sender.writeObject(obj);
                        this.sender.flush();
                        it.remove(); // remove only if no errors
                    }
                }
            } catch (final IOException ignored) { }
            this.mustBeSent = null;
        });
        executor.shutdown();
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
