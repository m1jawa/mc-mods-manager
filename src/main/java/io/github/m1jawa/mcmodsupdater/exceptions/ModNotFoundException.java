package io.github.m1jawa.mcmodsupdater.exceptions;

public class ModNotFoundException extends Exception{
    
    private static final long serialVersionUID = 1L;

    public ModNotFoundException(){
        super("Cant find manifest");
    }

    public ModNotFoundException(String errorMessage){
        super(errorMessage);
    }

    public ModNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
