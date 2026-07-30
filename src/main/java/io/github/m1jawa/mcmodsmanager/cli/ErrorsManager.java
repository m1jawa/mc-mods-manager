package io.github.m1jawa.mcmodsmanager.cli;


public class ErrorsManager {

    private ErrorsManager() {}
    
    public static void printExceptionMessage(Throwable e){
        if (e == null) return;
        System.err.println(e.getMessage() != null ? e.getMessage() : e.toString());
    }

    public static void printCustomMessage(String message){
        System.err.println(message);
    }
}
