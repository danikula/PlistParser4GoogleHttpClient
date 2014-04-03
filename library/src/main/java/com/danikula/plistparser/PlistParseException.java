package com.danikula.plistparser;

public class PlistParseException extends Exception {
    
    public PlistParseException(String message) {
        super(message);
    }

    public PlistParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlistParseException(Throwable cause) {
        super(cause);
    }
}
