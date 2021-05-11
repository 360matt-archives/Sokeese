import fr.i360matt.sokeese.server.ServerOptions;
import fr.i360matt.sokeese.server.SokeeseServer;


public class Tests {

    public static void main (final String[] args) {

        final ServerOptions serverOptions = new ServerOptions();
        serverOptions.setMaxClients(10); // only 10 clients accepted simultaneous

        final SokeeseServer server = new SokeeseServer(25565, "key", serverOptions);


        server.onMessage("hello", (event, client) -> {
            event.reply("truc");
        });






    }

}
