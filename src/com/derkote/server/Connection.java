package com.derkote.server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class Connection implements Runnable {

    private Socket socket;
    private com.derkote.server.FileReader fileReader;
    private ObjectInputStream objInputStream;
    private ObjectOutputStream  objOutputStream;
    private Logger log;
    private int id;
    private boolean isInterrupt;
    private FileCounter fileCounter;

    /* Constructors */

    public Connection(Socket socket, com.derkote.server.FileReader fileReader, int id, Logger log) throws IOException {
        this.log = log;
        this.socket = socket;
        this.fileReader = fileReader;
        this.id = id;
        isInterrupt = false;
        fileCounter = FileCounter.getFileCounter();
        socket.setSoTimeout(5000);
    }


    /* Methods */

    @Override
    public void run() {
        // Создаем IO потоки
        createStreams();

        while (true) {
            // Если входящий поток закрыт, отмечаем поток как мертвый
            if (socket.isInputShutdown()) {
                interrupt();
            }

            // Если поток мертвый пытаемся отправить сообщение озавершении сессии клиенту,
            // Закрываем IOStreams, завершаем цикл
            if (isInterrupt) {
                log.log(Level.FINE, "Connection is interrupt");
                if (!socket.isInputShutdown()) {
                    try {
                        objOutputStream.writeObject(new Request(RequestType.EXIT));
                        objOutputStream.flush();
                        log.log(Level.FINE, "Have sent a message about stopping the server");
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Somthing wrong", e);
                    }
                }
                try {
                    closeStreams();
                    log.log(Level.FINE, "Successfully closed streams");
                } catch (IOException e) {
                    log.log(Level.WARNING, "Could not closed streams", e);
                }
                break;
            }
            // Принимаем запрос клиента, отправляем ответ
            try {
                Message message = null;
                try {
                    message = (Message) objInputStream.readObject();
                } catch (IOException e) {
                    log.log(Level.FINE, "IOStream timeout", e);
                }
                log.log(Level.FINE, "Have got Message");
                if (message instanceof Request) {
                    log.log(Level.FINE, "Message is Request");
                    Request request = (Request) message;
                    switch (request.getRequestType()) {
                        case LIST:
                            objOutputStream.writeObject(new Data(fileReader.getPath()));
                            objOutputStream.flush();
                            log.log(Level.FINE, "List has been sent");
                            break;
                        case EXIT:
                            interrupt();
                            break;
                        case FILE:
                            sendFile(request);
                            break;
                        default:
                            break;
                    }
                } else {
                    log.log(Level.INFO, "Message is not request.");
                }

            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "InputStream is corrupted?", e);
            } catch (IOException e) {
                interrupt();
                log.log(Level.SEVERE, "IOStreams  have not been working", e);
            }
        }

    }

    //Создаем IOStreams
    private void createStreams() {
        log.log(Level.FINE, "New connection running");
        try {
            objInputStream = new ObjectInputStream(socket.getInputStream());
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            log.log(Level.FINE, "IOStreams created ");
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOStreams have not created", e);
        }
    }

    // Закрываем IOStreams и сокет
    private void closeStreams() throws IOException {
        System.out.println("Close IO Streams" + id);

        objOutputStream.close();
        objInputStream.close();
        socket.close();
    }

    public boolean sendFile(Request request) throws IOException {
        File file = fileReader.getFile(request.getFileName());
        // Отправляем имя файла
        objOutputStream.writeObject(new Data(file));
        objOutputStream.flush();

        // Отправляем длину файла
        int length = 0;
        length = (int) file.length();
        objOutputStream.write(length);
        objOutputStream.flush();

        // Отправляем данные
        byte[] arr = new byte[length];
        byte[] b = new byte[1];
        FileInputStream f = null;
        f = new FileInputStream(file);
        for (int i = 0; i < length; i++) {
            f.read(b);
            arr[i] = b[0];
        }
        objOutputStream.write(arr);
        objOutputStream.flush();
        fileCounter.add(file);
        log.log(Level.FINE, "File has been sent");
        return true;
    }



    /* Getters & Setters */


    // Устанавливает метку смерти потока
    public void interrupt() {
        isInterrupt = true;
    }

}
