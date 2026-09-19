package io.github.m1jawa.mcmodsmanager.cli;

import io.github.m1jawa.mcmodsmanager.model.*;
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

    public boolean run(Scanner scanner, List<String> requestedModNames, String gameVersion, ModLoader loader, Path targetPath) {

        List<String> modNames = new ArrayList<>(requestedModNames);
        List<SearchedModData> fetchedMods = new ArrayList<>();
        String currentModName = modNames.getFirst();
        SearchedModData selectedMod = null;
        boolean needsFetch = true;
        boolean itemSelected = false;
        int page = 1;

        while (true) {
            if (!itemSelected) { //List of mods

                //fetching mods if necessary
                if (needsFetch) {
                    try {
                        fetchedMods = ModrinthService.getSearchedModsData(currentModName, gameVersion, loader, pageSize, page);
                        needsFetch = false;

                    } catch (Exception e) {
                        InfoManager.log("Got an exception in MiSubcommand while fetching mods: " + e.getMessage(), InfoType.ERROR);
                        InfoManager.log("Skipping this mod", InfoType.INFO);

                        //next mod
                        modNames.removeFirst();
                        if (modNames.isEmpty()) return true;
                        currentModName = modNames.getFirst();
                        selectedMod = null;
                    }
                }

                //rendering list of mods
                renderListPage(fetchedMods, currentModName, page, allowSkip);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.matches("[0-9]")) {
                    int idx = Integer.parseInt(input);
                    if (idx < fetchedMods.size()) {
                        selectedMod = fetchedMods.get(idx);
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
                    modNames.removeFirst();
                    if (modNames.isEmpty()) return true;
                    currentModName = modNames.getFirst();
                    selectedMod = null;

                    //refresh the menu
                    needsFetch = true;

                } else if (input.equals("q")) {
                    return false;

                } else {
                    InfoManager.log("Invalid input", InfoType.ERROR);
                }

            } else {//Details about mod
                //rendering
                renderDetailPage(selectedMod, allowSkip);

                //asking user what to do next
                System.out.print("Enter choice: ");
                String input = scanner.nextLine().trim().toLowerCase();

                //parsing
                if (input.equals("d")) {
                    try {
                        provider.downloadMod(selectedMod, gameVersion, targetPath, false);
                        InfoManager.log("Downloaded " + selectedMod.name(), InfoType.SUCCESS);

                        if (exitOnDownload) return true;

                        modNames.removeFirst();
                        if (modNames.isEmpty()) return true;
                        currentModName = modNames.getFirst();
                        selectedMod = null;

                        //reset the menu
                        needsFetch = true;
                        itemSelected = false;

                    } catch (Exception e) {
                        InfoManager.logExceptionMessage(e);
                    }
                } else if (input.equals("b")) {
                    itemSelected = false;

                } else if (input.equals("s") && allowSkip) {
                    modNames.removeFirst();
                    if (modNames.isEmpty()) return true;
                    currentModName = modNames.getFirst();
                    selectedMod = null;

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

    public boolean run(Scanner scanner, String mod, String gameVersion, ModLoader loader, Path targetPath) {
        return run(scanner, List.of(mod), gameVersion, loader, targetPath);
    }

    private static void renderListPage(List<? extends IModData> mods, String query, int currentPage, boolean allowSkip) {

        int i = 0;

        System.out.printf("=== Search results | %s | Page: %d ===%n", query, currentPage);

        for (IModData mod : mods) {
            System.out.printf("[%d] %s%n", i++, mod.name());
        }

        StringBuilder controls = new StringBuilder("Controls: [n]ext page; ");
        if (currentPage > 1) controls.append("[p]revious page; ");
        if (allowSkip) controls.append("[s]kip this mod; ");
        controls.append("[0-9] select item; [q]uit").append(System.lineSeparator());

        System.out.println(controls);
    }

    private static void renderDetailPage(SearchedModData mod,  boolean allowSkip) {

        System.out.printf("=== Mod's Details ===%n");
        System.out.printf("Name: %s%n", mod.name());
        System.out.printf("Downloads: %s%n", parseDownloads(mod.downloads()));
        System.out.printf("Description: %s%n%n", mod.description());

        String skipPart = allowSkip ? "[s]kip this mod; " : "";
        System.out.printf("Controls: [d]ownload; %s[b]ack to list; [q]uit%n".formatted(skipPart));

    }

    private static String parseDownloads(int downloads) {
        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        fmt.setMaximumFractionDigits(1);
        return fmt.format(downloads);
    }
}
