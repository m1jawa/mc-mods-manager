package io.github.m1jawa.mcmodsupdater.exceptions;

public class ManifestNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public ManifestNotFoundException(){
        super("Cant find manifest");
    }

    public ManifestNotFoundException(String errorMessage){
        super(errorMessage);
    }

    public ManifestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
