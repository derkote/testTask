package com.derkote.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class Server implements Runnable{
    private Settings set;
    private ServerSocket serverSocket;
    private Socket socket;
    private ConnectionHandler connectionHandler;
    private boolean isInterrupt;
    private FileCounter fileCounter;
    com.derkote.server.FileReader fr;
    Logger log;


    /* Constructors */

    public Server(Settings set, com.derkote.server.FileReader fr, Logger log) {
        this.log = log;


        connectionHandler = new ConnectionHandler(log);
        Thread handlerThread = new Thread(connectionHandler);
        handlerThread.start();

        this.set = set;
        this.fr = fr;
        isInterrupt = false;
        fileCounter = FileCounter.getFileCounter();
    }


    /* Methods */

    @Override
    public void run() {
        int id = 0;
        try {
            serverSocket = new ServerSocket(set.getPort());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not create ServerSocket", e);
            throw new RuntimeException("Could not create ServerSocket", e);
        }
        while (true) {
            if (isInterrupt) {
                log.log(Level.FINE, "Server tread is stopped");
                connectionHandler.setInterrupt();
                break;
            } else {
                try {
                    socket = null;
                    try {
                        socket = serverSocket.accept();
                        log.log(Level.FINE, "socket is involved");
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Error activating the socket", e);
                    }
                    Connection connection = new Connection(socket, fr, ++id, log);
                    new Thread(connection).start();
                    connectionHandler.put(connection);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Could not create Connection", e);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Smthg wrong", e);
            }
        }
    }


    /* Getters & Setters */


    public boolean setInterrupt() {
        isInterrupt = true;
        connectionHandler.setInterrupt();
        fileCounter.setInterrupt();
        return isInterrupt;
    }
}
