import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.ServerOptions;
import fr.i360matt.sokeese.server.SokeeseServer;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Tests {

    public static void main (final String[] args) {

        final ServerOptions serverOptions = new ServerOptions();
        serverOptions.setMaxClients(1);

        final SokeeseServer server = new SokeeseServer(25565, "key", serverOptions);

        final Random random = new Random();

        server.onMessage("none", (event, client) -> {

            byte[] array = new byte[1024 * 1024 * 8];
            random.nextBytes(array);

            final Reply reply = new Reply();
            reply.content = new String(array, StandardCharsets.UTF_8);

            array = null;

            /*event.reply(reply1 -> {
                reply1.content = "truc";
            });*/

            event.reply("truc");


        });







        final Session session = new Session();
        session.name = "cetus";
        session.group = "admin";
        session.token = "7e1f7d5986709da333cccc722347ca8b96c8668b947a7f9f8b4e1d7a53c33da";

        final SokeeseClient client = new SokeeseClient("91.167.152.22", 25565, session);




        final AtomicInteger compteur = new AtomicInteger();

        for (int i = 0; i < 10_000; i++) {
            byte[] array = new byte[1024];
            random.nextBytes(array);

            final Message message = new Message();
            message.channel = "i";
            message.recipient = "server";
            message.content = "o";

            client.send(message, (reply, state) -> {
                System.out.println(compteur.incrementAndGet());
            });

        }








    }

}
