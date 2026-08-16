package io.github.m1jawa.mcmodsmanager.cli;

import io.github.m1jawa.mcmodsmanager.cli.subcommands.FdSubommand;
import io.github.m1jawa.mcmodsmanager.cli.subcommands.MiSubcommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "mcmm",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "CLI tool to download Minecraft mods",
    subcommands = { FdSubommand.class, MiSubcommand.class }
)
public class ModsManagerCommand implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ModsManagerCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("=== Minecraft Mods Manager | modes ===");
        System.out.printf( "Usage: mcmm <mode> [options]%n%n");

        System.out.println("Available modes:");
        System.out.println("  fd    Scans directory for existing mods and downloads them for a target game version");
        System.out.printf( "  mi    Manual installation. Search mods by name and choose what to download (Unsupported yet) %n%n");

        System.out.println("For more information on a specific mode, run:");
        System.out.printf( "  mcmm <mode> --help%n%n");
    }
}