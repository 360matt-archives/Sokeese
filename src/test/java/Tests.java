import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.SokeeseServer;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class Tests {

    public static void main (final String[] args) throws InterruptedException {

        final SokeeseServer server = new SokeeseServer(25565, "key");


        server.onMessage("i", (event, client) -> {

            final Reply reply = new Reply();
            reply.content = 0;

            event.reply(reply);
        });


        Thread.sleep(1000);

        final Session session = new Session();
        session.name = "cetus";
        session.group = "admin";
        session.token = "7e1f7d5986709da333cccc722347ca8b96c8668b947a7f9f8b4e1d7a53c33da";

        final SokeeseClient client = new SokeeseClient("91.167.152.22", 25565, session);



        final Random random = new Random();





        for (int i = 0; i < 10_000; i++) {
            byte[] array = new byte[1024 * 16];
            random.nextBytes(array);

            final Message message = new Message();
            message.channel = "i";
            message.recipient = "server";
            message.content = new String(array, StandardCharsets.UTF_8);

            array = null;

            final AtomicLong atomicLong = new AtomicLong(System.nanoTime());

            client.send(message, (reply, state) -> {
                System.out.println(System.nanoTime() - atomicLong.get());
            });

            TimeUnit.MILLISECONDS.sleep(10);
        }






    }

}
