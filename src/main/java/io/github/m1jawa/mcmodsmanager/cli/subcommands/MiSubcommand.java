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
        List<ModData> mods = new ArrayList<>();
        ModData selectedMod = null;
        boolean needsFetch = true;
        int page = 1;

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


        while (true) {
            if (selectedMod == null) { //List of mods

                //fetching mods if necessary
                if (needsFetch) {
                    try {
                        mods = ModrinthService.getSearchedModsData(modName, gameVersion, loader, PAGE_SIZE, page);
                        needsFetch = false;
                    } catch (Exception e) {
                        InfoManager.logExceptionMessage(e);
                        return 1;
                    }
                }

                //rendering list of mods
                renderListPage(mods, page);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.matches("[0-9]")) {
                    int idx = Integer.parseInt(input);
                    if (idx < mods.size()) {
                        selectedMod = mods.get(idx);
                    } else {
                        InfoManager.log("Invalid index. Choice out of bounds", InfoType.ERROR);
                    }
                } else if (input.equals("n")) {
                    if (mods.isEmpty() || mods.size() < PAGE_SIZE) {
                        InfoManager.log("You are already on the latest page", InfoType.ERROR);
                        continue;
                    }
                    page++;
                    needsFetch = true;
                } else if (input.equals("p")) {
                    if (page == 1) {
                        InfoManager.log("Can't go back, you are already on the first page", InfoType.ERROR);
                        continue;
                    }
                    page--;
                    needsFetch = true;
                } else if (input.equals("q")) {
                    return 0;
                } else {
                    InfoManager.log("Invalid input", InfoType.ERROR);
                }

            } else {//Details about mod
                //rendering
                renderDetailPage(selectedMod);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.equals("d")) {
                    try {
                        provider.downloadMod(selectedMod, gameVersion, path, false);
                        InfoManager.log("Downloaded " + selectedMod.name(), InfoType.SUCCESS);
                        selectedMod = null;
                    } catch (Exception e) {
                        InfoManager.logExceptionMessage(e);
                    }
                } else if (input.equals("b")) {
                    selectedMod = null;
                } else if (input.equals("q")) {
                    return 0;
                } else {
                    InfoManager.log("Invalid input", InfoType.ERROR);
                }
            }
        }
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

    private static void renderListPage(List<ModData> mods, int currentPage) {

        int i = 0;

        System.out.printf("=== Search results | Page: %d ===%n", currentPage);

        for (ModData md : mods) {
            System.out.printf("[%d] %s%n", i++, md.name());
        }

        StringBuilder controls = new StringBuilder("Controls: [n]ext page; ");
        if (currentPage > 1) controls.append("[p]revious page; ");
        controls.append("[0-9] select item; [q]uit").append(System.lineSeparator());

        System.out.println(controls);
    }

    private static void renderDetailPage(ModData mod) {

        System.out.printf("=== Mod's Details ===%n");
        System.out.printf("Name: %s%n", mod.name());
        System.out.printf("Downloads: %s%n", parseDownloads(mod.downloads()));
        System.out.printf("Description: %s%n%n", mod.description());

        System.out.println("Controls: [d]ownload; [b]ack to list; [q]uit");

    }

    private static String parseDownloads(int downloads) {
        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        fmt.setMaximumFractionDigits(1);
        return fmt.format(downloads);
    }
}
