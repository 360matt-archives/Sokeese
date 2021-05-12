# 💥 Sokeese - A Socket API
This rich and varied library will greatly facilitate development, it is very light, contains no dependencies, and offers performance almost equal to vanilla sockets

## 💙 Features:
* Dematerialized authentication system (with cryptogtaphy)
* Mode system, with 3 choices of permission settings
* Open to the internet
* Response / callback system with delay
* Possible to limit the number of simultaneously connected clients
* Header on Message requests.

### Authentication module:
Customers log in by presenting their username, group and token.   
  
Each client's token is never stored, yet authentication works even after a server restart, how do you think it is?  
The token is the Sha256 of the concatenation ``NAME`` + ``GROUP`` + ``SERVER PRIVATE KEY``.  

The server must solve this equation with its private key to generate a token,  
but also at each connection to compare the tokens and thus authorize the connection.  

If you want to "create" accounts automatically, just use this equation wherever you want.

### Autorization Mode system:
In the server options you will have the choice between:
* ALL (each client can send a Message request to all clients at once without specifying their name)
* SINGLE (each customer will have to know the name of the customers to whom he wants to send, he will have to send them one by one if there are several)
* NOTHING (no exchanges will be allowed, for all customers without exceptions)

### Open to the internet:
You can easily open the server port to the internet if your use requires it,  
You will not have to worry about the authentication system.

### Response / Callback system:
When a client receives a Message request, it can return a response to that request using the request id it contains in the header (the ``reply(...)`` methods will do the job for you).

On the other side, from the sender, once the request has been sent, he can wait for an asynchronous response specific to this request, with a temporary single-use biconsumer event.


### Limit
You can easily limit the number of clients allowed to connect simultaneously  
You can also allow a number of clients with the same username (so yes, you can connect clients with the same username logged in)  


If any of the thresholds are met, the connection is simply reset after sending the error message to the client.  


## Requests body:
### Action request:
Action requests allow the simple exchange between the client and the server, without response support, it does not contain a header.  
This is the most minimalist request.
```java
final Action action = new Action();
// we create a Action instance (or use existant in lambda ...)

action.setName("hello"); // the name of the action.
action.setContent(new Object()); // can send all serializable object (must be the same class at other side)

// permet to define the map, add values in lambda, and set the content
action.setMap(map0 -> {
    map0.put("test", "hello world");
});


String name = action.getName();
Object content = action.getContent();

Map<?, ?> map = action.getMap();
Map<Vector<?>, Arrays> map_wtf = action.getMap(); // we can get as all types

// permit to get map values in a lambda
action.getMap(map3 -> {

});
```
### Message request:
Message requests are the most complete and sophisticated, they contain headers such as sender and recipient,  
as well as a request id which makes the response / callback service possible
```java
final Message message = new Message();
// we create a Message instance (or use existant in lambda ...)

message.setChannel("hello"); // the name of the channel.
message.setRecipient("Mr. Krabks"); // choose the client recipient ("server" to send to the server)
message.setContent(new Object()); // can send all serializable object (must be the same class at other side)

// permet to define the map, add values in lambda, and set the content
message.setMap(map0 -> {
    map0.put("test", "hello world");
});


String channel = message.getChannel();
String recipient = message.getRecipient();
Object content = message.getContent();

Map<?, ?> map = message.getMap();
Map<Vector<?>, Arrays> map_wtf = message.getMap(); // we can get as all types

// permit to get map values in a lambda
message.getMap(map3 -> {

});
```


## Create a server
To start a Sokeese server, it's quite simple:
```java
final ServerOptions serverOptions = new ServerOptions();
serverOptions.setMaxClients(int default 50); // only 10 clients accepted simultaneous
serverOptions.setDebug(boolean default false); // permit to print all exceptions
serverOptions.setMaxSameClient(int default 10); // if clients can have the same username in multiple sessions
serverOptions.setLevelMessages(ServerOptions.Level default SINGLE); // if clients can send Message requests to others clients


final SokeeseServer server = new SokeeseServer( int port, String privateKey, [ServerOptions options] );
// server options is optionnal

final ServerOptions options2 = server.getOptions();
// You can [re]change all options after starting the server by getting the ServerOptions instance.


/**
* For the following request-sending methods, the server cannot take advantage of the root-level response system.
* You must send the request from an instance of ClienLogged
**/



server.sendMessage( Message );
// The API will send this request to the client concerned using the "recipient" field

server.sendAction("recipient", Action );
// As Action requests do not have a header, we must in this case inform the container in the method


server.sendMessage(Message -> {
    // define the request here
});

server.sendAction("recipient", (Action) -> {
    // define the request here
}));


server.close();
// permit to close the server.

boolean state = server.isClosed();
// get the current state of the server
```  
  
Now, we will learn how to harness the potential of the API methods of the server:  

### User Manager
The user manager makes it possible to recover the desired user instances and to be able to disconnect them
```java
final UserManager usrMan = server.getUserManager();

int clientCount = usrMan.getUserCount("a name");
// how many client under this name are connected

int globalCount = usrMan.getCount();
// the number of connected clients

final Set<ClientLogged> clients = usrMan.getUser("hello");
// get all instances of clients that have this name, can be null.

final boolean exist = usrMan.exist("bonjour");
// if at least one client is logged in under this name

usrMan.disconnect("hey");
// disconnect all clients that have this name

usrMan.disconnectAll();
// disconnect all clients

final Set<ClientLogged> allClients = usrMan.getAllUsers();
// get all connected clients
```

### Login manager
Allows you to generate a token from a provided Session instance
```java
final LoginManager loginMan = server.getLoginManager();

String token = loginMan.getTokenRelated(Session session);
```

### Registering events
Great novelty for this API: the server can now process requests in addition to redistributing them  

#### For Action event:
```java

server.onAction("action name", (event, client) -> {
    Map<K, V> map = event.getMap();
    // convert the Action#content as Map type

    // or
    event.getMap(mapLambda -> {
      // do stuff with map in lambda
    });


    event.getName();
    // = "action name" for example
    
    event.getContent();
    // get content of the request

    Action action = event.getRequest();
    // the raw Action request received

    event.send( Action );
    // send a Action request to the client

    event.send( Message );
    // send a Message request to the client

    event.sendAction(Action -> {
      x.setName("test");
      x.setContent("the content");
      // the server will send x
    });

    event.sendMessage(Message -> {
      x.setChannel("the channel for the listener");
      x.setSender("server"); // only server can overwrite the sender
      x.setContent(new Object()); // can be all types, must be Serializable and same class other side
      // the server will x 
    });
});
```

#### For Message request:
```java
server.onMessage("channel name", (event, client) -> {
    Map<K, V> map = event.getMap();
    // convert the Action#content as Map type

    // or
    event.getMap(mapLambda -> {
      // do stuff with map in lambda
    });


    event.getChannel();
    // retrieve the channel name
    
    event.getSender();
    // retrieve the sender name
    
    event.getContent();
    // retrieve the content of request

    Action action = event.getRequest();
    // the raw Message request received

    event.sendMessage( Action );
    // send a Action request to the client

    event.sendAction( Message );
    // send a Message request to the client

    event.sendAction(Action -> {
      x.setName("test");
      x.setContent("the content");
      // the server will send x
    });

    event.sendMessage(Message -> {
      x.setChannel("the channel for the listener");
      x.setSender("server"); // only server can overwrite the sender
      x.setContent(new Object()); // can be all types, must be Serializable and same class other side
      // the server will x
    });
});
```
### What to do with a ClientLogged ?
An instance of this class represents a client session.  
If multiple clients are connected with the same name, they each have a dedicated instance.
```java
client.sendMessage( Message );
client.sendAction( Action );

client.sendAction((Action action) -> {

});

client.sendMessage((Message message) -> {

});

// catch a reply while 200ms (default)
client.sendMessage(new Message(), (Reply reply, Boolean isTimeout) -> {
    // reply is the request reply received
});

// with delay: in example: 50ms
client.sendMessage(new Message(), 50, (Reply reply, Boolean isTimeout) -> {
    // reply is the request reply received
});

Session session = client.getSession();
// the exact session sent from the client at login phase.

client.close();
// disconnect the client and close the instance, eliminate it in GC
```
