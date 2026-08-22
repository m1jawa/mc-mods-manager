package io.github.m1jawa.mcmodsmanager.cli;

import io.github.m1jawa.mcmodsmanager.model.InfoType;


public class InfoManager {
    
    private InfoManager() {}

    public static void log(String log) {
        log(log, InfoType.UNSPECIFIED);
    }

    public static void log(String log, InfoType type) {
        switch (type) {
            case INFO -> System.out.println("[INFO]: " + log);
            case SUCCESS -> System.out.println("[SUCCESS]: " + log);
            case WARN -> System.out.println("[WARN]: " + log);
            case ERROR -> System.out.println("[ERROR]: " + log);
            default -> System.out.println("[?]: " + log);
        }
    }

    public static void logExceptionMessage(Throwable e){
        if (e == null) return;
        System.err.println(e.getMessage() != null ? e.getMessage() : e.toString());
    }
}
