package com.derkote.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class FileCounter implements Runnable{
    private ConcurrentHashMap<File, Integer> counterMap;
    private static FileCounter fileCounter;
    // Интервал записи в файл
    private int writingInterval;
    private boolean isInterrupt;
    // Файл для записи
    private File path;
    private Logger log;


    /* Constructors */


    public FileCounter(Logger log, int writingInterval, File path) {
        this.log = log;
        this.writingInterval = writingInterval;
        this.path = path;
        log.log(Level.FINE, "FileCounter private constructor");
        counterMap = new ConcurrentHashMap<>(10, 0.8f);
        isInterrupt = false;
        fileCounter = this;
    }


    /* Methods */


    // Увеличивает на еденицу счетчик файла file
    public synchronized void add(File file) {
        log.log(Level.FINE, "Add one more downloading");
        if (counterMap.containsKey(file)) {
            int tempValue = counterMap.get(file);
            counterMap.replace(file, tempValue + 1);
        } else {
            counterMap.put(file, 1);
        }
    }

    // Инициализация счетчика
    public void init() {
        log.log(Level.FINE, "Init FileCounter");
    }

    @Override
    public void run() {
        while (!isInterrupt) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(path);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error creating statistics file");
            }

            for (Map.Entry<File, Integer> fileIntegerEntry : counterMap.entrySet()) {
                try {
                    writer.write(fileIntegerEntry.getKey() + ":  " + fileIntegerEntry.getValue() + " раз.     ");
                    writer.flush();

                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error writing statictic file");
                }
            }

            try {
                Thread.sleep(writingInterval*1000);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Sleep error", e);
            }
        }
    }


    /* Getters & Setters */


    public void setInterrupt() {
        isInterrupt = true;
    }

    public static FileCounter getFileCounter() {
        return fileCounter;
    }
}
