package com.derkote.server;


import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class ConnectionHandler implements Runnable{
    private ConcurrentHashMap<Integer, Connection> connectionPool;
    private int id;
    private boolean isInterrupt;
    Logger log;

    /* Constructors */

    public ConnectionHandler(Logger log) {
        this.log = log;
        connectionPool = new ConcurrentHashMap<>(10, 0.8f);
        id = 0;
        isInterrupt = false;
    }



    /* Methods */

    @Override
    public void run() {
        while (true) {
            if (isInterrupt) {
                // Отмечаем все connections как interrupt
                log.log(Level.FINE, "Connection handler tread is stopped");
                for (Connection connection : connectionPool.values()) {
                    connection.interrupt();
                }
                break;
            }
            //System.out.println("Connection handler is GOOD");


            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Smthg wrong", e);
            }
        }
    }


    public void put(Connection connection) {
        connectionPool.put(++id, connection);
    }


    /* Getters & Setters */


    public void setInterrupt() {
        isInterrupt = true;
    }

    public boolean isInterrupt() {
        return isInterrupt;
    }
}
