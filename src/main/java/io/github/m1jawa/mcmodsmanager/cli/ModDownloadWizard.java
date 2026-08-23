package io.github.m1jawa.mcmodsmanager.cli;

import io.github.m1jawa.mcmodsmanager.model.InfoType;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;
import io.github.m1jawa.mcmodsmanager.modrinth.ModrinthService;
import io.github.m1jawa.mcmodsmanager.net.ModDownloaderProvider;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ModDownloadWizard {
    private final ModDownloaderProvider provider = ModrinthService.getInstance();
    private final int pageSize;
    private final boolean exitOnDownload;
    private final boolean allowSkip;

    public ModDownloadWizard(int pageSize, boolean exitOnDownload, boolean allowSkip) {
        this.pageSize = pageSize;
        this.exitOnDownload = exitOnDownload;
        this.allowSkip = allowSkip;
    }

    public boolean run(Scanner scanner, List<ModData> requestedMods, String gameVersion, ModLoader loader, Path targetPath) {

        List<ModData> mods = new ArrayList<>(requestedMods);
        List<ModData> fetchedMods = new ArrayList<>();
        ModData currentMod = mods.getFirst();
        boolean needsFetch = true;
        boolean itemSelected = false;
        int page = 1;

        while (true) {
            if (!itemSelected) { //List of mods

                //fetching mods if necessary
                if (needsFetch) {
                    try {
                        fetchedMods = ModrinthService.getSearchedModsData(currentMod.name(), gameVersion, loader, pageSize, page);
                        needsFetch = false;

                    } catch (Exception e) {
                        InfoManager.log("Got an exception in MiSubcommand while fetching mods: " + e.getMessage(), InfoType.ERROR);
                        InfoManager.log("Skipping this mod", InfoType.INFO);

                        //next mod
                        mods.removeFirst();
                        if (mods.isEmpty()) return true;
                        currentMod = mods.getFirst();
                    }
                }

                //rendering list of mods
                renderListPage(fetchedMods, page);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.matches("[0-9]")) {
                    int idx = Integer.parseInt(input);
                    if (idx < fetchedMods.size()) {
                        currentMod = fetchedMods.get(idx);
                        itemSelected = true;
                    } else {
                        InfoManager.log("Invalid index. Choice out of bounds", InfoType.ERROR);
                    }

                } else if (input.equals("n")) {
                    if (fetchedMods.isEmpty() || fetchedMods.size() < pageSize) {
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

                } else if (input.equals("s") && allowSkip) {
                    mods.removeFirst();
                    if (mods.isEmpty()) return true;
                    currentMod = mods.getFirst();

                    //refresh the menu
                    needsFetch = true;

                } else if (input.equals("q")) {
                    return false;

                } else {
                    InfoManager.log("Invalid input", InfoType.ERROR);
                }

            } else {//Details about mod
                //rendering
                renderDetailPage(currentMod);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.equals("d")) {
                    try {
                        provider.downloadMod(currentMod, gameVersion, targetPath, false);
                        InfoManager.log("Downloaded " + currentMod.name(), InfoType.SUCCESS);

                        mods.removeFirst();
                        if (mods.isEmpty()) return true;
                        currentMod = mods.getFirst();

                        //reset the menu
                        needsFetch = true;
                        itemSelected = false;

                    } catch (Exception e) {
                        InfoManager.logExceptionMessage(e);
                    }
                } else if (input.equals("b")) {
                    itemSelected = false;

                } else if (input.equals("s") && allowSkip) {
                    mods.removeFirst();
                    if (mods.isEmpty()) return true;
                    currentMod = mods.getFirst();

                    needsFetch = true;
                    itemSelected = false;

                } else if (input.equals("q")) {
                    return false;

                } else {
                    InfoManager.log("Invalid input", InfoType.ERROR);
                }
            }
        }
    }

    public boolean run(Scanner scanner, String modName, String gameVersion, ModLoader loader, Path targetPath) {
        ModData mod = new ModData(null, modName, null, null, null);
        List<ModData> mods = new ArrayList<>(List.of(mod));

        return run(scanner, mods, gameVersion, loader, targetPath);
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
