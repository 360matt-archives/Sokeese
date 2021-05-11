package fr.i360matt.sokeese.server;

import fr.i360matt.sokeese.commons.events.ActionEvent;
import fr.i360matt.sokeese.commons.events.MessageEvent;
import fr.i360matt.sokeese.commons.modules.CatcherManager;
import fr.i360matt.sokeese.commons.modules.LoginManager;
import fr.i360matt.sokeese.commons.modules.UserManager;
import fr.i360matt.sokeese.commons.requests.Action;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Allows to receive connections and communicate with SokeeseClient clients.
 * The client must be of the same type and version as the server.
 *
 * @author 360matt
 * @version 1.3.0
 */
public class SokeeseServer implements Closeable {

    private boolean isEnabled = true;

    private final LoginManager loginManager;
    private final CatcherManager.SERVER catcherManager;
    private final UserManager userManager = new UserManager();

    private ServerSocket server;
    private final ServerOptions options;

    protected final Random random = new Random();

    /**
     * Allows you to start the server with a port number and its secret key.
     *
     * @param port The listening port of the server.
     * @param privateKey The secret key from which the cryptography is based.
     */
    public SokeeseServer (final int port, final String privateKey) {
        this(port, privateKey, new ServerOptions());
    }

    /**
     * Allows you to start the server with a port number and its secret key.
     *
     * @param port The listening port of the server.
     * @param privateKey The secret key from which the cryptography is based.
     * @param options Server options
     */
    public SokeeseServer (final int port, final String privateKey, final ServerOptions options) {
        this.loginManager = new LoginManager(privateKey);
        this.catcherManager = new CatcherManager.SERVER();
        this.options = options;

        final CompletableFuture<Void> future = new CompletableFuture<>();

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            try (final ServerSocket server = new ServerSocket(port)) {
                this.server = server;

                future.complete(null);

                while (this.isEnabled && !server.isClosed()) {
                    final Socket socket = server.accept();
                    socket.setSoTimeout(1000); // timeout 1 seconds while login
                    new ClientLogged(this, socket);
                }
            } catch (final Exception e) {
                if (this.getOptions().getDebug())
                    e.printStackTrace();
            }
            finally {
                future.complete(null);
                this.catcherManager.close();
                this.userManager.close();
            }
        });
        service.shutdown();

        future.join();
    }

    /**
     * Sends a Message or Action type request to one or multiple clients
     *
     * @param recipient The name of the client who should receive the request.
     *                  'SERVER' -> send the intended request to the server
     *                  'ALL'    -> send the intended request to all clients except the server
     * @param obj The request which must be of the Message or Action type.
     *
     * @see Message
     * @see Action
     * @see Reply
     */
    private void send (final String recipient, final Object obj) throws IOException {
        if (!isEnabled) return;

        final ServerOptions.Level level = this.getOptions().getLevelMessages();
        if (recipient.equalsIgnoreCase("all") && level.getLevel() == 3) {
            // send to every clients
            for (final ClientLogged user : this.getUserManager().getAllUsers()) {
                synchronized (user.syncOut) {
                    user.sender.writeObject(obj);
                    user.sender.flush();
                }
            }
        } else if (level.getLevel() == 1 && !recipient.equalsIgnoreCase("server")) {
            // send to unique client (or multiple terminals with the same name)
            for (final ClientLogged user : this.getUserManager().getUser(recipient)) {
                synchronized (user.syncOut) {
                    user.sender.writeObject(obj);
                    user.sender.flush();
                }
            }
        }
    }

    /**
     * Sends a Message request to one or multiple clients
     *
     * The name of the client who should receive the request:
     *                  'SERVER' -> send the intended request to the server.
     *                  'ALL'    -> send the intended request to all clients except the server.
     *                    *      -> send the intended request to one client.
     * @param message The message request.
     *
     * @see Message
     */
    public final void sendMessage (final Message message) throws IOException {
        this.send(message.getRecipient(), message);
    }

    /**
     * Sends a Action request to one or multiple clients
     *
     * @param recipient The name of the client who should receive the request:
     *                  'SERVER' -> send the intended request to the server.
     *                  'ALL'    -> send the intended request to all clients except the server.
     *                    *      -> send the intended request to one client.
     * @param action The action request.
     *
     * @see Action
     */
    public final void sendAction (final String recipient, final Action action) throws IOException {
        this.send(recipient, action);
    }

    /**
     * Sends a Message request to one or multiple clients
     *
     * The name of the client who should receive the request:
     *                  'SERVER' -> send the intended request to the server.
     *                  'ALL'    -> send the intended request to all clients except the server.
     *                    *      -> send the intended request to one client.
     * @param reply The reply request.
     *
     * @see Message
     */
    protected final void sendReply (final Reply reply) throws IOException {
        this.send(reply.getRecipient(), reply);
    }

    /**
     * Sends a Message request to one or multiple clients
     *
     * The name of the client who should receive the request:
     *                  'SERVER' -> send the intended request to the server.
     *                  'ALL'    -> send the intended request to all clients except the server.
     *                    *      -> send the intended request to one client.
     * @param consumer The message request consumer.
     *
     * @see Message
     */
    public final void sendMessage (final Consumer<Message> consumer) throws IOException {
        final Message message = new Message();
        consumer.accept(message);
        this.send(message.getRecipient(), message);
    }

    /**
     * Sends a Action request to one or multiple clients
     *
     * @param recipient The name of the client who should receive the request:
     *                  'SERVER' -> send the intended request to the server.
     *                  'ALL'    -> send the intended request to all clients except the server.
     *                    *      -> send the intended request to one client.
     * @param consumer The action request consumer.
     *
     * @see Action
     */
    public final void sendAction (final String recipient, final Consumer<Action> consumer) throws IOException {
        final Action action = new Action();
        consumer.accept(action);
        this.send(recipient, action);
    }

    /**
     * Allows to register an event for the reception of a MESSAGE request on a certain channel
     *
     * @param channel The name of the channel that will be listened to.
     * @param consumer The consumer which will be executed for each reception.
     *
     * @see MessageEvent.SERVER
     */
    public final void onMessage (final String channel, final BiConsumer<MessageEvent.SERVER, ClientLogged> consumer) {
        if (!isEnabled) return;
        this.catcherManager.addMessageEvent(channel, consumer);
    }

    /**
     * Allows to register an event for the reception of a ACTION request on a certain name
     *
     * @param name The name of the action that will be listened to.
     * @param consumer The consumer which will be executed for each reception.
     *
     * @see ActionEvent.SERVER
     */
    public final void onAction (final String name, final BiConsumer<ActionEvent.SERVER, ClientLogged> consumer) {
        if (!isEnabled) return;
        this.catcherManager.addActionEvent(name, consumer);
    }


    /**
     * Allows to retrieve the login manager.
     * @return The login manager of the instantiated server.
     */
    public final LoginManager getLoginManager () {
        return this.loginManager;
    }

    /**
     * Allows to retrieve the user manager.
     * @return The user manager of the instantiated server.
     */
    public final UserManager getUserManager () {
        return this.userManager;
    }

    /**
     * Allows to retrieve the event-catcher manager.
     * @return The event-catcher manager of the instantiated server.
     */
    protected final CatcherManager.SERVER getCatcherManager () {
        return this.catcherManager;
    }

    /**
     * Used to retrieve the option of the current session.
     * @return The current session.
     */
    public final ServerOptions getOptions () {
        return this.options;
    }

    /**
     * Allows the server to be closed.
     *
     * The connection to all clients is closed.
     * All services stop.
     * All temporary data is deleted (users references, events)
     */
    @Override public void close () {
        this.isEnabled = false;
        try {
            this.server.close();
        } catch (final Exception e) {
            if (this.getOptions().getDebug())
                e.printStackTrace();
        }

    }

    /**
     * Allow to retrieve the connection state of the server.
     * @return the connection state of the client.
     */
    public final boolean isClosed () {
        return !isEnabled;
    }

    /**
     * Allow to retrieve the connection state of the server.
     * @return the connection state of the client.
     */
    public final boolean isOpen () {
        return isEnabled;
    }
}
