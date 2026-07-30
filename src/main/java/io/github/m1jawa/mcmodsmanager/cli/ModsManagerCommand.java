package io.github.m1jawa.mcmodsmanager.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.github.m1jawa.mcmodsmanager.file.ModsScanner;
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
                Path targetDir = (outputDir != null) ? outputDir : inputDir;

                System.out.println("=== Minecraft Mods Updater ===");
                System.out.printf("Target Version : %s%n", gameVersion);
                System.out.printf("Mod Loader     : %s%n", modLoader);
                System.out.printf("Source Dir     : %s%n", inputDir.toAbsolutePath());
                System.out.printf("Output Dir     : %s%n%n", targetDir.toAbsolutePath());

                try {
                    List<ModData> mods = ModsScanner.fetchAllFromDirectory(inputDir, ModLoader.valueOf(modLoader));

                    if (mods.isEmpty()) {
                        ErrorsManager.printCustomMessage("No valid mods found in directory: " + inputDir);
                        return 1;
                    }

                    System.out.printf("Found %d mods. Starting update...%n%n", mods.size());

                    AsyncDownloader.downloadAllViaModrinth(mods, gameVersion, targetDir, ModrinthService.getInstance());

                    System.out.println("All operations completed successfully!");
                    return 0;

                } catch (Exception e) {
                    ErrorsManager.printExceptionMessage(e);
                    return 1;
                }
            }
            default -> {
                ErrorsManager.printCustomMessage("Unknown mode: " + mode);
                return 1;
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ModsManagerCommand()).execute(args);
        System.exit(exitCode);
    }
}