package com.derkote.client;

import com.derkote.server.Data;
import com.derkote.server.Message;
import com.derkote.server.Request;
import com.derkote.server.RequestType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Created by derkote.
 * Клиент при подключении выдает приглашение пользователю на ввод команды, возможные команды:
 * Запрос списка доступных файлов.
 * Скачать один файл и сохранить его локально.
 * Завершить сессию.
 * После выполнения запросов 1 или 2 клиент ожидает новой команды от пользователя.
 */
public class Client {
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket s;
    static Logger log = Logger.getLogger(Client.class.getName());


    /* Methods */

    public static void main(String[] args) {
        try {
            LogManager.getLogManager().readConfiguration(Client.class.getResourceAsStream("logging.cfg"));
            log.log(Level.INFO, "Logger has been initialized");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not loaded logging.cfg", e);
        }
        Client client = new Client();
            client.start();
    }

    private void init() throws IOException {
        //Загружаем настойки для поключения к серверу
        log.log(Level.FINE, "Load settings");
        try {
            s = new Socket(InetAddress.getByName("127.0.0.1"), 19876);
            log.log(Level.FINE, "Socket has been created");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Socket has not been created");
            throw new IOException("Socket has not been created", e);
        }

        try {
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);
            log.log(Level.FINE, "ObjectIOStream has been created");
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOStream error", e);
            throw new IOException("IOStream error", e);
        }
    }

    private void start() {
        //Подключаемся к серверу
        try {
            init();
            log.log(Level.FINE, "Connection has been created");
            //Выводим приветственное сообщение и список команд
            startDialog();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Something wrong", e);
            System.out.print("Ошибка подключения к серверу. Попробуй перезапустить.");
        }


        //Обрабатываем ввод пользователя
        while (true) {
            Message message = (Message) getRequest();
            try {
                System.out.println("Enter: ");
                sendMessage(message);
                Thread.sleep(500);
                log.log(Level.FINE, "Otpravili message");
                message = getMessage();
                log.log(Level.FINE, "Poluchili message");
                if (message instanceof Data) {
                    Data data = (Data) message;
                    File file = parseMessage(message);
                    if (file.isDirectory()) {
                        log.log(Level.FINE, "Eto papka");
                        showFileList(file);
                    }
                    if (file.isFile()) {
                        log.log(Level.FINE, "Eto file");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getName()));

                        int length = 0;
                        length = ois.read();
                        byte[] arr = new byte[length];
                        byte[] b = new byte[1];

                        for (int i = 0; i < length; i++) {
                            ois.read(b);
                            writer.write(b[0]);
                            writer.flush();
                            System.out.println(b[0]);
                        }
                        log.log(Level.FINE, "Zapisali file");
                    }
                }
                if (message instanceof Request) {
                    Request request = (Request) message;
                    if (RequestType.EXIT == request.getRequestType()) {
                        System.out.println("Сервер разорвал соединение");
                        break;
                    }
                }

            } catch (IOException e) {
                log.log(Level.FINE, "IOExcp",e);
            } catch (ClassNotFoundException e) {
                log.log(Level.FINE, "ClassNotFound", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.log(Level.FINE, "Povtoryaem obrabotku");
        }



    }

    private void showFileList(File file) {
        File[] files = file.listFiles();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            stringBuilder.append(files[i].getName() + " ");
        }
        System.out.println(stringBuilder.toString());
    }

    private File parseMessage(Message message) {
        if (message instanceof Data) {
            Data data = (Data) message;
            return data.getFile();
        } else {
            System.err.println("Message isnt Data");
            return null;
        }
    }

    private void startDialog() {
        System.out.println("Для продолжения работы введите команды:");
        System.out.println("Список: Получить список файлов");
        System.out.println("Имя_файла: Получить файл с именем");
        System.out.println("Выход: Завершить сессию");
    }

    private Request getRequest() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String temp = null;
        try {
            temp  = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (temp) {
            case "Список":
                return new Request(RequestType.LIST);
            case "Выход":
                return new Request(RequestType.EXIT);
            default:
                return new Request(RequestType.FILE, temp);
        }

    }

    private Message getMessage() throws IOException, ClassNotFoundException {
        return (Message) ois.readObject();
    }

    private void sendMessage(Message message) throws IOException {
        oos.writeObject(message);
        oos.flush();
    }

}
