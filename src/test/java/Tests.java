import fr.i360matt.sokeese.client.Login;
import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.server.ServerOptions;
import fr.i360matt.sokeese.server.SokeeseServer;


public class Tests {

    public static void main (final String[] args) {

        final ServerOptions serverOptions = new ServerOptions();
        serverOptions.setMaxClients(10); // only 10 clients accepted simultaneous



        final SokeeseServer server = new SokeeseServer(25565, serverOptions);
        server.enableLoginManager((username, password) -> {
            return true; // no check, just for test
        });


        server.onMessage("camion", (event, client) -> {

            event.reply("truc");
        });


        final Login login = new Login("ah", "truc");
        final SokeeseClient client = new SokeeseClient("127.0.0.1", 25565, login);


        client.sendMessage((msg) -> {
            msg.setRecipient("server");
            msg.setChannel("camion");


           // msg.setContent("venez y'a un con devant ma porte");

            msg.setMap(map -> {
                map.put("", "");
            });



        }, 100, (reply, state) -> {
            System.out.println("Reponse: " + reply.getContent());
        });




    }

}
