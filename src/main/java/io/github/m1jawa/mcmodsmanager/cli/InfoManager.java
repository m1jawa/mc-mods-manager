package io.github.m1jawa.mcmodsmanager.cli;

import io.github.m1jawa.mcmodsmanager.model.InfoType;


public class InfoManager {
    
    private InfoManager() {}

    public static void log(String log) {
        log(log, InfoType.UNSPECIFIED);
    }

    public static void log(String log, InfoType type) {
        switch (type) {
            case INFO -> { System.out.println("[INFO]: " + log); break; }
            case SUCCESS -> { System.out.println("[SUCCES]: " + log); break; }
            case WARN -> { System.out.println("[WARN]: " + log); break; }
            case ERROR -> { System.err.println("[ERROR]: " + log); break; }
            default -> { System.out.println("[?]: " + log); break; }
        }
    }

    public static void logExceptionMessage(Throwable e){
        if (e == null) return;
        System.err.println(e.getMessage() != null ? e.getMessage() : e.toString());
    }
}
