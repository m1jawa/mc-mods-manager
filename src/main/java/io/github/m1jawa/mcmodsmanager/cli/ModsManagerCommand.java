package io.github.m1jawa.mcmodsmanager.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.github.m1jawa.mcmodsmanager.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsmanager.file.ModsScanner;
import io.github.m1jawa.mcmodsmanager.model.InfoType;
import io.github.m1jawa.mcmodsmanager.model.LoadedModsData;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;
import io.github.m1jawa.mcmodsmanager.modrinth.ModrinthService;
import io.github.m1jawa.mcmodsmanager.net.AsyncDownloader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "mcmm",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "CLI tool to download Minecraft mods manually or automatically copying modpacks to another game version."
)
public class ModsManagerCommand implements Callable<Integer> {

    @Parameters(
        index = "0",
        defaultValue = "fd",
        description = "Mode of operation: fd(installing mods obtained from a dirrectory)"
    )
    private String mode;


    @Option(
        names = {"-v", "--game-version"},
        description = "Target Minecraft version (e.g. 1.20.1, 1.20.4)."
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
        description = "Target directory to save downloaded mods. Defaults to input directory if not specified."
    )
    private Path outputDir;

    @Option(
        names = {"-l", "--loader"},
        defaultValue = "FABRIC",
        description = "Mod loader to target (values: FABRIC; default: ${DEFAULT-VALUE})."
    )
    private String modLoader;


    @Override
    public Integer call() {
        
        switch (mode.toLowerCase()) {
            case "fd" -> {

                if ( gameVersion == null ) {
                    InfoManager.log("Version was not entered", InfoType.ERROR);
                    return 1;
                }

                Path targetDir = (outputDir != null) ? outputDir : inputDir;

                System.out.println("=== Minecraft Mods Manager | downloading ===");
                System.out.printf("Target version : %s%n", gameVersion);
                System.out.printf("Mod loader     : %s%n", modLoader);
                System.out.printf("Source dir     : %s%n", inputDir.toAbsolutePath());
                System.out.printf("Output dir     : %s%n%n", targetDir.toAbsolutePath());

                try {
                    List<ModData> mods = ModsScanner.fetchAllFromDirectory(inputDir, ModLoader.valueOf(modLoader));

                    if (mods.isEmpty()) {
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

                        //TODO: asking if user wants manually install failed mods, manaul installation
                    }

                    return 0;

                } catch (IOException | ManifestNotFoundException e) {
                    InfoManager.log(e.getMessage(), InfoType.ERROR);
                    return 1;
                }
            }
            default -> {
                InfoManager.log("Unknown mode: " + mode, InfoType.ERROR);
                return 1;
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ModsManagerCommand()).execute(args);
        System.exit(exitCode);
    }
}