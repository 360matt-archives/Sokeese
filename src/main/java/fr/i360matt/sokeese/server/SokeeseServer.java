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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class SokeeseServer implements Closeable {

    private final LoginManager loginManager;
    protected final CatcherManager.SERVER catcherManager;

    private boolean isEnabled = true;

    protected final ExecutorService service = Executors.newSingleThreadExecutor();
    private final UserManager userManager = new UserManager();

    private ServerSocket server;

    final Random random = new Random();


    public SokeeseServer (final int port, final String privateKey) {
        this.loginManager = new LoginManager(privateKey);
        this.catcherManager = new CatcherManager.SERVER();


        service.execute(() -> {
            try (final ServerSocket server = new ServerSocket(port)) {
                this.server = server;

                while (this.isEnabled && !server.isClosed()) {
                    final Socket socket = server.accept();
                    socket.setSoTimeout(1000); // timeout 1 seconds while login

                    new ClientLogged(this, socket);
                }
            } catch (final Exception e) { }
            finally {
                service.shutdown();
            }
        });
    }

    public final void sendTo (final String recipient, final Object obj) throws IOException {
        if (obj instanceof Message || obj instanceof Action || obj instanceof Reply) {
            // be sure we are sending a good packet

            if (!recipient.equalsIgnoreCase("server")) {
                if (recipient.equalsIgnoreCase("all")) { // send to every clients
                    for (final ClientLogged users : this.getUserManager().getAllUsers()) {
                        users.sender.writeObject(obj);
                        users.sender.flush();
                    }
                } else { // send to unique client (or multiple terminals with the same name)
                    for (final ClientLogged users : this.getUserManager().getUser(recipient)) {
                        users.sender.writeObject(obj);
                        users.sender.flush();
                    }
                }
            }
        }
    }


    public final void onMessage (final String channel, final BiConsumer<MessageEvent.SERVER, ClientLogged> consumer) {
        this.catcherManager.addMessageEvent(channel, consumer);
    }
    public final void onAction (final String name, final BiConsumer<ActionEvent.SERVER, ClientLogged> consumer) {
        this.catcherManager.addActionEvent(name, consumer);
    }


    public final LoginManager getLoginManager () {
        return this.loginManager;
    }

    public final UserManager getUserManager () {
        return this.userManager;
    }

    @Override public void close () {
        this.isEnabled = false;
        try {
            this.server.close();
        } catch (final Exception ignored) {

        }

    }
    public final boolean isClosed () {
        return !isEnabled;
    }
    public final boolean isOpen () {
        return isEnabled;
    }
}
