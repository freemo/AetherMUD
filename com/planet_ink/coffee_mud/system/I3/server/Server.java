/*
 * imaginary.server.Server
 * Copyright (c) 1996 George Reese
 * The mudlib interface to the server.
 */
package com.planet_ink.coffee_mud.system.I3.server;


/**
 * The Server class is the mudlib's interface to the
 * Imaginary Mud Server.  It is responsible with knowing all
 * internal information about the server.
 * Last Update: 960921
 * @author George Reese
 * @version 1.0
 */
public class Server {
    static private ServerThread thread = null;
    static private boolean started = false;

    /**
     * Creates a server thread if one has not yet been
     * created.
     * @exception DatabaseException thrown if the database is unreachable
     * for some reason
     * @exception ServerSecurityException thrown if an attempt to call start()
     * is made once the server is running.
     * @param mud the name of the mud being started
     */
    static public void start(String mud, int port) throws ServerSecurityException {
        if( started ) {
            throw new ServerSecurityException("Illegal attempt to start Server.");
        }
        else {
            started = true;
        }
        thread = new ServerThread(mud, port);
        thread.start();
    }

    /**
     * Returns a distinct copy of the class identified.
     * @exception ObjectLoadException thrown when a problem occurs loading the object
     * @param file the name of the class being loaded
     */
    static public ServerObject copyObject(String file) throws ObjectLoadException {
        return thread.copyObject(file);
    }

    static public ServerObject findObject(String file) throws ObjectLoadException {
        return thread.findObject(file);
    }

    static public ServerUser[] getInteractives() {
        return thread.getInteractives();
    }

    static public String getMudName() {
        return thread.getMudName();
    }

    static public int getPort() {
        return thread.getPort();
    }

    static public void removeObject(ServerObject ob) {
        if( !ob.getDestructed() ) {
            return;
        }
        thread.removeObject(ob);
    }
}
