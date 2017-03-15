package com.derkote.server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class FileReader {
    private Settings settings;
    private ConcurrentHashMap<String, File> files;
    private File file;
    private Logger log = Logger.getLogger(FileReader.class.getName());


    /* Constructors */

    public FileReader(Settings settings) throws IOException {
        LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("logging.cfg"));
        this.settings = settings;
        file = settings.getPath();
        files = new ConcurrentHashMap<>(10, 0.8f);
        if (file.isDirectory()) {
            log.log(Level.INFO, "This file is directory");
            File[] tempFiles = settings.getPath().listFiles();
            for (int i = 0; i < tempFiles.length; i++) {
                files.put(tempFiles[i].getName(), tempFiles[i]);
            }
        }
    }


    /* Methods */

    public File getPath() {
        return file;
    }

    public File getFile(String filename) {
        return files.get(filename);
    }


}
