package io.github.m1jawa.mcmodsmanager.exceptions;

public class UnexpectedResponseStructureException extends Exception{
    
    private static final long serialVersionUID = 1L;

    public UnexpectedResponseStructureException(){
        super("Cant find manifest");
    }

    public UnexpectedResponseStructureException(String errorMessage){
        super(errorMessage);
    }

    public UnexpectedResponseStructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
