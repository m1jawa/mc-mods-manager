package io.github.m1jawa.mcmodsmanager.cli.subcommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import io.github.m1jawa.mcmodsmanager.cli.InfoManager;
import io.github.m1jawa.mcmodsmanager.cli.ModDownloadWizard;
import io.github.m1jawa.mcmodsmanager.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsmanager.exceptions.UnknownLoaderException;
import io.github.m1jawa.mcmodsmanager.file.ModsScanner;
import io.github.m1jawa.mcmodsmanager.model.InfoType;
import io.github.m1jawa.mcmodsmanager.model.LoadedModsData;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;
import io.github.m1jawa.mcmodsmanager.modrinth.ModrinthService;
import io.github.m1jawa.mcmodsmanager.net.AsyncDownloader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(
    name = "fd",
    description = "Scans directory for existing mods and downloads them for a target game version"
)
public class FdSubommand implements Callable<Integer>{
    @Option(
        names = {"-v", "--game-version"},
        description = "Target Minecraft version (e.g. 1.20.1, 26.3-snapshot-9).",
        required = true
    )
    private String gameVersion;

    @Option(
        names = {"-i", "--input-dir"},
        defaultValue = "./",
        description = "Directory containing existing mod JAR files (default: ${DEFAULT-VALUE})."
    )
    private Path inputDir;

    @Option(
        names = {"-o", "--output-dir"},
        description = "Target directory to save downloaded mods. Defaults to input directory if not specified.",
        required = true
    )
    private Path outputDir;

    @Option(
        names = {"-l", "--loader"},
        description = "Mod loader to target (values: fabric).",
        required = true
    )
    private String modLoader;


    @Override
    public Integer call() {

        Scanner scanner = new Scanner(System.in);

        if ( gameVersion == null ) {
            InfoManager.log("Version was not entered", InfoType.ERROR);
            return 0;
        }

        if (!Files.exists(inputDir)) {
            InfoManager.log("Input directory does not exists", InfoType.ERROR);
            return 0;
        }

        if (!Files.exists(outputDir)) {
            InfoManager.log("Output directory does not exists", InfoType.ERROR);
            return 0;
        }

        try {
            ModLoader.fromString(modLoader);
        } catch (UnknownLoaderException e) {
            InfoManager.log("Unknown mod loader", InfoType.ERROR);
            return 0;
        }

        Path targetDir = (outputDir != null) ? outputDir : inputDir;

        System.out.println("=== Minecraft Mods Manager | downloading ===");
        System.out.printf("Target version : %s%n", gameVersion);
        System.out.printf("Mod loader     : %s%n", modLoader);
        System.out.printf("Source dir     : %s%n", inputDir.toAbsolutePath());
        System.out.printf("Output dir     : %s%n%n", targetDir.toAbsolutePath());

        try {
            List<ModData> mods = ModsScanner.fetchAllFromDirectory(inputDir, ModLoader.fromString(modLoader));

            if (mods == null || mods.isEmpty()) {
                InfoManager.log("No valid mods found in directory: " + inputDir, InfoType.ERROR);
                return 1;
            }

            InfoManager.log(
                "Found %d mods. Starting download...%n%n".formatted(mods.size()),
                InfoType.INFO
            );

            LoadedModsData downloadedModsResult = AsyncDownloader.downloadAllViaModrinth(mods, gameVersion, targetDir, ModrinthService.getInstance());

            if (downloadedModsResult.failedCount() == 0) {
                InfoManager.log(
                    "All %d mods downloaded succesfully!".formatted(downloadedModsResult.totalMods()),
                    InfoType.SUCCESS
                );
            } else {
                InfoManager.log(
                    "Downloaded only %d mods of %d".formatted(downloadedModsResult.succesCount(), downloadedModsResult.totalMods()),
                    InfoType.SUCCESS
                );

                String answer = "";
                while (!(answer.equals("y") || answer.equals("n"))) {
                    System.out.print("Do you want manually download rest of the mods? (y/n): ");
                    answer = scanner.nextLine().trim();
                }

                if (answer.equals("n")) return 0;

                ModDownloadWizard downloadWizard = new ModDownloadWizard(10, true, true);
                boolean completed = downloadWizard.run(scanner, downloadedModsResult.failedMods(), gameVersion, ModLoader.fromString(modLoader), outputDir);

                if (completed) InfoManager.log("Installed all mods", InfoType.SUCCESS);
                if (!completed) InfoManager.log("Downloading was interrupted by the user", InfoType.INFO);
            }

            return 0;

        } catch (IOException | ManifestNotFoundException | UnknownLoaderException e) {
            InfoManager.log(e.getMessage(), InfoType.ERROR);
            return 0;
        }
    }
}
