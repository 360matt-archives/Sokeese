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

public class ClientLogged implements Closeable {



    public final SokeeseServer server;
    protected final ExecutorService service = Executors.newSingleThreadExecutor();
    private boolean isClientEnabled = true;



    protected ObjectOutputStream sender;
    protected ObjectInputStream receiver;

    private Session session;

    public ClientLogged (final SokeeseServer server, final Socket client) {
        this.server = server;

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


                    while (this.server.isOpen() && this.isOpen()) {
                        // can now listen every packets

                        try {
                            final Object obj = this.receiver.readObject();

                            if (obj instanceof Action)
                                this.server.catcherManager.handleAction((Action) obj, this);
                            else if (obj instanceof Message) {
                                final Message message = (Message) obj;
                                message.sender = this.session.name;

                                if (message.recipient.equalsIgnoreCase("server")) { // to the server
                                    this.server.catcherManager.handleMessage(message, this);
                                } else { // to be transmitted
                                    this.server.sendTo(message.recipient, message);
                                }
                            } else if (obj instanceof Reply) {
                                final Reply reply = (Reply) obj;
                                reply.sender = this.session.name;

                                if (reply.recipient.equalsIgnoreCase("server")) { // to the server
                                    this.server.catcherManager.handleReply(reply);
                                } else { // to be transmitted
                                    this.server.sendTo(reply.recipient, reply);
                                }
                            }
                        } catch (final ClassNotFoundException ignored) { }

                    }
                }
            } catch (final IOException | ClassNotFoundException ignored) { }
            finally {
                this.server.getUserManager().removeUser(this);
                try {
                    client.close();
                } catch (IOException ignored) { }
            }
        });
    }



    public final void sendObject (final Object obj) {
        if (this.isClientEnabled) {
            if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
                try {
                    this.sender.writeObject(obj);
                    this.sender.flush();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public final void sendObject (Object obj, final int delay, final BiConsumer<Reply, Boolean> consumer) {
        if (this.isClientEnabled) {
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
    }

    public final void sendObject (Object obj, final BiConsumer<Reply, Boolean> consumer) {
        this.sendObject(obj, 200, consumer);
    }


    private final List<String> blacklisted = Arrays.asList("server", "all");
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


    public final Session getSession () {
        return this.session;
    }

    @Override public void close () { this.isClientEnabled = false; }
    public final boolean isClosed () {
        return !this.isClientEnabled;
    }
    public final boolean isOpen () {
        return this.isClientEnabled;
    }

}
