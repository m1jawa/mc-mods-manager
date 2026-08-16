package io.github.m1jawa.mcmodsmanager.exceptions;

public class UnknownLoaderException extends Exception{

    private static final long serialVersionUID = 1L;

    public UnknownLoaderException(){
        super("Unknown loader");
    }

    public UnknownLoaderException(String errorMessage){
        super(errorMessage);
    }

    public UnknownLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
