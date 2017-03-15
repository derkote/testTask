package com.derkote.server;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class Main {
    public static void main(String[] args) {
        // Инициализация логера
        Logger log = Logger.getLogger(Main.class.getName());
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("logging.cfg"));
            log.log(Level.INFO, "Logger has been initialized");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not loaded logging.cfg", e);
        }

        // Инициализация конфигурации
        Settings set = null;
        try {
            set = new Settings(new File("settings.xml"));
            log.log(Level.INFO, "settings.xml loaded");
            System.out.println(set.getPort());
            System.out.println(set.getIp());
            System.out.println(set.getPath());
            System.out.println(set.getTimeSpan());
            System.out.println(set.getWritingInterval());
            System.out.println(set.getStatName());
        } catch(Exception e) {
            log.log(Level.SEVERE, "Could not loaded settings.xml", e);
        }

        // Инициализация и запуск счетчика скачиваний файлов
        FileCounter fileCounter = new FileCounter(log, set.getWritingInterval(), set.getStatName());
        Thread counterThread = new Thread(fileCounter);
        counterThread.start();
        log.log(Level.FINE, "FileCounter has been running");


        // Инициализация и запуск сервера
        Thread serverThread = null;
        Server server = null;
        try {
             com.derkote.server.FileReader fileReader = new com.derkote.server.FileReader(set);
             server = new Server(set, fileReader, log);
             serverThread = new Thread(server);
             serverThread.start();
             log.log(Level.INFO, "Server has been started");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Server had't started", e);
        }

        // Считываем ввод пользователя, выключаем сервер на любой ввод
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String temp = null;
        try {
            temp  = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (temp != null) {
            server.setInterrupt();
            log.log(Level.SEVERE, "Shutting down server");
        }
    }
}
