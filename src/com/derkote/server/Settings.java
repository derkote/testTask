package com.derkote.server;

import com.derkote.client.Client;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by derkote.
 */
public class Settings {
    //Имя файла конфигурации
    private File name;
    //Путь к рабочей папке
    private File path;
    //Интервал сохранения статистики
    private int timeSpan;
    //Порт сервера
    private int port;
    //IP адресс сервера
    private InetAddress ip;
    //Частота сохранения статистики (сек)
    private int writingInterval;
    //Имя файла статистики
    private File statName;
    Logger log = Logger.getLogger(Main.class.getName());

    /* Constructors */

    public Settings() {
        try {
            LogManager.getLogManager().readConfiguration(Client.class.getResourceAsStream("logging.cfg"));
            log.log(Level.INFO, "Logger has been initialized");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not loaded logging.cfg", e);
        }
        this.name = new File("settings.xml");
        this.path = new File("WorkFolder");
        this.statName = new File("stat.txt");
        this.timeSpan = 5;
        this.port = 8756;
        this.writingInterval = 30;
        try {
            this.ip = InetAddress.getLocalHost();
            log.log(Level.FINE, "Good ip");
        } catch (UnknownHostException e) {
            log.log(Level.SEVERE, "Wrong ip", e);
        }
    }

    public Settings(File name) {
        this();
        if (name != null) {
            this.name = name;
            try {
                initFromFile();
                log.log(Level.FINE, "Init from file");
            } catch (UnknownHostException e) {
                log.log(Level.SEVERE, "Wrong ip. Use default settings");
            }

        }else{
            log.log(Level.INFO, "Settings file not found. Use default settings");
        }


    }


    /* Methods */

    private void initFromFile() throws UnknownHostException {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(name);

            Node root = document.getDocumentElement();
            NodeList set = root.getChildNodes();
            for (int i = 0; i < set.getLength(); i++) {
                Node temp = set.item(i);
                switch (temp.getNodeName()) {
                    case "WorkPath":
                        path = new File(temp.getTextContent());
                        break;
                    case "TimeSpan":
                        timeSpan = Integer.parseInt(temp.getTextContent());
                        break;
                    case "Port":
                        port = Integer.parseInt(temp.getTextContent());
                        break;
                    case "Adress":
                        ip = InetAddress.getByName(temp.getTextContent());
                        break;
                    case "StatName":
                        statName = new File(temp.getTextContent());
                        break;
                    case "WritingInterval":
                        writingInterval = Integer.parseInt(temp.getTextContent());
                        break;
                    default:
                        break;
                }
            }
            log.log(Level.FINE, "Settings has been loaded from file");
        }  catch (SAXException e) {
            log.log(Level.WARNING, "Document had not parsed.", e);
        } catch (ParserConfigurationException e) {
            log.log(Level.WARNING, "DocumentBuilder had not created", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "Document had not parsed", e);
        } catch (Exception e) {
            log.log(Level.WARNING, "Something wrong", e);
        }
    }


    /* Getters & Setters */

    public File getName() {
        return name;
    }

    public File getPath() {
        return path;
    }

    public int getTimeSpan() {
        return timeSpan;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getWritingInterval() {
        return writingInterval;
    }

    public void setWritingInterval(int writingInterval) {
        this.writingInterval = writingInterval;
    }

    public File getStatName() {
        return statName;
    }

    public void setStatName(File statName) {
        this.statName = statName;
    }


}
