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
(will come)


(will come)
(will come)
(will come)
Be patient
