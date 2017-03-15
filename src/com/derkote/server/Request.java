/**
 * Created by derkote .
 */
package com.derkote.server;

public class Request implements Message {
    private RequestType requestType;
    private String fileName;


    /* Constructors */

    public Request(RequestType type) {
        this.requestType = type;
        this.fileName = null;
    }

    public Request(RequestType type, String fileName) {
        this(type);
        this.fileName = fileName;
    }


    /* Methods */

    public RequestType getRequestType() {
        return requestType;
    }

    public String getFileName() {
        return fileName;
    }
}
