import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.server.ServerOptions;
import fr.i360matt.sokeese.server.SokeeseServer;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class Tests {

    public static void main (final String[] args) {

        final ServerOptions serverOptions = new ServerOptions();
        serverOptions.setMaxClients(10); // only 10 clients accepted simultaneous

        final SokeeseServer server = new SokeeseServer(25565, "key", serverOptions);




        server.onMessage("none", (event, client) -> {
            event.reply("truc");
        });






        final Session session = new Session();
        session.name = "cetus";
        session.group = "admin";
        session.token = "7e1f7d5986709da333cccc722347ca8b96c8668b947a7f9f8b4e1d7a53c33da";

        final SokeeseClient client = new SokeeseClient("91.167.152.22", 25565, session);



        final Message message = new Message();
        message.channel = "un_Channel";
        message.recipient = "server";
        message.content = "du contenu";

        client.sendMessage(message);





        client.sendMessage(message1 -> {
            message1.channel = "getRank";
            message1.recipient = "proxy";
            message1.content = "360mat";
        });




        final Random random = new Random();


        for (int i = 0; i < 10_000; i++) {
            byte[] array = new byte[1024];
            random.nextBytes(array);

            client.sendMessage(message1 -> {
                message1.channel = "i";
                message1.recipient = "server";
                message1.content = "o";
            }, (reply, state) -> {
               // System.out.println(compteur.incrementAndGet());
            });

        }






    }

}
