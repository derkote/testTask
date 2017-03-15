package com.derkote.server;

import java.io.File;

/**
 * Created by derkote.
 */
public class Data implements Message {
    private File file;


    /* Constructors */

    public Data(File file) {
        this.file = file;
    }


    /* Methods */

    public File getFile() {
        return file;
    }
}
