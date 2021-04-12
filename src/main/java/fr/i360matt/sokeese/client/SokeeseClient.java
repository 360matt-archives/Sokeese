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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    protected String host;
    protected int port;

    final String prefix;
    final Random random = new Random();


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
                    this.mustBeSent = new HashSet<>();

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
                    finally {
                        service.shutdown();
                    }

                    if (this.isAvailable)
                        System.out.println(prefix + " Disconnected");

                    Thread.sleep(1000);
                }
            } catch (final Exception ignored) { }
        });
    }



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


    public final void sendObject (final Object obj) {
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            if (this.isAvailable) {
                try {
                    this.sender.writeObject(obj);
                    this.sender.flush();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            } else { // we are keep all objects, and send after, when the client will be logged
                final Queue queue = new Queue();
                queue.objToSend = obj;
                this.mustBeSent.add(queue);
            }
        }
    }


    public final void sendObject (Object obj, final int delay, final BiConsumer<Reply, Boolean> consumer) {
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            if (this.isAvailable) {

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
                    e.printStackTrace();
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

    public final void sendObject (Object obj, final BiConsumer<Reply, Boolean> consumer) {
        this.sendObject(obj, 200, consumer);
    }


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



    public final void onMessage (final String channel, final Consumer<MessageEvent.CLIENT> consumer) {
        this.catcherManager.addMessageEvent(channel, consumer);
    }
    public final void onAction (final String name, final Consumer<ActionEvent.CLIENT> consumer) {
        this.catcherManager.addActionEvent(name, consumer);
    }


    @Override
    public void close () {
        this.isEnabled = false;
    }
}
