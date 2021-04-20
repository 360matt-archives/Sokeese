package fr.i360matt.sokeese.commons.modules;

import fr.i360matt.sokeese.server.ClientLogged;

import java.io.Closeable;
import java.util.*;

/**
 * Allows to list the connected users to the server.
 *
 * @author 360matt
 * @version 1.1.0
 */
public class UserManager implements Closeable {
    private Map<String, ClientLogged> single = new HashMap<>();
    private Map<String, Set<ClientLogged>> multiple = new HashMap<>();

    private int count = 0;

    /**
     * Allows you to know how many clients are connected to the server
     * @return number of clients are connected to the server
     */
    public final int getCount () {
        return this.count;
    }

    /**
     * Allows you to know how many clients of a certain user are connected to the server
     * @return number of user's clients are connected to the server
     */
    public final int getUserCount (final String name) {
        if (this.single == null)
            return 0;
        else if (this.single.containsKey(name)) {
            return 1;
        } else {
            return this.multiple.get(name).size();
        }
    }

    /**
     * Allows to add the connected customer to his name.
     * If a client with the same name already exists,
     * We place the two clients with the same name in the multiple Map.
     * @param client An instance of the client.
     */
    public final void addUser (final ClientLogged client) {
        if (this.single == null) return;
        final ClientLogged probably = this.single.get(client.getSession().name);
        if (probably == null)
            this.single.put(client.getSession().name, client);
        else {
            final Set<ClientLogged> newList = new HashSet<>();
            newList.add(probably);
            newList.add(client);
            this.multiple.put(client.getSession().name, newList);
            this.single.remove(client.getSession().name);
        }
        this.count++;
    }

    /**
     * Allows to delete all the references of a user from the list of users.
     * @param name The name of the user to delete.
     */
    public final void removeUser (final String name) {
        if (this.single == null) return;
        if (this.single.containsKey(name)) {
            this.single.remove(name);
            this.count--;
        } else {
            this.count -= this.multiple.get(name).size();
            this.multiple.remove(name);
        }
    }

    /**
     * Allows to delete a user reference from the user list
     * @param client The reference of the user to be deleted.
     */
    public final void removeUser (final ClientLogged client) {
        if (this.single == null) return;
        if (client.getSession() != null) {
            if (this.single.containsValue(client)) {
                this.single.remove(client.getSession().name);
                this.count--;
            } else if (multiple.containsKey(client.getSession().name)) {
                this.multiple.get(client.getSession().name).remove(client);
                this.count--;
            }
        }
    }

    /**
     Allows you to disconnect all sessions of a user by name.
     * @param name The name of the user to log out.
     */
    public final void disconnect (final String name) {
        if (this.single == null) return;
        final ClientLogged probably;
        if ((probably = this.single.get(name)) != null)
            probably.close();
        else {
            final Set<ClientLogged> probably2;
            if ((probably2 = this.multiple.get(name)) != null)
                probably2.forEach(ClientLogged::close);
        }
    }

    /**
     * Allows to disconnect all users
     */
    public final void disconnectAll () {
        if (this.single == null) return;
        this.single.values().forEach(ClientLogged::close);
        this.multiple.values().forEach(clients -> {
            clients.forEach(ClientLogged::close);
        });
    }

    /**
     * Allows to retrieve all the references of a logged in user
     * @param name User name.
     * @return The list containing the active references of the searched user.
     */
    public final Set<ClientLogged> getUser (final String name) {
        if (this.single != null) {
            final ClientLogged probably;
            if ((probably = this.single.get(name)) != null)
                return new HashSet<>(Collections.singletonList(probably));
            else {
                final Set<ClientLogged> probably2;
                if ((probably2 = this.multiple.get(name)) != null)
                    return probably2;
            }
        }
        return new HashSet<>();
    }

    /**
     * Allows to retrieve all the references of connected users
     * @return The list containing all the references of connected users.
     */
    public final Set<ClientLogged> getAllUsers () {
        if (this.single != null) {
            final Set<ClientLogged> res = new HashSet<>(single.values());
            this.multiple.values().forEach(res::addAll);
            return res;
        }
        return new HashSet<>();
    }

    /**
     * Allows to find out if a user with a certain name is currently logged in.
     * @param name The name of the user concerned.
     * @return Whether the user is online or not.
     */
    public final boolean exist (final String name) {
        return this.single != null && (this.single.containsKey(name) || this.multiple.containsKey(name));
    }

    /**
     * Allows to delete all references
     */
    @Override
    public void close () {
        this.disconnectAll();
        this.single = null;
        this.multiple = null;
    }
}