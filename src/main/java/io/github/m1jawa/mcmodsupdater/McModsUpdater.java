package io.github.m1jawa.mcmodsupdater;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.github.m1jawa.mcmodsupdater.cli.ErrorsManager;
import io.github.m1jawa.mcmodsupdater.file.ModsScanner;
import io.github.m1jawa.mcmodsupdater.model.ModData;
import io.github.m1jawa.mcmodsupdater.model.ModLoader;
import io.github.m1jawa.mcmodsupdater.modrinth.ModrinthService;
import io.github.m1jawa.mcmodsupdater.net.AsyncDownloader;


public class McModsUpdater {

    public static void main(String[] args){

        Path modDir = Path.of("testmods");
        Path newModsDir = Path.of("newmods");
        String gameVer = "1.20.1";
        ModLoader loader = ModLoader.valueOf("FABRIC");

        //Getting list of mods in directory
        try {
            
            List<ModData> modsData = ModsScanner.fetchAllFromDirectory(modDir, loader);

            if (modsData.isEmpty()) {
                ErrorsManager.printCustomMessage("No valid mods found in " + modDir);
                return;
            } 
            
            System.out.printf("Found %d mods. Starting async download for Minecraft %s%n", modsData.size(), gameVer);

            AsyncDownloader.downloadAllViaModrinth(modsData, gameVer, newModsDir, ModrinthService.getInstance());

        } catch (IOException e) {
            ErrorsManager.printExceptionMessage(e);
        }

    }
}