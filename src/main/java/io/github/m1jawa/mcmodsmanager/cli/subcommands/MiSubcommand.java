package io.github.m1jawa.mcmodsmanager.cli.subcommands;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(
    name = "mi",
    description = "Manual installation. Search mods by name and choose what to download"
)
public class MiSubcommand implements Callable<Integer>{

    @Override
    public Integer call(){
        //TODO: manual installation
        return 0;
    }
}
