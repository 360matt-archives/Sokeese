import fr.i360matt.sokeese.client.SokeeseClient;
import fr.i360matt.sokeese.commons.Session;
import fr.i360matt.sokeese.commons.requests.Message;
import fr.i360matt.sokeese.commons.requests.Reply;
import fr.i360matt.sokeese.server.SokeeseServer;
import fr.i360matt.sokeese.utils.RunnableSerialized;

import javax.swing.*;


public class Tests {

    public static void main (final String[] args) {

        final SokeeseServer server = new SokeeseServer(25565, "key");
        server.onMessage("truc", ((event, clientLogged) -> {
            System.out.println(event.getMessage().idRequest);
            ((Runnable) event.getMessage().content).run();

            Reply reply = new Reply();
            reply.channel = "test";
            reply.content = "Woaaw";

            event.reply(reply);
        }));


        final Session session = new Session();
        session.name = "360matt";
        session.group = "admin";
        session.token = server.getLoginManager().getTokenRelated(session);


        final SokeeseClient client = new SokeeseClient("127.0.0.1", 25565, session);

        final RunnableSerialized runnable = new RunnableSerialized() {
            @Override
            public void run () {
                JFrame f=new JFrame();//creating instance of JFrame

                JButton b=new JButton("click");//creating instance of JButton
                b.setBounds(130,100,100, 40);//x axis, y axis, width, height

                f.add(b);//adding button in JFrame

                f.setSize(400,500);//400 width and 500 height
                f.setLayout(null);//using no layout managers
                f.setVisible(true);//making the frame visible
            }
        };

        final Message action = new Message();
        action.channel = "truc";
        action.recipient = "server";
        action.content = runnable;

        client.sendObject(action, (reply, state) -> {

            System.out.println(state);

            System.out.println(reply.content);
        });


    }

}
