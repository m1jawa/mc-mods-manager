package io.github.m1jawa.mcmodsupdater;


import java.io.IOException;

import io.github.m1jawa.mcmodsupdater.cli.ErrorsManager;
import io.github.m1jawa.mcmodsupdater.cli.InfoManager;
import io.github.m1jawa.mcmodsupdater.net.HttpManager;


public class McModsUpdater {

    public static void main(String[] args) {

        String url = ModrinthApiManager.getModSearchUrl("sodium", "1.20.1", "fabric"); // temporality debug data

        try {
            InfoManager.printHttpResponse(HttpManager.sendRequest(url));
        } catch (IOException | InterruptedException e) {
            ErrorsManager.printExceptionMessage(e);
        }
    }
}