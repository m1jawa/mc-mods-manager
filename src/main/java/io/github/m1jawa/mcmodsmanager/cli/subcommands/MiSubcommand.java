package io.github.m1jawa.mcmodsmanager.cli.subcommands;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Callable;

import io.github.m1jawa.mcmodsmanager.cli.InfoManager;
import io.github.m1jawa.mcmodsmanager.cli.ModDownloadWizard;
import io.github.m1jawa.mcmodsmanager.exceptions.UnknownLoaderException;
import io.github.m1jawa.mcmodsmanager.model.InfoType;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;
import io.github.m1jawa.mcmodsmanager.modrinth.ModrinthService;
import io.github.m1jawa.mcmodsmanager.net.ModDownloaderProvider;
import picocli.CommandLine.Command;

@Command(
    name = "mi",
    description = "Manual installation. Search mods by name and choose what to download"
)
public class MiSubcommand implements Callable<Integer>{

    private static final int PAGE_SIZE = 10;
    private static final ModDownloaderProvider provider = ModrinthService.getInstance();

    @Override
    public Integer call(){
        Scanner scanner = new Scanner(System.in);
        ModDownloadWizard downloadWizard = new ModDownloadWizard(PAGE_SIZE, true, false);

        // obtaining info about the mod
        System.out.print("Enter mod name: ");
        String modName = scanner.nextLine().trim();

        System.out.print("Enter game version: ");
        String gameVersion = scanner.nextLine().trim().toLowerCase().replaceAll("[^a-z0-9.-]", "");

        ModLoader loader = askForModLoader(scanner);

        System.out.print("Enter mod downloading path(./ by default): ");
        String pathInput = scanner.nextLine().trim();
        Path path;

        try {
            path = Path.of(pathInput); //empty input will be treated as ./
            if (!Files.exists(path)){
                throw new IOException();
            }
        } catch (InvalidPathException e) {
            InfoManager.log("Invalid path format, using current directory", InfoType.ERROR);
            path = Path.of(".");
        } catch (IOException e) {
            InfoManager.log("The folder does not exists, using current directory", InfoType.ERROR);
            path = Path.of(".");
        }

        boolean isDownloaded = downloadWizard.run(scanner, modName, gameVersion, loader, path);
        if (!isDownloaded) InfoManager.log("%s wasn't downloaded".formatted(modName), InfoType.INFO);

        return 0;
    }

    private static ModLoader askForModLoader(Scanner scanner){
        while(true){
            System.out.print("Enter Mod Loader: ");

            try {
                String input = scanner.nextLine().trim().toLowerCase();
                return ModLoader.fromString(input);
            } catch (UnknownLoaderException e) {
                InfoManager.log(e.getMessage() + " | Try again", InfoType.ERROR);
                System.out.println("Allowed values: " + ModLoader.getValues());
            }
        }
    }
}
